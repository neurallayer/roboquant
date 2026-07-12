package org.roboquant.charts

import java.io.File

/**
 * Create an HTML page with one or more charts. If you run one or more back tests and
 * want some visualization saved at the end, this is a good option.
 */
class HtmlPage {

    private val charts = mutableListOf<Chart>()

    fun addChart(chart : Chart) {
        charts.add(chart)
    }

    fun renderFragment(chart: Chart) : String {
        val convertor = if (chart.containsJavaScript) {
            "option.tooltip.formatter = new Function('p', option.tooltip.formatter);"
        } else {
            ""
        }

        val id = "chart-${chart.hashCode()}"
        return """
            <div id="$id" class="chart" style="width: 100%;height:${chart.height}px;"></div>
            <script type="text/javascript">
                var option = ${chart.renderJson()};
                ${convertor};
                var chart = echarts.init(document.getElementById('$id'));
                chart.setOption(option);
                var resizeObserver = new ResizeObserver(() => chart.resize());
                resizeObserver.observe(elem);
            </script>
            """.trimMargin()
    }

    fun render(fileName: String) {
        var result = """
            <html>
                <head>
                    <meta charset="utf-8" />
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/6.1.0/echarts.min.js"></script>
                </head>
                <body>
        """.trimIndent()

        for (chart in charts) {
            result += renderFragment(chart)
        }

        result += """
            </body>
        </html>
        """.trimIndent()
        File(fileName).writeText(result.trimIndent())
    }

}