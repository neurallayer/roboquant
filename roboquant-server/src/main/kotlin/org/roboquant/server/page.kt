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

import kotlinx.html.*

var SCRIPT.crossorigin
    get() = attributes["crossorigin"]
    set(value) {
        attributes["crossorigin"] = value!!
    }

var LINK.crossorigin
    get() = attributes["crossorigin"]
    set(value) {
        attributes["crossorigin"] = value!!
    }

var SCRIPT.referrerpolicy
    get() = attributes["referrerpolicy"]
    set(value) {
        attributes["referrerpolicy"] = value!!
    }

internal fun HTML.page(title: String, bodyFn: BODY.() -> Unit) {
    lang = "en"
    head {
        meta { charset = "utf-8" }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1, shrink-to-fit=no"
        }
        script {
            src = "https://unpkg.com/htmx.org@1.9.5"
            integrity = "sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO"
            crossorigin = "anonymous"
        }
        link {
            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
            rel = "stylesheet"
            integrity = "sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
            crossorigin = "anonymous"
        }
        script {
            src = "https://cdnjs.cloudflare.com/ajax/libs/echarts/5.4.3/echarts.min.js"
            integrity =
                "sha512-EmNxF3E6bM0Xg1zvmkeYD3HDBeGxtsG92IxFt1myNZhXdCav9MzvuH/zNMBU1DmIPN6njrhX1VTbqdJxQ2wHDg=="
            crossorigin = "anonymous"
            referrerpolicy = "no-referrer"
        }
        script { src = "/static/htmx-extensions.js" }
        title { +title }
        link {
            href = "/static/custom.css"
            rel = "stylesheet"
        }
    }
    body(classes = "container") {
        h1(classes = "my-3 text-center") { +title }
        bodyFn()
    }

}