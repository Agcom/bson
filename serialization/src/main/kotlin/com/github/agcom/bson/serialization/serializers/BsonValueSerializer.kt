package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.BsonDecodingException
import com.github.agcom.bson.serialization.BsonEncodingException
import com.github.agcom.bson.serialization.decoders.BsonInput
import com.github.agcom.bson.serialization.utils.fold
import kotlinx.serialization.*
import org.bson.BsonValue

/**
 * External serializer for [BsonValue].
 *
 * Ports to [BsonDocumentSerializer], [BsonArraySerializer] or [BsonPrimitiveSerializer] based on [BsonValue.getBsonType].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
@Serializer(BsonValue::class)
object BsonValueSerializer : KSerializer<BsonValue> {

    override val descriptor: SerialDescriptor =
        SerialDescriptor(BsonValue::class.qualifiedName!!, PolymorphicKind.SEALED)

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
            unexpected = { throw BsonDecodingException("Unexpected bson type '${it.bsonType}'") }
        )
    }

}