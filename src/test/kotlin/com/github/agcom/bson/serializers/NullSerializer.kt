package com.github.agcom.bson.serializers

import kotlinx.serialization.*

/**
 * For test purposes
 */
@Serializer(Nothing::class)
object NullSerializer : KSerializer<Nothing?> {
    
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("null", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Nothing?) {
        encoder.encodeNull()
    }
    
    override fun deserialize(decoder: Decoder): Nothing? {
        return decoder.decodeNull()
    }
    
}