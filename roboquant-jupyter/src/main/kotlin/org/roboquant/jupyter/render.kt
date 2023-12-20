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

package org.roboquant.jupyter

import org.roboquant.charts.Chart


private var counter = 0

/**
 * Generates the HTML snippet required to draw a chart. This is an HTML snippet and not a full HTML page,
 * and it is suitable to be rendered in the cell output of a Jupyter notebook.
 */
fun Chart.asHTML(theme: String): String {
    val fragment = renderJson().trimStart()
    val id = "roboquant-${counter++}"
    val convertor = if (containsJavaScript)
        "option.tooltip.formatter = new Function('p', option.tooltip.formatter);"
    else
        ""

    val themeStatement = if (theme == "auto") """
            let theme = document.body.dataset.jpThemeLight === 'false' ? 'dark' : 'light'
        """.trimIndent() else """
            let theme='${theme}'
        """.trimIndent()

    val descr = this::class.simpleName ?: "chart"

    return """ 
        <!-- roboquant $descr -->
        <div style="width:100%;height:${height}px;" class="rqcharts" id="$id">
            <script type="text/javascript">
                (function () {
                    let elem = document.currentScript.parentElement;
                    if (elem.tagName === "HEAD") {
                        elem = document.getElementById("$id");
                    }
                    const option = $fragment;$convertor
                    window.call_echarts(
                        function () {
                            $themeStatement;
                            let myChart = echarts.init(elem, theme);
                            myChart.setOption(option);
                            let resizeObserver = new ResizeObserver(() => myChart.resize());
                            resizeObserver.observe(elem);
                        }
                    );
                })()
            </script>
        </div>
        """.trimIndent()
}

/**
 * Generates a standalone HTML page for the chart. This page can be saved and, for example, viewed in a
 * standalone browser.
 */
fun Chart.asHTMLPage(theme: String): String {
    val fragment = asHTML(theme)
    val script = Chart.getScript()

    return """
        <html lang="en">
            <head>
                <title>roboquant chart</title>
                $script
                <style media='screen'>
                    html { margin: 0; padding: 0; min-height: ${height}px;}
                    body { margin: 0; padding: 10px; min-height: ${height}px;}
                </style>
            </head>
            <body>
                $fragment
            </body>
        </html>
        """.trimIndent()
}
