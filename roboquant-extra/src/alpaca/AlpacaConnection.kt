package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.asset.enums.AssetStatus
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.Exchange
import java.util.*
import java.util.logging.Logger

typealias AccountType = EndpointAPIType
typealias DataType = DataAPIType

/**
 * Connect to Alpaca API, logic shared between the Alpaca Feeds and Alpaca Broker
 */
internal object AlpacaConnection {

    private val logger: Logger = Logger.getLogger("AlpacaConnection")

    fun getAPI(
        apiKey: String? = null,
        apiSecret: String? = null,
        accountType: AccountType = AccountType.PAPER,
        dataType: DataType = DataType.IEX
    ): AlpacaAPI {
        val finalKey = apiKey ?: Config.getProperty("APCA_API_KEY_ID")
        val finalSecret = apiSecret ?: Config.getProperty("APCA_API_SECRET_KEY")
        require(finalKey != null) { "No public key provided or set as environment variable APCA_API_KEY_ID" }
        require(finalSecret != null) { "No secret key provided or set as environment variable APCA_API_SECRET_KEY" }
        return AlpacaAPI(finalKey, finalSecret, accountType, dataType)
    }

    /**
     * Get the available assets
     */
    fun getAvailableAssets(api: AlpacaAPI): SortedSet<Asset> {
        val availableAssets = api.assets().get(AssetStatus.ACTIVE, "us_equity")
        val exchangeCodes = Exchange.exchanges.map { e -> e.exchangeCode }
        val result = mutableListOf<Asset>()
        availableAssets.forEach {
            if (it.exchange !in exchangeCodes) logger.warning("Exchange ${it.exchange} not known")
            result.add(Asset(it.symbol, AssetType.STOCK, "USD", it.exchange, id = it.id))
        }
        return result.toSortedSet()
    }

}