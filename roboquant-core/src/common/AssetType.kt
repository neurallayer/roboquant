package org.roboquant.common

/**
 * Enumeration with the supported asset types.
 *
 * Please note that Crypto is an asset type of its own and not mapped to the FOREX asset type.
 *
 */
enum class AssetType {

    /**
     * Stock (or also often referred to as equity) are pieces (shares) of publicly traded companies.
     */
    STOCK,

    /**
     * Option contract
     */
    OPTION,

    /**
     * A futures contract is a standardized agreement to buy or sell the underlying commodity or other asset at a
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