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

package org.roboquant.common

/**
 * Enumeration with the supported asset types.
 *
 * Please note that Crypto is an asset type of its own and not mapped to the FOREX asset type.
 *
 */
enum class AssetType {

    /**
     * Stock (or also often referred to as equity) are shares of traded companies.
     */
    STOCK,

    /**
     * Option contract
     */
    OPTION,

    /**
     * A futures contract is an agreement to buy or sell the underlying commodity or other asset at a
     * specific price at a future date.
     */
    FUTURES,

    /**
     * FX pairs
     */
    FOREX,

    /**
     * Cryptocurrency pair
     */
    CRYPTO,

    /**
     * Bonds
     */
    BOND,

    /**
     * Contract For Difference, is a contract between a buyer and a seller that stipulates that the buyer must pay
     * the seller the difference between the current value of an asset and its value at contract time.
     */
    CFD,

    /**
     * Standard Warrants. They give the holder the right, but not the obligation, to buy common shares of stock
     * directly from the company at a fixed price for a pre-defined time period.
     */
    WARRANT,

    /**
     * Indexes
     */
    INDEXES,

    /**
     * Mutual Funds
     */
    MUTUAL_FUND,

    /**
     * Commodities, they are basic goods used as inputs in the economy. Some commodities, such as precious metals,
     * are used as a store of value and a hedge against inflation.
     */
    COMMODITY
}