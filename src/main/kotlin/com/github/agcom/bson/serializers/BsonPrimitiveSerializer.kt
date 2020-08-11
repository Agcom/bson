package com.github.agcom.bson.serializers

import com.github.agcom.bson.BsonEncodingException
import com.github.agcom.bson.decoders.BsonInput
import com.github.agcom.bson.encoders.BsonOutput
import com.github.agcom.bson.utils.toBinary
import com.github.agcom.bson.utils.toRegex
import kotlinx.serialization.*
import org.bson.BsonType.*
import org.bson.BsonValue

@Serializer(BsonValue::class)
object BsonPrimitiveSerializer : KSerializer<BsonValue> {

    // The serial name is as exact as BsonValueSerializer
    override val descriptor: SerialDescriptor = SerialDescriptor(BsonValue::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify(); encoder as BsonOutput
        when (value.bsonType) {
            DOUBLE -> encoder.encodeDouble(value.asDouble().value)
            STRING -> encoder.encodeString(value.asString().value)
            BINARY -> encoder.encodeBinary(value.asBinary().toBinary())
            OBJECT_ID -> encoder.encodeObjectId(value.asObjectId().value)
            BOOLEAN -> encoder.encodeBoolean(value.asBoolean().value)
            DATE_TIME -> encoder.encodeDateTime(value.asDateTime().value)
            NULL -> encoder.encodeNull()
            JAVASCRIPT -> encoder.encodeJavaScript(value.asJavaScript().code)
            INT32 -> encoder.encodeInt(value.asInt32().value)
            INT64 -> encoder.encodeLong(value.asInt64().value)
            DECIMAL128 -> encoder.encodeDecimal128(value.asDecimal128().value)
            REGULAR_EXPRESSION -> encoder.encodeRegularExpression(value.asRegularExpression().toRegex())
            else -> throw BsonEncodingException("Unexpected bson type '${value.bsonType}'")
        }
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        val value = decoder.decodeBson()
        return when (value.bsonType) {
            END_OF_DOCUMENT, TIMESTAMP, UNDEFINED, DB_POINTER, SYMBOL, DOCUMENT, ARRAY, JAVASCRIPT_WITH_SCOPE, null ->
                throw BsonEncodingException("Unexpected bson type '${value.bsonType}'")
            else -> value
        }
    }

}