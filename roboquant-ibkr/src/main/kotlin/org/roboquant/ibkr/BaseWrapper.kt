package org.roboquant.ibkr

import com.ib.client.*
import org.roboquant.common.Logging
import java.time.Instant
import kotlin.math.absoluteValue

@Suppress("TooManyFunctions")
internal open class BaseWrapper(private val logger: Logging.Logger) : EWrapper {


    override fun tickPrice(tickerId: Int, field: Int, price: Double, attrib: TickAttrib?) {
        logger.trace("Not yet implemented")
    }

    override fun tickSize(tickerId: Int, field: Int, size: Decimal?) {
        logger.trace("Not yet implemented")
    }

    override fun tickOptionComputation(
        tickerId: Int,
        field: Int,
        tickAttrib: Int,
        impliedVol: Double,
        delta: Double,
        optPrice: Double,
        pvDividend: Double,
        gamma: Double,
        vega: Double,
        theta: Double,
        undPrice: Double
    ) {
        logger.trace("Not yet implemented")
    }

    override fun tickGeneric(tickerId: Int, tickType: Int, value: Double) {
        logger.trace("Not yet implemented")
    }

    override fun tickString(tickerId: Int, tickType: Int, value: String?) {
        logger.trace("Not yet implemented")
    }

    override fun tickEFP(
        tickerId: Int,
        tickType: Int,
        basisPoints: Double,
        formattedBasisPoints: String?,
        impliedFuture: Double,
        holdDays: Int,
        futureLastTradeDate: String?,
        dividendImpact: Double,
        dividendsToLastTradeDate: Double
    ) {
        logger.trace("Not yet implemented")
    }

    override fun orderStatus(
        orderId: Int,
        status: String,
        filled: Decimal?,
        remaining: Decimal?,
        avgFillPrice: Double,
        permId: Int,
        parentId: Int,
        lastFillPrice: Double,
        clientId: Int,
        whyHeld: String?,
        mktCapPrice: Double
    ) {
        logger.trace("Not yet implemented")
    }

    override fun openOrder(orderId: Int, contract: Contract, order: Order, orderState: OrderState) {
        logger.trace("Not yet implemented")
    }

    override fun openOrderEnd() {
        logger.trace("Not yet implemented")
    }

    override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
        logger.trace("Not yet implemented")
    }

    override fun updatePortfolio(
        contract: Contract,
        position: Decimal,
        marketPrice: Double,
        marketValue: Double,
        averageCost: Double,
        unrealizedPNL: Double,
        realizedPNL: Double,
        accountName: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun updateAccountTime(timeStamp: String?) {
        logger.trace("Not yet implemented")
    }

    override fun accountDownloadEnd(accountName: String?) {
        logger.trace("Not yet implemented")
    }

    override fun nextValidId(orderId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun contractDetails(reqId: Int, contractDetails: ContractDetails) {
        logger.trace("Not yet implemented")
    }

    override fun bondContractDetails(reqId: Int, contractDetails: ContractDetails) {
        logger.trace("Not yet implemented")
    }

    override fun contractDetailsEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun execDetails(reqId: Int, contract: Contract, execution: Execution) {
        logger.trace("Not yet implemented")
    }

    override fun execDetailsEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun updateMktDepth(
        tickerId: Int,
        position: Int,
        operation: Int,
        side: Int,
        price: Double,
        size: Decimal?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun updateMktDepthL2(
        tickerId: Int,
        position: Int,
        marketMaker: String?,
        operation: Int,
        side: Int,
        price: Double,
        size: Decimal?,
        isSmartDepth: Boolean
    ) {
        logger.trace("Not yet implemented")
    }

    override fun updateNewsBulletin(msgId: Int, msgType: Int, message: String?, origExchange: String?) {
        logger.trace("Not yet implemented")
    }

    override fun managedAccounts(accountsList: String?) {
        logger.trace("Not yet implemented")
    }

    override fun receiveFA(faDataType: Int, xml: String?) {
        logger.trace("Not yet implemented")
    }

    override fun historicalData(reqId: Int, bar: Bar) {
        logger.trace("Not yet implemented")
    }

    override fun scannerParameters(xml: String?) {
        logger.trace("Not yet implemented")
    }

    override fun scannerData(
        reqId: Int,
        rank: Int,
        contractDetails: ContractDetails?,
        distance: String?,
        benchmark: String?,
        projection: String?,
        legsStr: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun scannerDataEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun realtimeBar(
        reqId: Int,
        time: Long,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        volume: Decimal?,
        wap: Decimal?,
        count: Int
    ) {
        logger.trace("Not yet implemented")
    }

    override fun currentTime(time: Long) {
        logger.info { EWrapperMsgGenerator.currentTime(time).toString() }

        // If more than 60 seconds difference, give a warning
        val diff = Instant.now().epochSecond - time
        if (diff.absoluteValue > 60) logger.warn("Time clocks out of sync by $diff seconds")
    }

    override fun fundamentalData(reqId: Int, data: String?) {
        logger.trace("Not yet implemented")
    }

    override fun deltaNeutralValidation(reqId: Int, deltaNeutralContract: DeltaNeutralContract?) {
        logger.trace("Not yet implemented")
    }

    override fun tickSnapshotEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun marketDataType(reqId: Int, marketDataType: Int) {
        logger.trace("Not yet implemented")
    }

    override fun commissionReport(commissionReport: CommissionReport) {
        logger.trace("Not yet implemented")
    }

    override fun position(account: String?, contract: Contract?, pos: Decimal?, avgCost: Double) {
        logger.trace("Not yet implemented")
    }

    override fun positionEnd() {
        logger.trace("Not yet implemented")
    }

    override fun accountSummary(reqId: Int, account: String?, tag: String, value: String?, currency: String?) {
        logger.trace("Not yet implemented")
    }

    override fun accountSummaryEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun verifyMessageAPI(apiData: String?) {
        logger.trace("Not yet implemented")
    }

    override fun verifyCompleted(isSuccessful: Boolean, errorText: String?) {
        logger.trace("Not yet implemented")
    }

    override fun verifyAndAuthMessageAPI(apiData: String?, xyzChallenge: String?) {
        logger.trace("Not yet implemented")
    }

    override fun verifyAndAuthCompleted(isSuccessful: Boolean, errorText: String?) {
        logger.trace("Not yet implemented")
    }

    override fun displayGroupList(reqId: Int, groups: String?) {
        logger.trace("Not yet implemented")
    }

    override fun displayGroupUpdated(reqId: Int, contractInfo: String?) {
        logger.trace("Not yet implemented")
    }

    override fun error(e: Exception?) {
        logger.warn("Received exception", e)
    }

    override fun error(str: String?) {
        logger.warn { "$str" }
    }

    override fun error(id: Int, errorCode: Int, errorMsg: String?, advancedOrderRejectJson: String?) {
        if (id == -1)
            logger.debug { "$id $errorCode $errorMsg $advancedOrderRejectJson" }
        else
            logger.warn { "$id $errorCode $errorMsg $advancedOrderRejectJson" }
    }

    override fun connectionClosed() {
        logger.trace("Not yet implemented")
    }

    override fun connectAck() {
        logger.trace("Not yet implemented")
    }

    override fun positionMulti(
        reqId: Int,
        account: String?,
        modelCode: String?,
        contract: Contract?,
        pos: Decimal?,
        avgCost: Double
    ) {
        logger.trace("Not yet implemented")
    }

    override fun positionMultiEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun accountUpdateMulti(
        reqId: Int,
        account: String?,
        modelCode: String?,
        key: String?,
        value: String?,
        currency: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun accountUpdateMultiEnd(reqId: Int) {
        logger.info("accountUpdateMultiEnd")
    }

    override fun securityDefinitionOptionalParameter(
        reqId: Int,
        exchange: String?,
        underlyingConId: Int,
        tradingClass: String?,
        multiplier: String?,
        expirations: Set<String>?,
        strikes: Set<Double>?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun securityDefinitionOptionalParameterEnd(reqId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun softDollarTiers(reqId: Int, tiers: Array<SoftDollarTier?>?) {
        logger.trace("Not yet implemented")
    }

    override fun familyCodes(familyCodes: Array<FamilyCode?>?) {
        logger.trace("Not yet implemented")
    }

    override fun symbolSamples(reqId: Int, contractDescriptions: Array<ContractDescription?>?) {
        logger.trace("Not yet implemented")
    }

    override fun historicalDataEnd(reqId: Int, startDateStr: String?, endDateStr: String?) {
        logger.trace("Not yet implemented")
    }

    override fun mktDepthExchanges(depthMktDataDescriptions: Array<DepthMktDataDescription?>?) {
        logger.trace("Not yet implemented")
    }

    override fun tickNews(
        tickerId: Int,
        timeStamp: Long,
        providerCode: String?,
        articleId: String?,
        headline: String?,
        extraData: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun smartComponents(reqId: Int, theMap: Map<Int, Map.Entry<String, Char>>?) {
        logger.trace("Not yet implemented")
    }

    override fun tickReqParams(tickerId: Int, minTick: Double, bboExchange: String?, snapshotPermissions: Int) {
        logger.trace("Not yet implemented")
    }

    override fun newsProviders(newsProviders: Array<NewsProvider?>?) {
        logger.trace("Not yet implemented")
    }

    override fun newsArticle(requestId: Int, articleType: Int, articleText: String?) {
        logger.trace("Not yet implemented")
    }

    override fun historicalNews(
        requestId: Int,
        time: String?,
        providerCode: String?,
        articleId: String?,
        headline: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun historicalNewsEnd(requestId: Int, hasMore: Boolean) {
        logger.trace("Not yet implemented")
    }

    override fun headTimestamp(reqId: Int, headTimestamp: String) {
        logger.trace("Not yet implemented")
    }

    override fun histogramData(reqId: Int, items: List<HistogramEntry>?) {
        logger.trace("Not yet implemented")
    }

    override fun historicalDataUpdate(reqId: Int, bar: Bar) {
        logger.trace("Not yet implemented")
    }

    override fun rerouteMktDataReq(reqId: Int, conId: Int, exchange: String) {
        logger.trace("Not yet implemented")
    }

    override fun rerouteMktDepthReq(reqId: Int, conId: Int, exchange: String) {
        logger.trace("Not yet implemented")
    }

    override fun marketRule(marketRuleId: Int, priceIncrements: Array<PriceIncrement?>?) {
        logger.trace("Not yet implemented")
    }

    override fun pnl(reqId: Int, dailyPnL: Double, unrealizedPnL: Double, realizedPnL: Double) {
        logger.trace("Not yet implemented")
    }

    override fun pnlSingle(
        reqId: Int,
        pos: Decimal?,
        dailyPnL: Double,
        unrealizedPnL: Double,
        realizedPnL: Double,
        value: Double
    ) {
        logger.trace("Not yet implemented")
    }

    override fun historicalTicks(reqId: Int, ticks: List<HistoricalTick>?, done: Boolean) {
        logger.trace("Not yet implemented")
    }

    override fun historicalTicksBidAsk(reqId: Int, ticks: List<HistoricalTickBidAsk>?, done: Boolean) {
        logger.trace("Not yet implemented")
    }

    override fun historicalTicksLast(reqId: Int, ticks: List<HistoricalTickLast>?, done: Boolean) {
        logger.trace("Not yet implemented")
    }

    override fun tickByTickAllLast(
        reqId: Int,
        tickType: Int,
        time: Long,
        price: Double,
        size: Decimal?,
        tickAttribLast: TickAttribLast?,
        exchange: String?,
        specialConditions: String?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun tickByTickBidAsk(
        reqId: Int,
        time: Long,
        bidPrice: Double,
        askPrice: Double,
        bidSize: Decimal?,
        askSize: Decimal?,
        tickAttribBidAsk: TickAttribBidAsk?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun tickByTickMidPoint(reqId: Int, time: Long, midPoint: Double) {
        logger.trace("Not yet implemented")
    }

    override fun orderBound(orderId: Long, apiClientId: Int, apiOrderId: Int) {
        logger.trace("Not yet implemented")
    }

    override fun completedOrder(contract: Contract?, order: Order?, orderState: OrderState?) {
        logger.trace("Not yet implemented")
    }

    override fun completedOrdersEnd() {
        logger.trace("Not yet implemented")
    }

    override fun replaceFAEnd(reqId: Int, text: String?) {
        logger.trace("Not yet implemented")
    }

    override fun wshMetaData(reqId: Int, dataJson: String?) {
        logger.trace("Not yet implemented")
    }

    override fun wshEventData(reqId: Int, dataJson: String?) {
        logger.trace("Not yet implemented")
    }

    override fun historicalSchedule(
        reqId: Int,
        startDateTime: String?,
        endDateTime: String?,
        timeZone: String?,
        sessions: List<HistoricalSession>?
    ) {
        logger.trace("Not yet implemented")
    }

    override fun userInfo(reqId: Int, whiteBrandingId: String?) {
        logger.trace("Not yet implemented")
    }
}