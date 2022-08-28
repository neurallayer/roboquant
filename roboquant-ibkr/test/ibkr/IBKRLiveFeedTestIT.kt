/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.ibkr

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Timeframe
import org.roboquant.common.minutes
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IBKRLiveFeedTestIT {

    @Test
    fun ibkrFeed() {
        System.getProperty("TEST_IBKR") ?: return

        val feed = IBKRLiveFeed()
        val contract = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
        feed.subscribe(contract)

        val actions = feed.filter<PriceAction>(Timeframe.next(5.minutes))
        assertTrue(actions.isNotEmpty())
    }

}