package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.decoders.BsonInput
import kotlinx.serialization.*
import org.bson.BsonType.*
import org.bson.BsonValue

/**
 * Ports to [BsonDocumentSerializer], [BsonArraySerializer] or [BsonPrimitiveSerializer] based on [BsonValue.getBsonType].
 */
@Serializer(BsonValue::class)
object BsonValueSerializer : KSerializer<BsonValue> {

    override val descriptor: SerialDescriptor = SerialDescriptor(BsonValue::class.qualifiedName!!, PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify()
        when (value.bsonType) {
            DOUBLE, STRING, OBJECT_ID, BOOLEAN, DATE_TIME, NULL, INT32, INT64, REGULAR_EXPRESSION, JAVASCRIPT, DECIMAL128, MIN_KEY, MAX_KEY, BINARY ->
                encoder.encode(BsonPrimitiveSerializer, value)
            DOCUMENT -> encoder.encode(BsonDocumentSerializer, value.asDocument())
            ARRAY -> encoder.encode(BsonArraySerializer, value.asArray())
            else -> throw BsonEncodingException("Unexpected bson type '${value.bsonType}'")
        }
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        val value = decoder.decodeBson()
        return when (value.bsonType) {
            END_OF_DOCUMENT, TIMESTAMP, UNDEFINED, DB_POINTER, SYMBOL, JAVASCRIPT_WITH_SCOPE, null ->
                throw BsonEncodingException("Unexpected bson type '${value.bsonType}'")
            else -> value
        }
    }

}