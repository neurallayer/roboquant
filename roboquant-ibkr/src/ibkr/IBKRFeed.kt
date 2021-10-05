package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant


/**
 * Get realtime bars from IBKR. Please note that often you need paid subscriptions to get this
 * data and additional there are limitations to the frequency of API calls you can make.
 *
 * The default settings like the port number are the ones for a paper trading account. It is easy to
 * share the market data subscriptions between live and paper trading accounts, so it is recommended to
 * use a paper trading account if possible at all.
 *
 * @constructor
 *
 * @param host The host to connect to
 * @param port The port to connect to
 * @param clientId The client id to use. By default, roboquant uses clientId=2 for the IBKR feed
 */
class IBKRFeed(host: String = "127.0.0.1", port: Int = 4002, clientId: Int = 2) : LiveFeed() {

    private var tickerId: Int = 0
    private val wrapper: Wrapper
    private val ibkrConnection: IBKRConnection
    private val subscriptions = mutableMapOf<Int, Asset>()
    val logger = Logging.getLogger("IBKRFeed")

    init {
        wrapper = Wrapper()
        ibkrConnection = IBKRConnection(wrapper, logger)
        ibkrConnection.connect(host, port, clientId)
        ibkrConnection.client.reqCurrentTime()
    }

    fun disconnect() = ibkrConnection.disconnect()

    /**
     * Subscribe to the realtime bars for a particular contract. Often IBKR platform requires a subscription in order
     * to be able to receive realtime bars. Please check the documentation at hte IBKR website for more details.
     *
     * @param assets
     */
    fun subscribe(vararg assets: Asset, interval: Int = 5, type: String = "MIDPOINT") {
        for (asset in assets) {
            try {
                val contract = ibkrConnection.getContract(asset)
                ibkrConnection.client.reqRealTimeBars(++tickerId, contract, interval, type, false, arrayListOf())
                subscriptions[tickerId] = asset
                logger.info("Added subscription to receive realtime bars for ${contract.symbol()}")
            } catch (e: Exception) {
                e.printStackTrace()
                logger.warning(e.message)
            }
        }
    }


    inner class Wrapper : DefaultEWrapper() {

        override fun realtimeBar(
            reqId: Int,
            time: Long,
            open: Double,
            high: Double,
            low: Double,
            close: Double,
            volume: Long,
            wap: Double,
            count: Int
        ) {
            val asset = subscriptions[reqId]
            if (asset == null) {
                logger.warning("unexpected realtimeBar received with request id $reqId")
            } else {
                val action =
                    PriceBar(asset, open.toFloat(), high.toFloat(), low.toFloat(), close.toFloat(), volume.toFloat())
                val now = Instant.ofEpochSecond(time) // IBKR uses seconds resolution
                val event = Event(listOf(action), now)
                channel?.offer(event)
            }
        }

        override fun error(var1: Exception) {
            logger.warning { "$var1" }
        }

        override fun error(var1: String?) {
            logger.warning { "$var1" }
        }

        override fun error(var1: Int, var2: Int, var3: String?) {
            if (var1 == -1)
                logger.fine { "$var1 $var2 $var3" }
            else
                logger.warning { "$var1 $var2 $var3" }
        }

    }


}

