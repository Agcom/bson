package com.github.agcom.bson.mongodb.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerialModuleCollector
import kotlin.reflect.KClass

/**
 * Iterates on all of the registered polymorphic serializers.
 *
 * Won't fail (returns a non-null [PolymorphicSerializer]) if one of the below conditions hold,
 * - A serializer for the [kClass] is registered as a subclass (safe serializer)
 * - [kClass] is registered as a base class (unsafe serializer)
 *
 * The serializer yielded from the second condition is unsafe, because there is no guarantee that all sub-classes of [kClass] are present.
 * @return A [PolymorphicSerializer] for the [kClass] or null if no polymorphic serializer related to the [kClass] is registered.
 * @param T The base class
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> SerialModule.getPolymorphic(kClass: KClass<out T>): PolymorphicSerializer<T>? {
    lateinit var baseClazz: KClass<T>
    try {
        dumpTo(object : SerialModuleCollector {
            override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {
                // No op
            }

            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>
            ) {
                if (kClass == actualClass || kClass == baseClass) {
                    baseClazz = baseClass as KClass<T>
                    throw BaseClassFound
                }
            }
        })
    } catch (ex: BaseClassFound) {
        return PolymorphicSerializer(baseClazz)
    }
    return null
}

private object BaseClassFound : RuntimeException("Stop dumping, already found the needed base class")