package com.github.agcom.bson.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

interface Message

@Serializable
data class IntMessage(val value: Int) : Message

@Serializable
data class StringMessage(val value: String) : Message

val messageSerialModule = SerializersModule {

    polymorphic<Message> {
        subclass(StringMessage.serializer())
        subclass(IntMessage.serializer())
    }
    
}