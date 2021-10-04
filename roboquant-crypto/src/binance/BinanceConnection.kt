package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config

internal val binanceTemplate = Asset("TEMPLATE", AssetType.CRYPTO, exchangeCode = "BINANCE")

internal object BinanceConnection {

    private const val KEY_NAME = "BINANCE_API_KEY"
    private const val SECRET_NAME = "BINANCE_API_SECRET"

    fun getFactory(apiKey: String? = null, secret:String? = null): BinanceApiClientFactory {
        val finalKey = apiKey ?: Config.getProperty(KEY_NAME)
        val finalSecret = secret ?: Config.getProperty(SECRET_NAME)
        return if (apiKey == null ) BinanceApiClientFactory.newInstance() else BinanceApiClientFactory.newInstance(finalKey, finalSecret)
    }

    /**
     * get available assets
     *
     * TODO currently broken, seems to be a Binance problem
     */
    fun retrieveAssets(api: BinanceApiClientFactory): List<Asset> {
        val client = api.newRestClient()
        return client.exchangeInfo.symbols.map {
            binanceTemplate.copy(symbol = it.symbol, currencyCode = it.quoteAsset)
        }
    }



}