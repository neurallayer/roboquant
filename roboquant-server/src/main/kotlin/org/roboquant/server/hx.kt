/*
 * Copyright 2020-2023 Neural Layer
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

import kotlinx.html.FORM
import kotlinx.html.HTMLTag

internal var FORM.hxPost: String
    get() = attributes["data-hx-post"] ?: ""
    set(value) {
        attributes["data-hx-post"] = value
    }

internal var HTMLTag.hxExt: String
    get() = attributes["data-hx-ext"] ?: ""
    set(value) {
        attributes["data-hx-ext"] = value
    }

internal var HTMLTag.hxGet: String
    get() = attributes["data-hx-get"] ?: ""
    set(value) {
        attributes["data-hx-get"] = value
    }

internal var HTMLTag.hxConfirm: String
    get() = attributes["data-hx-confirm"] ?: ""
    set(value) {
        attributes["data-hx-confirm"] = value
    }

internal var HTMLTag.hxTarget: String
    get() = attributes["data-hx-target"] ?: ""
    set(value) {
        attributes["data-hx-target"] = value
    }