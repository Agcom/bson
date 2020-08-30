package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.utils.NamedListClassDescriptor
import kotlinx.serialization.*
import kotlinx.serialization.builtins.list
import org.bson.BsonArray

/**
 * External serializer for [BsonArray].
 *
 * Can only be used with [Bson][com.github.agcom.bson.serialization.Bson] format.
 */
@Serializer(BsonArray::class)
object BsonArraySerializer : KSerializer<BsonArray> {

    override val descriptor: SerialDescriptor =
        NamedListClassDescriptor(BsonArray::class.qualifiedName!!, BsonValueSerializer.descriptor)

    private val bsonValueListSerializer = BsonValueSerializer.list // Cache

    override fun serialize(encoder: Encoder, value: BsonArray) {
        encoder.verify()
        encoder.encode(bsonValueListSerializer, value)
    }

    override fun deserialize(decoder: Decoder): BsonArray {
        decoder.verify()
        return BsonArray(decoder.decode(bsonValueListSerializer))
    }

}