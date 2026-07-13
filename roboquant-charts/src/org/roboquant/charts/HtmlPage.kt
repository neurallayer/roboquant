package org.roboquant.charts

import java.io.File

/**
 * Create an HTML page with one or more charts. If you run one or more back tests and
 * want some visualization saved at the end, this is a good option.
 */
class HtmlPage {

    private val charts = mutableListOf<Chart>()

    var theme = ""
    var style = ""

    constructor(darkTheme: Boolean = false) {
        if (darkTheme) {
            setDarkTheme()
        } else {
            setLightTheme()
        }
    }

    /**
     * Default preset light theme
     */
    fun setLightTheme() {
        theme = "light"
        style = """
            body {
                background-color: #eee;
            }
            .chart {
                background-color: #fff;
                margin: 30px 10px;
            }
        """.trimIndent()
    }

    /**
     * Preset dark theme
     */
    fun setDarkTheme() {
        theme = "dark"
        style = """
           body {
                background-color: #555;
            }
            .chart {
                background-color: black;
                margin: 30px 10px;
            }
        """.trimIndent()
    }

    /**
     * Add a chart to this page.
     */
    fun addChart(chart: Chart) {
        charts.add(chart)
    }

    private fun renderFragment(chart: Chart): String {
        val convertor = if (chart.containsJavaScript) {
            "option.tooltip.formatter = new Function('p', option.tooltip.formatter);"
        } else {
            ""
        }

        val id = "chart-${chart.hashCode()}"
        return """
            <div id="$id" class="chart" style="height:${chart.height}px;"></div>
            <script type="text/javascript">
            {
                let option = ${chart.renderJson()};
                ${convertor};
                let elem = document.getElementById('$id')
                let chart = echarts.init(elem, "$theme");
                chart.setOption(option);
                let resizeObserver = new ResizeObserver(() => chart.resize());
                resizeObserver.observe(elem);
            }
            </script>
            """.trimMargin()
    }

    /**
     * Render all the charts into a single HTML file. The resulting file is self-contained
     * except for the ECharts JavaScript library that is referenced from a CDN.
     */
    fun render(fileName: String) {
        var result = """
            <html>
                <head>
                    <meta charset="utf-8" />
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/6.1.0/echarts.min.js"></script>
                </head>
                <style>
                $style
                </style>
                <body>
        """.trimIndent()

        for (chart in charts) {
            result += renderFragment(chart)
        }

        result += """
            </body>
        </html>
        """.trimIndent()
        File(fileName).writeText(result)
    }

}