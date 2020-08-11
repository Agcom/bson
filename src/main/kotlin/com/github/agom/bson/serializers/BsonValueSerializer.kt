package com.github.agom.bson.serializers

import com.github.agom.bson.BsonEncodingException
import com.github.agom.bson.decoders.BsonInput
import kotlinx.serialization.*
import org.bson.BsonType
import org.bson.BsonValue

@Serializer(BsonValue::class)
object BsonValueSerializer : KSerializer<BsonValue> {

    override val descriptor: SerialDescriptor = SerialDescriptor("org.bson.BsonValue", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify()
        when (value.bsonType) {
            BsonType.DOUBLE, BsonType.STRING, BsonType.OBJECT_ID, BsonType.BOOLEAN, BsonType.DATE_TIME, BsonType.NULL, BsonType.INT32, BsonType.INT64, BsonType.REGULAR_EXPRESSION, BsonType.JAVASCRIPT, BsonType.DECIMAL128, BsonType.MIN_KEY, BsonType.MAX_KEY, BsonType.BINARY ->
                encoder.encode(BsonPrimitiveSerializer, value)
            BsonType.DOCUMENT -> encoder.encode(BsonDocumentSerializer, value.asDocument())
            BsonType.ARRAY -> encoder.encode(BsonArraySerializer, value.asArray())
            else -> throw BsonEncodingException("Unexpected bson type '${value.bsonType}'")
        }
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        return decoder.decodeBson()
    }

}