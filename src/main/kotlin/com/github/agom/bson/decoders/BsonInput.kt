package com.github.agom.bson.decoders

import com.github.agom.bson.Bson
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import org.bson.BsonValue
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId

interface BsonInput : Decoder, CompositeDecoder {

    val bson: Bson

    fun decodeBson(): BsonValue

    fun decodeBinary(): Binary

    fun decodeObjectId(): ObjectId

    fun decodeDateTime(): Long

    fun decodeJavaScript(): String

    fun decodeDecimal128(): Decimal128

    fun decodeRegularExpression(): Regex

}