package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import com.github.agcom.bson.serialization.utils.*
import kotlinx.serialization.*
import org.bson.*
import org.bson.BsonType.*
import org.bson.types.MaxKey
import org.bson.types.MinKey

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
 * - [BsonDbPointer]
 * - [BsonMaxKey]
 * - [BsonMinKey]
 *
 * Note: Doesn't support some of deprecated or internal types:
 * - [BsonJavaScriptWithScope]
 * - [BsonSymbol]
 * - [BsonTimestamp]
 * - [BsonUndefined]
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
@Serializer(BsonValue::class)
object BsonPrimitiveSerializer : KSerializer<BsonValue> {

    // The serial name is as exact as BsonValueSerializer
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonValue::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify(); encoder as BsonOutput
        value.fold(
            primitive = {
                when (it.bsonType) {
                    DOUBLE -> encoder.encode(BsonDoubleSerializer, it.asDouble())
                    STRING -> encoder.encode(BsonStringSerializer, it.asString())
                    BINARY -> encoder.encode(BsonBinarySerializer, it.asBinary())
                    OBJECT_ID -> encoder.encode(BsonObjectIdSerializer, it.asObjectId())
                    BOOLEAN -> encoder.encode(BsonBooleanSerializer, it.asBoolean())
                    DATE_TIME -> encoder.encode(BsonDateTimeSerializer, it.asDateTime())
                    NULL -> {
                        if (it.bsonType != NULL) throw BsonInvalidOperationException("Value expected to be of type $NULL is of unexpected type ${it.bsonType}") // To comply with other types exceptions
                        encoder.encode(BsonNullSerializer, it as BsonNull)
                    }
                    JAVASCRIPT -> encoder.encode(BsonJavaScriptSerializer, it.asJavaScript())
                    INT32 -> encoder.encode(BsonInt32Serializer, it.asInt32())
                    INT64 -> encoder.encode(BsonInt64Serializer, it.asInt64())
                    DECIMAL128 -> encoder.encode(BsonDecimal128Serializer, it.asDecimal128())
                    REGULAR_EXPRESSION -> encoder.encode(BsonRegularExpressionSerializer, it.asRegularExpression())
                    DB_POINTER -> encoder.encode(BsonDbPointerSerializer, it.asDBPointer())
                    JAVASCRIPT_WITH_SCOPE -> encoder.encode(
                        BsonJavaScriptWithScopeSerializer,
                        it.asJavaScriptWithScope()
                    )
                    MAX_KEY -> encoder.encode(
                        BsonMaxKeySerializer,
                        it as? BsonMaxKey
                            ?: throw BsonInvalidOperationException("Value expected to be of type $MAX_KEY is of unexpected type ${it.bsonType}")
                    )
                    MIN_KEY -> encoder.encode(
                        BsonMinKeySerializer,
                        it as? BsonMinKey
                            ?: throw BsonInvalidOperationException("Value expected to be of type $MIN_KEY is of unexpected type ${it.bsonType}")
                    )
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

/**
 * External serializer for [BsonDouble].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonDoubleSerializer : KSerializer<BsonDouble> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonDouble::class.qualifiedName!!, PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: BsonDouble) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDouble(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonDouble {
        decoder.verify(); decoder as BsonInput
        return BsonDouble(decoder.decodeDouble())
    }
}

/**
 * External serializer for [BsonString].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonStringSerializer : KSerializer<BsonString> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonString::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonString) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonString {
        decoder.verify(); decoder as BsonInput
        return BsonString(decoder.decodeString())
    }
}

/**
 * External serializer for [BsonBinary].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonBinarySerializer : KSerializer<BsonBinary> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonBinary::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonBinary) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeBinary(value.toBinary())
    }

    override fun deserialize(decoder: Decoder): BsonBinary {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeBinary().toBsonBinary()
    }
}

/**
 * External serializer for [BsonObjectId].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonObjectIdSerializer : KSerializer<BsonObjectId> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonObjectId::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonObjectId) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeObjectId(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonObjectId {
        decoder.verify(); decoder as BsonInput
        return BsonObjectId(decoder.decodeObjectId())
    }
}

/**
 * External serializer for [BsonBoolean].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonBooleanSerializer : KSerializer<BsonBoolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonBoolean::class.qualifiedName!!, PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: BsonBoolean) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeBoolean(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonBoolean {
        decoder.verify(); decoder as BsonInput
        return BsonBoolean(decoder.decodeBoolean())
    }
}

/**
 * External serializer for [BsonDateTime].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonDateTimeSerializer : KSerializer<BsonDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonDateTime::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: BsonDateTime) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDateTime(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonDateTime {
        decoder.verify(); decoder as BsonInput
        return BsonDateTime(decoder.decodeDateTime())
    }
}

/**
 * External serializer for [BsonNull].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonNullSerializer : KSerializer<BsonNull> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonNull::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonNull) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): BsonNull {
        decoder.verify(); decoder as BsonInput
        decoder.decodeNull()
        return BsonNull.VALUE
    }
}

/**
 * External serializer for [BsonJavaScript].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonJavaScriptSerializer : KSerializer<BsonJavaScript> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonJavaScript::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonJavaScript) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeJavaScript(value.code)
    }

    override fun deserialize(decoder: Decoder): BsonJavaScript {
        decoder.verify(); decoder as BsonInput
        return BsonJavaScript(decoder.decodeJavaScript())
    }
}

/**
 * External serializer for [BsonIn32].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonInt32Serializer : KSerializer<BsonInt32> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor(BsonInt32::class.qualifiedName!!, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: BsonInt32) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonInt32 {
        decoder.verify(); decoder as BsonInput
        return BsonInt32(decoder.decodeInt())
    }
}

/**
 * External serializer for [BsonInt64].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonInt64Serializer : KSerializer<BsonInt64> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonInt64::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: BsonInt64) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeLong(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonInt64 {
        decoder.verify(); decoder as BsonInput
        return BsonInt64(decoder.decodeLong())
    }
}

/**
 * External serializer for [BsonDecimal128].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonDecimal128Serializer : KSerializer<BsonDecimal128> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonDecimal128::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonDecimal128) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDecimal128(value.value)
    }

    override fun deserialize(decoder: Decoder): BsonDecimal128 {
        decoder.verify(); decoder as BsonInput
        return BsonDecimal128(decoder.decodeDecimal128())
    }
}

/**
 * External serializer for [BsonRegularExpression].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonRegularExpressionSerializer : KSerializer<BsonRegularExpression> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonRegularExpression::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonRegularExpression) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeRegularExpression(value.toPattern())
    }

    override fun deserialize(decoder: Decoder): BsonRegularExpression {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeRegularExpression().toBsonRegularExpression()
    }
}

/**
 * External serializer for [BsonNumber].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonNumberSerializer : KSerializer<BsonNumber> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor(BsonNumber::class.qualifiedName!!, PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: BsonNumber) {
        encoder.verify(); encoder as BsonOutput
        when (value.bsonType) {
            DOUBLE -> encoder.encode(BsonDoubleSerializer, value.asDouble())
            INT32 -> encoder.encode(BsonInt32Serializer, value.asInt32())
            INT64 -> encoder.encode(BsonInt64Serializer, value.asInt64())
            DECIMAL128 -> encoder.encode(BsonDecimal128Serializer, value.asDecimal128())
            else -> throw RuntimeException("Should not reach here")
        }
    }

    override fun deserialize(decoder: Decoder): BsonNumber {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeBson().asNumber()
    }
}

/**
 * External serializer for [BsonDbPointer].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonDbPointerSerializer : KSerializer<BsonDbPointer> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonDbPointer::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonDbPointer) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeDbPointer(value)
    }

    override fun deserialize(decoder: Decoder): BsonDbPointer {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeDbPointer()
    }

}

/**
 * External serializer for [BsonJavaScriptWithScope].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonJavaScriptWithScopeSerializer : KSerializer<BsonJavaScriptWithScope> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonJavaScriptWithScope::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonJavaScriptWithScope) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeJavaScriptWithScope(value)
    }

    override fun deserialize(decoder: Decoder): BsonJavaScriptWithScope {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeJavaScriptWithScope()
    }
}

/**
 * External serializer for [BsonMaxKey].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonMaxKeySerializer : KSerializer<BsonMaxKey> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonMaxKey::class.qualifiedName!!, PrimitiveKind.BYTE)

    override fun serialize(encoder: Encoder, value: BsonMaxKey) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeMaxKey(MaxKey())
    }

    override fun deserialize(decoder: Decoder): BsonMaxKey {
        decoder.verify(); decoder as BsonInput
        decoder.decodeMaxKey()
        return BsonMaxKey()
    }

}

/**
 * External serializer for [BsonMinKey].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
object BsonMinKeySerializer : KSerializer<BsonMinKey> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(BsonMinKey::class.qualifiedName!!, PrimitiveKind.BYTE)

    override fun serialize(encoder: Encoder, value: BsonMinKey) {
        encoder.verify(); encoder as BsonOutput
        encoder.encodeMinKey(MinKey())
    }

    override fun deserialize(decoder: Decoder): BsonMinKey {
        decoder.verify(); decoder as BsonInput
        decoder.decodeMinKey()
        return BsonMinKey()
    }

}