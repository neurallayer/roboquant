package oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import org.roboquant.brokers.Position
import org.roboquant.brokers.UsageCalculator
import org.roboquant.common.Cash
import kotlin.math.absoluteValue

class OANDAUsageCalculator(ctx: Context, accountID: AccountID) : UsageCalculator {

    private val rates: Map<String, Double>

    init {
        val instruments = ctx.account.instruments(accountID).instruments
        rates = instruments.associate { it.name.toString() to it.marginRate.doubleValue() }
    }


    override fun calculate(positions: List<Position>, changes: List<Position>): Cash {
        val result = Cash()

         for (p in positions) {
             val rate = rates[p.asset.symbol] !!
             result.deposit(p.asset.currency, p.totalCost.absoluteValue * rate)
         }
        return result
    }
}