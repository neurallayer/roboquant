/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.server

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.roboquant.common.Logging
import org.roboquant.server.routes.chart
import org.roboquant.server.routes.details
import org.roboquant.server.routes.overview
import java.security.MessageDigest


internal val logger = Logging.getLogger(WebServer::class)

private fun getMd5Digest(str: String): ByteArray =
    MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8))


private fun Route.addRoutes() {
    overview()
    details()
    chart()
    staticResources("/static", null)
}

internal fun Application.setup() {

    logger.info { "digest auth disabled" }

    routing {
        addRoutes()
    }
}

internal fun Application.secureSetup(username: String, password: String) {

    logger.info { "digest auth enabled" }

    data class CustomPrincipal(val userName: String, val realm: String) : Principal

    val digest = getMd5Digest("$username:roboquant:$password")

    install(Authentication) {
        digest("auth-digest") {
            realm = "roboquant"
            digestProvider { userName, realm ->
                if (username == userName && realm == "roboquant") digest else null
            }
            validate { credentials ->
                if (credentials.userName.isNotEmpty()) {
                    CustomPrincipal(credentials.userName, credentials.realm)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authenticate("auth-digest") {
            addRoutes()
        }
    }

}
