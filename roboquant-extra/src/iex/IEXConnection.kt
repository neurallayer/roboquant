/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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