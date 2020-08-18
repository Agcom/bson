package com.github.agcom.bson.serialization.decoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import org.bson.BsonValue
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId

/**
 * The [Decoder] instance passed to [kotlinx.serialization.KSerializer.deserialize] when using [Bson].
 */
interface BsonInput : Decoder, CompositeDecoder {

    val bson: Bson

    fun decodeBson(): BsonValue

    /**
     * Corresponds to [org.bson.BsonBinary] type.
     */
    fun decodeBinary(): Binary

    /**
     * Corresponds to [org.bson.BsonObjectId] type.
     */
    fun decodeObjectId(): ObjectId

    /**
     * Corresponds to [org.bson.BsonDateTime] type.
     */
    fun decodeDateTime(): Long

    /**
     * Corresponds to [org.bson.BsonJavaScript] type.
     */
    fun decodeJavaScript(): String

    /**
     * Corresponds to [org.bson.BsonDecimal128] type.
     */
    fun decodeDecimal128(): Decimal128

    /**
     * Corresponds to [org.bson.BsonRegularExpression] type.
     */
    fun decodeRegularExpression(): Regex

}