/*
 * Copyright 2020-2026 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.tickerall

import org.roboquant.common.Config

/**
 * Resolve a TickerAll account id for the id-based integration tests. Uses a non-numeric
 * `TICKERALL_ACCOUNT_ID` if one is set; otherwise opens a session from the MetaTrader login
 * (`TICKERALL_BROKER` / `TICKERALL_SERVER` / `TICKERALL_ACCOUNT` / `TICKERALL_PASSWORD`) and reuses the id
 * it produces — so the suite can run from a broker login alone and you never have to look up the internal
 * account id (a broker account NUMBER is not it). Returns null (the caller returns early / skips) when
 * neither a usable id nor a full login is available.
 */
internal fun resolveTickerAllAccountId(key: String): String? {
    val id = Config.getProperty("TICKERALL_ACCOUNT_ID")
    if (id != null && id.isNotBlank() && !id.all { it.isDigit() }) return id
    val brokerPlatform = Config.getProperty("TICKERALL_BROKER") ?: return null
    val serverName = Config.getProperty("TICKERALL_SERVER") ?: return null
    val accountLogin = Config.getProperty("TICKERALL_ACCOUNT") ?: return null
    val pw = Config.getProperty("TICKERALL_PASSWORD") ?: return null
    return TickerAllBroker.connect {
        apiKey = key
        broker = brokerPlatform
        server = serverName
        account = accountLogin
        password = pw
    }.accountId
}
