package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.encoders.BsonOutput
import com.github.agcom.bson.serialization.utils.*
import kotlinx.serialization.*
import org.bson.BsonValue

/**
 * Serializes anything other than [org.bson.BsonDocument] and [org.bson.BsonArray].
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
            unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

}