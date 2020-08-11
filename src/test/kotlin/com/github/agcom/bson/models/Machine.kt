package com.github.agcom.bson.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
abstract class Machine

@Serializable
@SerialName("car")
class Car : Machine() {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
    
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@Serializable
@SerialName("air-plane")
class AirPlane : Machine() {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
    
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

val machineSerialModule = SerializersModule {
    
    polymorphic(Machine.serializer()) {
        subclass(Car.serializer())
        subclass(AirPlane.serializer())
    }
    
}