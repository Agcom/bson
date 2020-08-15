package com.github.agcom.bson.codecs

import com.github.agcom.bson.serialization.Bson
import kotlinx.serialization.KSerializer
import org.bson.BsonValue
import kotlin.reflect.KClass

class SerializationCodec<T : Any>(private val bson: Bson, private val serializer: KSerializer<T>, private val clazz: KClass<T>) : BsonValueTransformingCodec<T>() {

    override fun getEncoderClass(): Class<T> = clazz.java

    override fun toBson(value: T): BsonValue = bson.toBson(serializer, value)

    override fun fromBson(value: BsonValue): T = bson.fromBson(serializer, value)

}

inline fun <reified T : Any> SerializationCodec(bson: Bson, serializer: KSerializer<T>): SerializationCodec<T> =
    SerializationCodec(bson, serializer, T::class)