package org.roboquant.charts

/**
 * Fragments for HtmlPage
 */
interface HtmlFragment {
    fun render(theme: String) : String
}

/**
 * Add an HTML header to the page
 */
class Header(val value: String, val level: Int = 1, val style:String="") : HtmlFragment {

    override fun render(theme: String): String {
        return """<h${level} style="$style">${value}</h${level}>"""
    }


}

/**
 * Add arbitrary HTML code to an HtmlPage
 */
class HtmlSnippet(val snippet: String) : HtmlFragment {

    override fun render(theme: String): String {
        return snippet
    }
}