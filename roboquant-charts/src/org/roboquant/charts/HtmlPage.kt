package org.roboquant.charts

import java.io.File

/**
 * Create an HTML page with one or more charts. If you run one or more back tests and
 * want some visualization saved at the end, this is a good option.
 */
class HtmlPage {

    private val fragments = mutableListOf<HtmlFragment>()

    /**
     * Theme to use for ECharts
     */
    var theme = ""

    /**
     * Any CSS style to include within the HTML page
     */
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
            h1, h2, h3, h4, h5, h6 {
                color: #222;
                text-align: center;
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
    fun add(fragment: HtmlFragment) {
        fragments.add(fragment)
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

        for (fragment in fragments) {
            result += fragment.render(theme)
        }

        result += """
            </body>
        </html>
        """.trimIndent()
        File(fileName).writeText(result)
    }

}