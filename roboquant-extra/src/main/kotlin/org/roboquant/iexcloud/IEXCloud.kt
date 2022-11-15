/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.iexcloud

import org.roboquant.common.Config
import pl.zankowski.iextrading4j.client.IEXCloudClient
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion
import pl.zankowski.iextrading4j.client.IEXTradingClient

/**
 * Configuration settings for connecting to IEXCloud Cloud
 *
 * @property publicKey
 * @property secretKey
 * @property sandbox
 * @constructor Create new IEXCloud configuration
 */
data class IEXCloudConfig(
    var publicKey: String = Config.getProperty("iexcloud.public.key", ""),
    var secretKey: String = Config.getProperty("iexcloud.secret.key", ""),
    var sandbox: Boolean = true,
)

/**
 * Shared logic for IEXCloud feeds
 */
internal object IEXCloud {

    fun getClient(config: IEXCloudConfig): IEXCloudClient {
        require(config.publicKey.isNotBlank())
        var tokenBuilder = IEXCloudTokenBuilder().withPublishableToken(config.publicKey)
        if (config.secretKey.isNotBlank()) tokenBuilder = tokenBuilder.withSecretToken(config.secretKey)
        val apiVersion =
            if (config.sandbox) IEXTradingApiVersion.IEX_CLOUD_V1_SANDBOX else IEXTradingApiVersion.IEX_CLOUD_V1
        return IEXTradingClient.create(apiVersion, tokenBuilder.build())
    }

}

