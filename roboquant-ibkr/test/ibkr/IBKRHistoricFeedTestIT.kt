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

package org.roboquant.ibkr

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IBKRHistoricFeedTestIT {

    @Test
    fun ibkrFeed() {
        System.getProperty("TEST_IBKR") ?: return

        val feed = IBKRHistoricFeed()
        val template = Asset("", AssetType.STOCK, "EUR", "AEB")
        feed.retrieve(template.copy(symbol = "ABN"), template.copy(symbol = "ASML"), template.copy(symbol = "KPN"))
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())

    }

}