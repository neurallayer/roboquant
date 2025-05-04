/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.charts

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.icepear.echarts.components.title.Title
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Create an image from a [Chart].
 */
class ImageCreator {


    private companion object {
        init {
            System.setProperty("polyglot.engine.WarnInterpreterOnly", false.toString())
        }
    }

    private val jsFunctionString =
        """
        import * as echarts from '/js/echarts.mjs';
        function svg(jsonString, width, height, theme) {
            const chart = echarts.init(null, theme, {
              renderer: 'svg', 
              ssr: true, 
              width: width, 
              height: height
            });
            
           const option = JSON.parse(jsonString);        
           chart.setOption(option);
           return chart.renderToSVGString();
       }
       svg
       """.trimIndent()


    private val svgFunction by lazy {
        @Suppress("DEPRECATION")
        val context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup { _ -> true }
            .fileSystem(ClassLoaderFileSystem())
            .allowIO(true)
            .build()

        val src = Source.newBuilder("js", jsFunctionString, "test.mjs").build()
        val fn = context.eval(src)
        assert(fn.canExecute())
        fn
    }

    /**
     * Render the chart as an SVG image and return the result as a String.
     *
     * The first time this method is invoked, it is slower because the context with all the Echarts code will be loaded.
     * Subsequent calls will be much faster.
     */
    fun renderToSVGString(
        chart: Chart,
        width: Int = 1000,
        theme: String = "dark",
        backgroundColor: String = "black"
    ): String {
        val orig = chart.customize

        chart.customize = {
            this.orig()

            // Remove interactive functionality
            setToolbox(arrayOf())
            setDataZoom(arrayOf())
            setTooltip(arrayOf())

            this.backgroundColor = backgroundColor
            (title as? Title)?.setTop(20)
        }

        val jsonString = chart.renderJson()
        return svgFunction.execute(jsonString, width, chart.height, theme).asString()
    }

}

/**
 * Transcode an [svg] image to a PNG and return the result as a [ByteArray]
 */
fun transcodeSVG2PNG(svg: String): ByteArray {
    val svgByteArray = svg.toByteArray()
    val pngTranscoder = PNGTranscoder()
    val os = ByteArrayOutputStream()
    pngTranscoder.transcode(TranscoderInput(ByteArrayInputStream(svgByteArray)), TranscoderOutput(os))
    os.flush()
    return os.toByteArray()
}

/**
 * Transcode an SVG to PNG and write the result to the [file]
 */
fun transcodeSVG2PNG(svg: String, file: File) {
    val os = transcodeSVG2PNG(svg)
    file.writeBytes(os)
}

