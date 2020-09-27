package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Encoder
import org.bson.BsonDbPointer
import org.bson.BsonValue
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
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

}