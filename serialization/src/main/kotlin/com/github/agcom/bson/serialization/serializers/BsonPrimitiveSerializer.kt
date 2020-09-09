package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import com.github.agcom.bson.serialization.utils.fold
import com.github.agcom.bson.serialization.utils.toBinary
import com.github.agcom.bson.serialization.utils.toRegex
import kotlinx.serialization.*
import org.bson.*
import org.bson.BsonType.*

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
                when (it.bsonType) {
                    DOUBLE -> encoder.encodeDouble(it.asDouble().value)
                    STRING -> encoder.encodeString(it.asString().value)
                    BINARY -> encoder.encodeBinary(it.asBinary().toBinary())
                    OBJECT_ID -> encoder.encodeObjectId(it.asObjectId().value)
                    BOOLEAN -> encoder.encodeBoolean(it.asBoolean().value)
                    DATE_TIME -> encoder.encodeDateTime(it.asDateTime().value)
                    NULL -> encoder.encodeNull()
                    JAVASCRIPT -> encoder.encodeJavaScript(it.asJavaScript().code)
                    INT32 -> encoder.encodeInt(it.asInt32().value)
                    INT64 -> encoder.encodeLong(it.asInt64().value)
                    DECIMAL128 -> encoder.encodeDecimal128(it.asDecimal128().value)
                    REGULAR_EXPRESSION -> encoder.encodeRegularExpression(it.asRegularExpression().toRegex())
                    else -> throw RuntimeException("Should not reach here")
                }
            }
        )
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        val value = decoder.decodeBson()
        return value.fold(
            primitive = { it }
        )
    }

}