package com.github.agcom.bson.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
open class Animal(val name: String)

@Serializable
@SerialName("cat")
data class Cat(val sound: String) : Animal("Cat")

@Serializable
@SerialName("dog")
data class Dog(val sound: String) : Animal("Dog")

val animalSerialModule = SerializersModule {
    polymorphic<Animal> {
        subclass(Cat.serializer())
        subclass(Dog.serializer())
    }
}