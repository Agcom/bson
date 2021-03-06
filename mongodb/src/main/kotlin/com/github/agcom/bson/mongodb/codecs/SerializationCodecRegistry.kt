package com.github.agcom.bson.mongodb.codecs

import com.github.agcom.bson.mongodb.utils.SubToBase
import com.github.agcom.bson.mongodb.utils.dumpPolymorphicsStructure
import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.serializerOrNull
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass

/**
 * Adapter between serialization [Bson] and [CodecRegistry].
 *
 * Extracts the requested class serializer in the following order,
 * 1. Class annotated with `@Serializable`
 * 2. Built-in type. E.g. `String`, `Long`, ...
 * 3. Contextual
 * 4. Polymorphic
 *
 * If a class is registered under multiple polymorphic serializers, the chosen serializer is unpredictable.
 *
 * Caches the found serializers.
 */
class SerializationCodecRegistry(private val bson: Bson) : CodecRegistry {

    private val cache: ConcurrentMap<KClass<*>, SerializationCodec<*>> = ConcurrentHashMap()

    @OptIn(ImplicitReflectionSerializer::class)
    override fun <T : Any> get(clazz: Class<T>): SerializationCodec<T> {
        return get(clazz.kotlin)
            ?: throw CodecConfigurationException("No serializer found for the requested class '${clazz.name}', hence no codec can be provided")
    }

    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry?): SerializationCodec<T>? = get(clazz.kotlin)

    // Has less usage compared to preceding serializer extract methods, so letting it be lazy
    private val polymorphicsStructure: SubToBase by lazy {
        dumpPolymorphicsStructure(bson.context).mapValues { (_, value) -> value.first() }
    }

    /**
     * @return null if no potential serializer can be found.
     */
    @Suppress("UNCHECKED_CAST")
    @OptIn(ImplicitReflectionSerializer::class)
    private fun <T : Any> get(clazz: KClass<T>): SerializationCodec<T>? {
        return cache.computeIfAbsent(clazz) { _ ->
            val serializer = clazz.serializerOrNull()
                ?: bson.context.getContextual(clazz)
                ?: polymorphicsStructure[clazz]?.let { base -> PolymorphicSerializer(base as KClass<T>) }
            if (serializer == null) null
            else SerializationCodec(bson = bson, serializer = serializer, clazz = clazz)
        } as SerializationCodec<T>?
    }

}