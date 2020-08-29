package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.utils.*
import kotlinx.serialization.*
import org.bson.BsonValue

/**
 * Ports to [BsonDocumentSerializer], [BsonArraySerializer] or [BsonPrimitiveSerializer] based on [BsonValue.getBsonType].
 */
@Serializer(BsonValue::class)
object BsonValueSerializer : KSerializer<BsonValue> {

    override val descriptor: SerialDescriptor = SerialDescriptor(BsonValue::class.qualifiedName!!, PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: BsonValue) {
        encoder.verify()
        value.fold(
            primitive = { encoder.encode(BsonPrimitiveSerializer, it) },
            document = { encoder.encode(BsonDocumentSerializer, it.asDocument()) },
            array = { encoder.encode(BsonArraySerializer, it.asArray()) },
            unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

    override fun deserialize(decoder: Decoder): BsonValue {
        decoder.verify(); decoder as BsonInput
        val value = decoder.decodeBson()
        return value.fold(
            primitive = { it },
            document = { it },
            unexpected = { throw BsonEncodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

}