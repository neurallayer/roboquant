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

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Interface for all types of currency converters that will convert an amount from one currency to another currency.
 * The interface allows currency conversions to take the following aspects into account (next to the two currencies):
 *
 * - the time of the conversion
 * - the amount to be converted
 *
 */
interface CurrencyConverter {

    /**
     * Convert a monetary amount from one currency to another currency at a specific moment in time.
     *
     * It depends on the implementation if all parameters are also actually used by the underlying algorithm. If a
     * conversion cannot be processed due to incorrect or missing configuration, it is expected to throw an exception.
     *
     * @param to The currency that the money needs to be converted to
     * @param amount The amount (in from currency denoted) that needs to be converted
     * @param now The time of the conversion
     * @return The converted amount
     */
    fun convert(amount: Amount, to: Currency, now: Instant): Amount

}
