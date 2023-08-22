/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.questdb

import io.questdb.cairo.CairoEngine
import io.questdb.cairo.TableWriter
import io.questdb.cairo.security.AllowAllSecurityContext
import io.questdb.cairo.sql.RecordCursor
import io.questdb.griffin.SqlExecutionContextImpl
import java.time.Instant


internal inline fun CairoEngine.query(query: String, block: RecordCursor.() -> Unit) {
    SqlExecutionContextImpl(this, 1).use { ctx ->
        sqlCompiler.use {
            val fact = it.compile(query, ctx).recordCursorFactory
            fact.use {
                fact.getCursor(ctx).use { cursor ->
                    cursor.block()
                }
            }
        }
    }
}


internal fun CairoEngine.insert(tableName: String, block: TableWriter.() -> Unit) {
    SqlExecutionContextImpl(this, 1).use { ctx ->
        getWriter(ctx.getTableToken(tableName), tableName).use {
            it.block()
            it.commit()
        }
    }
}

internal fun CairoEngine.tables(): Set<String> {
    val result = mutableSetOf<String>()
    query("select name from tables()") {
        while (hasNext()) {
            result.add(record.getStr(0).toString())
        }
    }
    return result
}

internal fun CairoEngine.tableColumns(tableName: String): Set<String> {
    val result = mutableSetOf<String>()
    query("select column from table_columns('$tableName')") {
        while (hasNext()) {
            result.add(record.getStr(0).toString())
        }
    }
    return result
}


fun CairoEngine.update(query: String) {
    SqlExecutionContextImpl(this, 1).with(AllowAllSecurityContext.INSTANCE, null, null).use { ctx ->
        sqlCompiler.use {
            it.compile(query, ctx)
        }
    }
}


val Instant.epochMicro
    get() : Long = epochSecond * 1_000_000L + nano / 1_000L


internal fun ofEpochMicro(epochMicro: Long) =
    Instant.ofEpochMilli(epochMicro / 1_000).plusNanos(epochMicro % 1_000 * 1_000)
