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
 * This implementation tries hard to avoid unnecessary updates.
 *
 * @constructor Create new Progress bar
 */
internal class ProgressBar {

    private var currentPercent = -1
    private val progressChar = getProgressChar()
    private var message1: String = ""
    private var message2: String = ""
    private var nextUpdate = Instant.MIN

    private var lastOutput = ""


    fun reset() {
        currentPercent = -1
        message1 = ""
        message2 = ""
        nextUpdate = Instant.MIN
        lastOutput = ""
    }

    fun update(info: RunInfo) {

        // Avoid updating the progress meter too often
        val now = Instant.now()
        if (now < nextUpdate) return
        nextUpdate = now.plusMillis(200)

        val totalDuration = info.timeFrame.duration
        var percent = ((info.duration.seconds * 100.0 / totalDuration.seconds)).roundToInt()
        percent = min(percent, 100)

        if (percent == currentPercent) return

        currentPercent = percent

        if (message1.isEmpty()) {
            message1 = "${info.name} | run=${info.run} | phase=${info.phase} |"
            message2 = info.timeFrame.toPrettyString() + " | "
        }

        draw(percent)
    }

    private fun draw(percent: Int) {
        val sb = StringBuilder(100)
        sb.append('\r').append(message2)
        sb.append(String.format("%3d", percent)).append("% |")
        val filled = percent * TOTAL_BAR_LENGTH / 100
        for (i in 0 until TOTAL_BAR_LENGTH) {
            if (i <= filled) sb.append(progressChar) else sb.append(' ')
        }

        sb.append("| ").append(message1)
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
     *
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
