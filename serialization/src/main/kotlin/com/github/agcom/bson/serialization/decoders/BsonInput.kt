package com.github.agcom.bson.serialization.decoders

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import org.bson.*
import org.bson.types.*
import java.util.regex.Pattern

/**
 * The [Decoder] instance which is passed to the [`deserialize`][kotlinx.serialization.KSerializer.deserialize] function when using [Bson].
 */
interface BsonInput : Decoder, CompositeDecoder {

    /**
     * Instance of the current [Bson].
     */
    val bson: Bson

    /**
     * Read a bson value.
     */
    fun decodeBson(): BsonValue

    /**
     * Read a [bson binary][org.bson.BsonBinary] value.
     */
    fun decodeBinary(): Binary

    /**
     * Read a [bson object id][org.bson.BsonObjectId] value.
     */
    fun decodeObjectId(): ObjectId

    /**
     * Read a [bson date time][org.bson.BsonDateTime] value.
     */
    fun decodeDateTime(): Long

    /**
     * Read a [bson java script][org.bson.BsonJavaScript] value.
     */
    fun decodeJavaScript(): String

    /**
     * Read a [bson decimal 128][org.bson.BsonDecimal128] value.
     */
    fun decodeDecimal128(): Decimal128

    /**
     * Read a [bson regular expression][org.bson.BsonRegularExpression] value.
     */
    fun decodeRegularExpression(): Pattern

    /**
     * Read a [bson db pointer][org.bson.BsonDbPointer] value.
     */
    fun decodeDbPointer(): BsonDbPointer

    /**
     * Read a [bson java script with scope][org.bson.BsonJavaScriptWithScope] value.
     */
    fun decodeJavaScriptWithScope(): BsonJavaScriptWithScope

    /**
     * Read a [bson max key][org.bson.BsonMaxKey] value.
     */
    fun decodeMaxKey(): MaxKey

    /**
     * Read a [bson min key][org.bson.BsonMinKey] value.
     */
    fun decodeMinKey(): MinKey

    /**
     * Read a [bson symbol][org.bson.BsonSymbol] value.
     */
    fun decodeSymbol(): String

    /**
     * Read a [bson undefined][org.bson.BsonUndefined] value.
     */
    fun decodeUndefined(): BsonUndefined

    /**
     * Read a [bson timestamp][org.bson.BsonTimestamp] value.
     */
    fun decodeTimestamp(): BsonTimestamp

}