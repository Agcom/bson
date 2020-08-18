package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Encoder
import org.bson.BsonValue
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId

/**
 * The [Encoder] instance passed to [kotlinx.serialization.KSerializer.serialize] when using [Bson].
 */
interface BsonOutput : Encoder, CompositeEncoder {

    val bson: Bson

    fun encodeBson(element: BsonValue)

    /**
     * Corresponds to [org.bson.BsonBinary] type.
     */
    fun encodeBinary(binary: Binary)

    /**
     * Corresponds to [org.bson.BsonObjectId] type.
     */
    fun encodeObjectId(objectId: ObjectId)

    /**
     * Corresponds to [org.bson.BsonDateTime] type.
     */
    fun encodeDateTime(time: Long)

    /**
     * Corresponds to [org.bson.BsonJavaScript] type.
     */
    fun encodeJavaScript(code: String)

    /**
     * Corresponds to [org.bson.BsonDecimal128] type.
     */
    fun encodeDecimal128(decimal: Decimal128)

    /**
     * Corresponds to [org.bson.BsonRegularExpression] type.
     */
    fun encodeRegularExpression(regex: Regex)

}