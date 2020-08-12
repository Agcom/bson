package com.github.agcom.bson.codecs

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.serialization.modules.SerializersModule
import com.github.agcom.bson.codecs.models.*
import com.github.agcom.bson.*

class SerializationCodecRegistryTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "default" {
        val registry = SerializationCodecRegistry(bson)
        val codec = registry[Data::class.java]
        codec.shouldNotBeNull()
        (codec is SerializationCodec).shouldBeTrue()
    }

    "polymorphic" {
        val registry = SerializationCodecRegistry(
            Bson(
                BsonConfiguration.DEFAULT,
                SerializersModule {
                    polymorphic<Message> {
                        subclass(IntMessage.serializer())
                        subclass(StringMessage.serializer())
                    }
                }
            )
        )
        val codec = registry[Message::class.java]
        codec.shouldNotBeNull()
        (codec is SerializationCodec).shouldBeTrue()
    }

})
