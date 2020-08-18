package com.github.agcom.bson.mongodb.codecs

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.PolymorphicSerializer
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlinx.serialization.serializerOrNull

/**
 * Adapter between serialization [Bson] and [CodecRegistry].
 * Extracts the requested class serializer in the following order,
 * 1. Class annotated with `@Serializable`
 * 2. Built-in type. E.g. `String`, `Long`, ...
 * 3. Contextual
 * 4. Polymorphic serializer (never fails, #26)
 */
class SerializationCodecRegistry(private val bson: Bson) : CodecRegistry {

    private val cache: ConcurrentMap<Class<*>, Codec<*>> = ConcurrentHashMap()

    @OptIn(ImplicitReflectionSerializer::class)
    override fun <T : Any> get(clazz: Class<T>): Codec<T> {
        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(clazz, {
            val kClazz = clazz.kotlin
            SerializationCodec(
                bson,
                kClazz.serializerOrNull() ?: bson.context.getContextual(kClazz) ?: PolymorphicSerializer(kClazz),
                kClazz
            )
        }) as Codec<T>
    }

    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T> = get(clazz)

}