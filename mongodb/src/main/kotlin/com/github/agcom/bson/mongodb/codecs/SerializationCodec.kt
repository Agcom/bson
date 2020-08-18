package com.github.agcom.bson.mongodb.codecs

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.KSerializer
import org.bson.BsonValue
import kotlin.reflect.KClass

/**
 * Adapter between [KSerializer] and [org.bson.codecs.Codec].
 */
class SerializationCodec<T : Any>(private val bson: Bson, private val serializer: KSerializer<T>, private val clazz: KClass<T>) : BsonValueTransformingCodec<T>() {

    companion object {
        /**
         * Kotlin friendly builder.
         */
        inline operator fun <reified T : Any> invoke(bson: Bson, serializer: KSerializer<T>): SerializationCodec<T> = SerializationCodec(bson, serializer, T::class)
    }

    override fun getEncoderClass(): Class<T> = clazz.java

    override fun toBson(value: T): BsonValue = bson.toBson(serializer, value)

    override fun fromBson(value: BsonValue): T = bson.fromBson(serializer, value)

}