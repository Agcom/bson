package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonDecodingException
import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import com.github.agcom.bson.serialization.utils.fold
import com.github.agcom.bson.serialization.utils.toBinary
import com.github.agcom.bson.serialization.utils.toRegex
import kotlinx.serialization.*
import org.bson.*

/**
 * External serializer for bson primitive types (anything other than [BsonDocument] and [BsonArray]):
 * - [BsonDouble]
 * - [BsonString]
 * - [BsonBinary]
 * - [BsonObjectId]
 * - [BsonBoolean]
 * - [BsonDateTime]
 * - [BsonNull]
 * - [BsonJavaScript]
 * - [BsonInt32]
 * - [BsonInt64]
 * - [BsonDecimal128]
 * - [BsonRegularExpression]
 *
 * Note: Doesn't support deprecated or internal types:
 * - [BsonDbPointer]
 * - [BsonJavaScriptWithScope]
 * - [BsonMaxKey]
 * - [BsonMinKey]
 * - [BsonSymbol]
 * - [BsonTimestamp]
 * - [BsonUndefined]
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
@Serializer(BsonValue::class)
object BsonPrimitiveSerializer : KSerializer<BsonValue> {

    // The serial name is as exact as BsonValueSerializer
    override val descriptor: SerialDescriptor = SerialDescriptor(BsonValue::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify(); encoder as BsonOutput
        value.fold(
            primitive = {
                when {
                    it.isDouble -> encoder.encodeDouble(it.asDouble().value)
                    it.isString -> encoder.encodeString(it.asString().value)
                    it.isBinary -> encoder.encodeBinary(it.asBinary().toBinary())
                    it.isObjectId -> encoder.encodeObjectId(it.asObjectId().value)
                    it.isBoolean -> encoder.encodeBoolean(it.asBoolean().value)
                    it.isDateTime -> encoder.encodeDateTime(it.asDateTime().value)
                    it.isNull -> encoder.encodeNull()
                    it.isJavaScript -> encoder.encodeJavaScript(it.asJavaScript().code)
                    it.isInt32 -> encoder.encodeInt(it.asInt32().value)
                    it.isInt64 -> encoder.encodeLong(it.asInt64().value)
                    it.isDecimal128 -> encoder.encodeDecimal128(it.asDecimal128().value)
                    it.isRegularExpression -> encoder.encodeRegularExpression(it.asRegularExpression().toRegex())
                    else -> throw BsonEncodingException("Unexpected bson type '${it.bsonType}'")
                }
            },
            unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        val value = decoder.decodeBson()
        return value.fold(
            primitive = { it },
            unexpected = { throw BsonDecodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

}