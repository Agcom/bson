package com.github.agcom.bson.serializers

import com.github.agcom.bson.utils.NamedMapClassDescriptor
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.bson.BsonDocument

@Serializer(BsonDocument::class)
object BsonDocumentSerializer : KSerializer<BsonDocument> {
    
    override val descriptor: SerialDescriptor = NamedMapClassDescriptor(
        BsonDocument::class.qualifiedName!!,
        String.serializer().descriptor,
        BsonValueSerializer.descriptor
    )
    
    private val bsonDocumentMapSerializer = MapSerializer(String.serializer(), BsonValueSerializer) // Cache
    
    override fun serialize(encoder: Encoder, value: BsonDocument) {
        encoder.verify()
        encoder.encode(bsonDocumentMapSerializer, value)
    }
    
    override fun deserialize(decoder: Decoder): BsonDocument {
        decoder.verify()
        return BsonDocument().apply {
            putAll(decoder.decode(bsonDocumentMapSerializer))
        }
    }
    
}