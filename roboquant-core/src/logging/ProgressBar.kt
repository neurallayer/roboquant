/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.logging

import org.roboquant.RunInfo
import java.time.Instant
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Display the progress of a run as a bar. Where often a progress bar works based on discrete steps, this
 * progress bar implementation works on the elapsed time. So how much time out the total time has passed, using the
 * time in the event as a reference.
 *
 * This implementation avoids unnecessary updates.
 */
internal class ProgressBar {

    private var currentPercent = -1
    private val progressChar = getProgressChar()
    private var pre: String = ""
    private var post: String = ""
    private var nextUpdate = Instant.MIN

    private var lastOutput = ""

    fun reset() {
        currentPercent = -1
        post = ""
        pre = ""
        nextUpdate = Instant.MIN
        lastOutput = ""
    }

    fun update(info: RunInfo) {

        // Avoid updating the progress meter too often
        val now = Instant.now()
        if (now < nextUpdate) return
        nextUpdate = now.plusMillis(500)

        val totalDuration = info.timeFrame.duration
        var percent = ((info.duration.seconds * 100.0 / totalDuration.seconds)).roundToInt()
        percent = min(percent, 100)

        if (percent == currentPercent) return

        currentPercent = percent

        if (post.isEmpty()) {
            post = "${info.roboquant} | run=${info.run} | phase=${info.phase} |"
            pre = info.timeFrame.toPrettyString() + " | "
        }

        draw(percent)
    }

    private fun draw(percent: Int) {
        val sb = StringBuilder(100)
        sb.append('\r').append(pre)
        sb.append(String.format("%3d", percent)).append("% |")
        val filled = percent * TOTAL_BAR_LENGTH / 100
        for (i in 0 until TOTAL_BAR_LENGTH) {
            if (i <= filled) sb.append(progressChar) else sb.append(' ')
        }

        sb.append("| ").append(post)
        if (percent == 100) sb.append("\n")
        val str = sb.toString()

        // Only update if there are some changes to the progress bar
        if (str != lastOutput) {
            print(str)
            lastOutput = str
            System.out.flush()
        }

    }

    /**
     * Signal that the current task is done, so the progress bar can show it has finished.
     */
    fun done() {
        if ((currentPercent < 100) && (currentPercent >= 0)) {
            draw(100)
            System.out.flush()
        }
    }

    companion object {

        private const val TOTAL_BAR_LENGTH = 40

        private fun getProgressChar(): Char {
            if (System.getProperty("os.name").startsWith("Win")) {
                return '='
            } else if (System.getProperty("os.name").startsWith("Linux")) {
                val lang = System.getenv("LANG")
                if (lang == null || !lang.contains("UTF-8")) {
                    return '='
                }
            }
            return '█'
        }
    }
}
