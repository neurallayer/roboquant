package iex

import org.roboquant.common.Config
import pl.zankowski.iextrading4j.client.IEXCloudClient
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion
import pl.zankowski.iextrading4j.client.IEXTradingClient

internal object IEXConnection {

    fun getClient(
        publicKey: String? = null,
        secretKey: String? = null,
        sandbox: Boolean = true
    ): IEXCloudClient {
        val pToken = publicKey ?: Config.getProperty("IEX_PUBLIC_KEY")
        require(pToken != null)
        val sToken = secretKey ?: Config.getProperty("IEX_SECRET_KEY")

        var tokenBuilder = IEXCloudTokenBuilder().withPublishableToken(pToken)
        if (sToken != null) tokenBuilder = tokenBuilder.withSecretToken(sToken)

        val apiVersion = if (sandbox) IEXTradingApiVersion.IEX_CLOUD_V1_SANDBOX else IEXTradingApiVersion.IEX_CLOUD_V1
        return IEXTradingClient.create(apiVersion, tokenBuilder.build())
    }
}