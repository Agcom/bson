package com.github.agcom.bson.mongodb.codecs

import com.github.agcom.bson.mongodb.utils.getPolymorphic
import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.serializerOrNull
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Adapter between serialization [Bson] and [CodecRegistry].
 * Extracts the requested class serializer in the following order,
 * 1. Class annotated with `@Serializable`
 * 2. Built-in type. E.g. `String`, `Long`, ...
 * 3. Contextual
 * 4. Polymorphic
 */
class SerializationCodecRegistry(private val bson: Bson) : CodecRegistry {

    private val cache: ConcurrentMap<Class<*>, SerializationCodec<*>> = ConcurrentHashMap()

    @OptIn(ImplicitReflectionSerializer::class)
    override fun <T : Any> get(clazz: Class<T>): SerializationCodec<T> {
        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(clazz, {
            val kClazz = clazz.kotlin
            SerializationCodec(
                bson = bson,
                serializer = kClazz.serializerOrNull()
                    ?: bson.context.getContextual(kClazz)
                    ?: bson.context.getPolymorphic(kClazz)
                    ?: throw CodecConfigurationException("No serializer found for the requested class '${clazz.name}', hence no codec can be provided"),
                clazz = kClazz
            )
        }) as SerializationCodec<T>
    }

    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry?): SerializationCodec<T>? {
        return try {
            get(clazz)
        } catch (ex: CodecConfigurationException) {
            null
        }
    }

}