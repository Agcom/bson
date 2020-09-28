package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Encoder
import org.bson.BsonDbPointer
import org.bson.BsonJavaScriptWithScope
import org.bson.BsonValue
import org.bson.types.*
import java.util.regex.Pattern

/**
 * The [Encoder] instance which is passed to [serialize][kotlinx.serialization.KSerializer.serialize] function when using [Bson].
 */
interface BsonOutput : Encoder, CompositeEncoder {

    /**
     * Instance of the current [Bson].
     */
    val bson: Bson

    fun encodeBson(element: BsonValue)

    /**
     * Write a [bson binary][org.bson.BsonBinary] value.
     */
    fun encodeBinary(binary: Binary)

    /**
     * Write a [bson object id][org.bson.BsonObjectId] value.
     */
    fun encodeObjectId(objectId: ObjectId)

    /**
     * Write a [bson date time][org.bson.BsonDateTime] value.
     */
    fun encodeDateTime(time: Long)

    /**
     * Write a [bson java script][org.bson.BsonJavaScript] value.
     */
    fun encodeJavaScript(code: String)

    /**
     * Write a [bson decimal 128][org.bson.BsonDecimal128] value.
     */
    fun encodeDecimal128(decimal: Decimal128)

    /**
     * Write a [bson regular expression][org.bson.BsonRegularExpression] value.
     */
    fun encodeRegularExpression(pattern: Pattern)

    /**
     * Write a [bson db pointer][org.bson.BsonDbPointer] value.
     */
    fun encodeDbPointer(pointer: BsonDbPointer)

    /**
     * Write a [bson java script with scope][org.bson.BsonJavaScriptWithScope] value.
     */
    fun encodeJavaScriptWithScope(jsWithScope: BsonJavaScriptWithScope)

    /**
     * Write a [bson max key][org.bson.BsonMaxKey] value.
     */
    fun encodeMaxKey(maxKey: MaxKey = MaxKey())

    /**
     * Write a [bson min key][org.bson.BsonMinKey] value.
     */
    fun encodeMinKey(minKey: MinKey = MinKey())

}