package com.github.agcom.bson.mongodb.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class Data(val value: String?)

data class NoSerializableAnnotationData(val value: String?) {
    @Serializer(NoSerializableAnnotationData::class)
    companion object : KSerializer<NoSerializableAnnotationData>
}

enum class Field {
    HELLO, WORLD
}

@Serializable
sealed class Filter

interface Message

@Serializable
@SerialName("int")
data class IntMessage(val value: Int) : Message

@Serializable
@SerialName("string")
data class StringMessage(val value: String) : Message

object EmptyMessage : Message {

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
object DualBase : Message, Filter()