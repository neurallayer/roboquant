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

package org.roboquant

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.SimBroker
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.journals.Journal
import org.roboquant.strategies.Strategy
import org.roboquant.traders.FlexTrader
import org.roboquant.traders.Trader
import java.time.Instant
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * Start a run, this can be used with historical data as well as live data. At least a [Feed] and [Strategy] should be
 * provide, all other parameters have defaults and are optional.
 *
 * This is the blocking version of runAsync. If you want to have many runs in parallel, use the async version.
 */
fun run(
    feed: Feed,
    strategy: Strategy,
    journal: Journal? = null,
    trader: Trader = FlexTrader(),
    timeframe: Timeframe = Timeframe.INFINITE,
    broker: Broker = SimBroker(),
    channel: EventChannel = EventChannel(timeframe, 10),
    timeOutMillis: Long = -1,
    showProgressBar: Boolean = false
): Account = runBlocking {
    return@runBlocking runAsync(
        feed,
        strategy,
        journal,
        trader,
        timeframe,
        broker,
        channel,
        timeOutMillis,
        showProgressBar
    )
}

/**
 * Run async
 * @param feed the feed to use
 * @param strategy The strategy to use
 * @param journal the journal to use, default is null
 * @param trader the trader to default, default is FlexTrader
 * @param timeframe the timeframe to limit this run to, default is `INFINITE` meaning all events in the feed will be used.
 * @param broker the broker to use, default is [SimBroker]
 * @param channel an event channel to use, default is an event channel with capacity of 10.
 * @param showProgressBar, should a progress-bar be shown, default is false.
 */
suspend fun runAsync(
    feed: Feed,
    strategy: Strategy?,
    journal: Journal? = null,
    trader: Trader = FlexTrader(),
    timeframe: Timeframe = Timeframe.INFINITE,
    broker: Broker = SimBroker(),
    channel: EventChannel = EventChannel(timeframe, 10),
    timeOutMillis: Long = -1,
    showProgressBar: Boolean = false
): Account {
    val job = feed.playBackground(channel)
    val progressBar = if (showProgressBar) {
        val tf = if (timeframe.isFinite()) timeframe else feed.timeframe
        val pb = ProgressBar(tf)
        pb.start()
        pb
    } else null

    try {
        val thread = Thread.currentThread()
        while (true) {
            val event = channel.receive(timeOutMillis)
            progressBar?.update(event.time)
            // Sync with broker
            val account = broker.sync(event)

            // Generate signals and place orders
            val signals = strategy?.createSignals(event) ?: listOf()
            val orders = trader.createOrders(signals, event, account)
            broker.placeOrders(orders)

            journal?.track(event, account, signals, orders)
            if (thread.isInterrupted) throw InterruptedException()
        }
    } catch (_: ClosedReceiveChannelException) {
        // intentionally empty
    } finally {
        if (job.isActive) job.cancel()
        progressBar?.stop()
    }
    return broker.sync()
}

/**
 * Progress bar used during a run
 */
private class ProgressBar(val timeframe: Timeframe) {

    private var currentPercent = -1
    private val progressChar = getProgressChar()
    private var pre: String = ""
    private var post: String = ""
    private var nextUpdate = Instant.MIN
    private var lastOutput = ""

    init {
        currentPercent = 0
        post = ""
        pre = "${timeframe.toPrettyString()} | "
        nextUpdate = Instant.MIN
        lastOutput = ""
    }

    /**
     * Start a progress bar for the [run] and [timeframe]
     */
    fun start() {
        draw()
    }

    /**
     * Update the progress bar, giving the provided [time]
     */
    fun update(time: Instant) {

        // Only if percentage changes we are going to refresh
        val totalDuration = timeframe.duration
        val currentDuration = Timeframe(timeframe.start, time).duration
        var percent = (currentDuration.seconds * 100.0 / totalDuration.seconds).roundToInt()
        percent = min(percent, 100)
        if (percent == currentPercent) return

        // Avoid updating the progress meter too often
        val now = Instant.now()
        if (now < nextUpdate) return
        nextUpdate = now.plusMillis(500)
        currentPercent = percent
        draw()
    }

    private fun draw() {
        val sb = StringBuilder(100)
        sb.append('\r').append(pre)
        sb.append(String.format(Locale.ENGLISH, "%3d", currentPercent)).append("% |")
        val filled = currentPercent * TOTAL_BAR_LENGTH / 100
        repeat(TOTAL_BAR_LENGTH) {
            if (it <= filled) sb.append(progressChar) else sb.append(' ')
        }

        sb.append(post)
        if (currentPercent == 100) sb.append("\n")
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
    fun stop() {
        if (currentPercent < 100) {
            currentPercent = 100
            draw()
            System.out.flush()
        }
    }

    private companion object {

        private const val TOTAL_BAR_LENGTH = 36

        private fun getProgressChar(): Char {
            return if (System.getProperty("os.name").startsWith("Win")) '=' else '█'
        }
    }
}
