package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Encoder
import org.bson.BsonValue
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId

interface BsonOutput : Encoder, CompositeEncoder {

    val bson: Bson

    fun encodeBson(element: BsonValue)

    fun encodeBinary(binary: Binary)

    fun encodeObjectId(objectId: ObjectId)

    fun encodeDateTime(time: Long)

    fun encodeJavaScript(code: String)

    fun encodeDecimal128(decimal: Decimal128)

    fun encodeRegularExpression(regex: Regex)

}