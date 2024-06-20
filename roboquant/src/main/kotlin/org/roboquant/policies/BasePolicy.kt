/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.getPosition
import org.roboquant.common.Asset
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order

/**
 * Contains a number of utility methods that are useful when implementing a new policy.
 *
 * For example, how to deal with conflicting signals or how to handle amounts in a multi-currency environment. It
 * also contains a simple method to record metrics.
 *
 */
abstract class BasePolicy : Policy {



}
