package com.github.agcom.bson.mongodb.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerialModuleCollector
import kotlin.reflect.KClass

/**
 * Iterates on all of the registered polymorphic serializers.
 * Won't fail (returns a non-null [PolymorphicSerializer]) if one of the below conditions hold,
 * - A serializer for the [kClass] is registered as a subclass (safe serializer)
 * - [kClass] is registered as a base class (unsafe serializer)
 * The serializer yielded from the second condition is unsafe, because there is no guarantee that all sub-classes of [kClass] are present.
 * @return A [PolymorphicSerializer] for the [kClass] or null if no polymorphic serializer related to the [kClass] is registered.
 */
internal fun <T : Any> SerialModule.getPolymorphic(kClass: KClass<T>): PolymorphicSerializer<T>? {
    lateinit var baseClazz: KClass<T>
    try {
        dumpTo(object : SerialModuleCollector {
            override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {
                // No op
            }

            @Suppress("UNCHECKED_CAST")
            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>
            ) {
                if (kClass == actualClass || kClass == baseClass) {
                    baseClazz = baseClass as KClass<T>
                    throw SerializerFoundException
                }
            }
        })
    } catch (ex: SerializerFoundException) {
        return PolymorphicSerializer(baseClazz)
    }
    return null
}

private object SerializerFoundException : RuntimeException("Stop dumping, already found the needed serializer")