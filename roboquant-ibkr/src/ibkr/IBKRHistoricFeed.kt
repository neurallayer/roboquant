package ibkr

import com.ib.client.Bar
import com.ib.client.DefaultEWrapper
import com.ib.client.EClientSocket
import org.roboquant.common.Asset
import org.roboquant.feeds.*
import org.roboquant.feeds.csv.AutoDetectTimeParser
import org.roboquant.ibkr.IBKRConnection
import java.time.Instant
import java.util.*
import java.util.logging.Logger

class IBKRHistoricFeed(
    host: String = "127.0.0.1",
    port: Int = 4002,
    clientId: Int = 1,
) : HistoricFeed {

    private var tickerId: Int = 0
    private val subscriptions = mutableMapOf<Int, Asset>()
    private val logger: Logger = Logger.getLogger(this.javaClass.simpleName)
    private val events = TreeMap<Instant, MutableList<PriceAction>>()
    private var client: EClientSocket

    override val timeline: List<Instant>
        get() = events.keys.toList()

    override val assets
        get() = events.values.map { priceBars -> priceBars.map { it.asset }.distinct() }.flatten().distinct()
            .toSortedSet()

    init {
        val wrapper = Wrapper()
        client = IBKRConnection.connect(wrapper, host, port, clientId)
        client.reqCurrentTime()
    }

    fun disconnect() = client.eDisconnect()

    /**
     * Historical Data requests need to be assembled in such a way that only a few thousand bars are returned at a time.
     * So you cannot retrieve short barSize for a very long duration.
     */
    fun retrieve(
        vararg assets: Asset,
        endDate: Instant = Instant.now(),
        duration: String = "1 Y",
        barSize: String = "1 day",
        dataType: String = "TRADES"
    ) {
        for (asset in assets) {
            val contract = IBKRConnection.getContract(asset)
            val formatted = IBKRConnection.getFormattedTime(endDate)
            client.reqHistoricalData(++tickerId, contract, formatted, duration, barSize, dataType, 1, 1, false, null)
            subscriptions[tickerId] = asset
        }
        // TODO better implementation
        while (subscriptions.isNotEmpty()) Thread.sleep(1_000)
    }

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        events.forEach {
            val event = Event(it.value, it.key)
            channel.send(event)
        }
    }

    inner class Wrapper : DefaultEWrapper() {

        override fun historicalData(reqId: Int, bar: Bar) {
            val asset = subscriptions[reqId]!!
            val action = PriceBar(
                asset, bar.open(), bar.high(), bar.low(), bar.close(), bar.volume().toDouble()
            )
            val parser = AutoDetectTimeParser(asset.exchangeCode)
            val time = parser.parse(bar.time())
            val event = events.getOrPut(time) { mutableListOf() }
            event.add(action)
        }

        override fun historicalDataEnd(reqId: Int, startDateStr: String, endDateStr: String) {
            val v = subscriptions.remove(reqId)
            logger.info("Finished retrieving $v")
        }
    }

}