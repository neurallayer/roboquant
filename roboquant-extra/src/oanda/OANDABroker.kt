package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.account.AccountID
import com.oanda.v20.primitives.InstrumentType
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.CurrencyConverter
import org.roboquant.brokers.Position
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

class OANDABroker(
    private var accountID: String? = null,
    token: String? = null,
    url: String = "https://api-fxpractice.oanda.com/",
    currencyConverter: CurrencyConverter? = null,
) : Broker {

    private lateinit var ctx: Context
    override val account: Account = Account(currencyConverter = currencyConverter)
    private val logger = Logging.getLogger("OANDABroker")

    val availableAssets by lazy {
        val instruments = ctx.account.instruments(AccountID(accountID)).instruments
        instruments.map {
            val currency = it.name.split('_').last()
            val type = when (it.type!!) {
                InstrumentType.CURRENCY -> AssetType.FOREX
                InstrumentType.CFD -> AssetType.CFD
                InstrumentType.METAL -> AssetType.CFD
            }
            Asset(it.name.toString(), type = type, currencyCode = currency)
        }.associateBy { it.symbol }
    }


    init {
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        accountID = accountID ?: Config.getProperty("OANDA_ACCOUNT_ID")
        ctx = ContextBuilder(url)
            .setToken(apiToken)
            .setApplication("roboquant")
            .build()

        initAccount()
        logger.info("Retrieved account with id $accountID")
    }


    private fun initAccount() {
        val accounts = ctx.account.list().accounts
        if (accountID != null) {
            assert(accountID in accounts.map { it.id.toString() })
        } else {
            accountID = accounts.first().id.toString()
        }

        val summary = ctx.account.summary(AccountID(accountID)).account
        account.baseCurrency = Currency.getInstance(summary.currency.toString())
        account.cash.deposit(account.baseCurrency, summary.balance.doubleValue())
        val positions = ctx.position.list(AccountID(accountID)).positions

        for (p in positions) {
            val asset = availableAssets[p.instrument.toString()]!!
            if (p.long.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.long.units.doubleValue(), p.long.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
            if (p.short.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.short.units.doubleValue(), p.short.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
        }

        account.time = Instant.now()
    }

    /**
     * TODO implement this logic, right now all orders will be rejected.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        orders.forEach { it.status = OrderStatus.REJECTED }
        account.orders.addAll(orders)
        account.portfolio.updateMarketPrices(event)
        return account
    }
}