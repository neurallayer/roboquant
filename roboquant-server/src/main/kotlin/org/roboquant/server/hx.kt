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
@file:Suppress("unused")

package org.roboquant.server

import kotlinx.html.FORM
import kotlinx.html.HTMLTag

fun HTMLTag.hxGet(value: String) {
    attributes += "hx-get" to value
}
fun HTMLTag.hxSwap(value: String) {
    attributes += "hx-swap" to value
}
fun HTMLTag.hxTarget(value: String) {
    attributes += "hx-target" to value
}

fun FORM.hxPost(value: String) {
    attributes += "hx-post" to value
}

fun HTMLTag.hxBoost(value: Boolean) {
    attributes += "hx-boost" to value.toString()
}


fun HTMLTag.echarts() {
    attributes += "hx-ext" to "echarts"
}




fun HTMLTag.hxExt(value: Boolean) {
    attributes += "hx-ext" to value.toString()
}