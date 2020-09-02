package com.github.agcom.bson.serialization.serializers

import kotlinx.serialization.*

/**
 * For test purposes
 */
@Deprecated("Use nullable serializers", replaceWith = ReplaceWith("UnitSerializer().nullable", "kotlinx.serialization.builtins.UnitSerializer", "kotlinx.serialization.builtins.nullable"))
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