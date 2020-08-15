package com.github.agcom.bson.serialization.serializers

import com.github.agcom.bson.serialization.utils.NamedListClassDescriptor
import kotlinx.serialization.*
import kotlinx.serialization.builtins.list
import org.bson.BsonArray

@Serializer(BsonArray::class)
object BsonArraySerializer : KSerializer<BsonArray> {
    
    override val descriptor: SerialDescriptor = NamedListClassDescriptor(BsonArray::class.qualifiedName!!, BsonValueSerializer.descriptor)

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