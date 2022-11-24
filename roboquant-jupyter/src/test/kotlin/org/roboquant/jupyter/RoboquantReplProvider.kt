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

package org.roboquant.jupyter

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.kotlinx.jupyter.ReplForJupyter
import org.jetbrains.kotlinx.jupyter.api.libraries.ExecutionHost
import org.jetbrains.kotlinx.jupyter.api.libraries.LibraryResolutionInfo
import org.jetbrains.kotlinx.jupyter.common.getHttp
import org.jetbrains.kotlinx.jupyter.common.jsonObject
import org.jetbrains.kotlinx.jupyter.defaultRepositories
import org.jetbrains.kotlinx.jupyter.defaultRuntimeProperties
import org.jetbrains.kotlinx.jupyter.libraries.AbstractLibraryResolutionInfo
import org.jetbrains.kotlinx.jupyter.libraries.ByNothingLibraryResolutionInfo
import org.jetbrains.kotlinx.jupyter.libraries.ResolutionInfoProvider
import org.jetbrains.kotlinx.jupyter.libraries.SpecificLibraryResolver
import org.jetbrains.kotlinx.jupyter.messaging.DisplayHandler
import org.jetbrains.kotlinx.jupyter.repl.creating.createRepl
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import java.io.File
import java.net.URL

internal object RoboquantReplProvider : ReplProvider {

    private class DisplayHandlerImpl : DisplayHandler {

        override fun handleDisplay(value: Any, host: ExecutionHost, id: String?) { /* NOP */
        }

        override fun handleUpdate(value: Any, host: ExecutionHost, id: String?) { /* NOP */
        }
    }

    override fun invoke(classpath: List<File>): ReplForJupyter {

        return createRepl(
            resolutionInfoProvider = resolutionInfoProvider,
            displayHandler = DisplayHandlerImpl(),
            scriptClasspath = classpath,
            homeDir = null,
            mavenRepositories = defaultRepositories,
            libraryResolver =  urlEditingResolver,
            runtimeProperties = defaultRuntimeProperties,
            scriptReceivers = emptyList(),
            isEmbedded = true,
        )
    }

    private val urlEditingResolver = SpecificLibraryResolver(AbstractLibraryResolutionInfo.ByURL::class) { info, _ ->
        val response = getHttp(info.url.toString())
        val json = response.jsonObject
        val newJson = buildJsonObject {
            for ((key, value) in json.entries) {
                // if (key == "repositories" || key == "dependencies") continue
                put(key, value)
            }
        }
        Json.encodeToString(newJson)
    }

    private val resolutionInfoProvider = object : ResolutionInfoProvider {
        override var fallback: LibraryResolutionInfo
            get() = ByNothingLibraryResolutionInfo
            set(_) {}

        override fun get(string: String): LibraryResolutionInfo {
            return AbstractLibraryResolutionInfo.ByURL(URL(string))
        }
    }

}