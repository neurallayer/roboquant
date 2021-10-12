package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.account.AccountID
import com.oanda.v20.pricing.PricingGetRequest
import kotlinx.coroutines.delay
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Background
import org.roboquant.common.Config
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.OrderBook
import java.time.Instant

/**
 * Retrieve live  data from OANDA.
 */
class OANDALiveFeed(
    private var accountID: String? = null,
    token: String? = null,
    url: String = "https://api-fxpractice.oanda.com/"
) : LiveFeed() {

    private val ctx: Context
    private val assetMap = mutableMapOf<String, Asset>()

    val assets
        get() = assetMap.values.toSortedSet()

    init {
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        ctx = ContextBuilder(url)
            .setToken(apiToken)
            .setApplication("roboquantLiveFeed")
            .build()
    }

    fun subscribeOrderBook(vararg symbols: String) {
        val accounts = ctx.account.list().accounts
        if (accountID != null) {
            assert(accountID in accounts.map { it.id.toString() })
        } else {
            accountID = accounts.first().id.toString()
        }

        symbols.forEach {
            assetMap[it] = Asset(it, AssetType.FOREX)
        }

        require(accountID != null) { }
        val job = Background.ioJob {
            while (true) {
                if (channel != null) {
                    val request = PricingGetRequest(AccountID(accountID), symbols.toList())
                    val resp = ctx.pricing[request]
                    val now = Instant.now()
                    val actions = resp.prices.map {
                        val asset = assetMap[it.instrument.toString()]!!
                        OrderBook(
                            asset,
                            it.asks.map { entry ->
                                OrderBook.OrderBookEntry(
                                    entry.liquidity.toDouble(),
                                    entry.price.doubleValue()
                                )
                            },
                            it.bids.map { entry ->
                                OrderBook.OrderBookEntry(
                                    entry.liquidity.toDouble(),
                                    entry.price.doubleValue()
                                )
                            }
                        )
                    }
                    channel?.offer(Event(actions, now))
                    request.setSince(resp.time)
                }
                delay(1_000)
            }

        }
    }

}