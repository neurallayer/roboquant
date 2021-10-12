package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.account.AccountID
import com.oanda.v20.primitives.InstrumentType
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.CurrencyConverter
import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.Currency
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import java.time.Instant

class OANDABroker(
    private var accountID: String? = null,
    token: String? = null,
    url: String = "https://api-fxpractice.oanda.com/",
    currencyConverter: CurrencyConverter? = null,
) : Broker {

    private lateinit var ctx: Context
    override val account: Account = Account(currencyConverter = currencyConverter)

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

    override fun place(orders: List<Order>, event: Event): Account {
        TODO("Not yet implemented")
    }
}