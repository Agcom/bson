package com.github.agcom.bson.mongodb.utils

import com.github.agcom.bson.mongodb.models.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule

class PolymorphicSerializerTest : FreeSpec({

    val module = SerializersModule {
        polymorphic<Message> {
            subclass(IntMessage.serializer())
            subclass(StringMessage.serializer())
        }
    }

    "registered as a subclass; Safe" {
        module.getPolymorphic(IntMessage::class).shouldNotBeNull()
    }

    "registered as a base class; Unsafe" {
        module.getPolymorphic(Message::class).shouldNotBeNull()
    }

    "not registered; Fails" {
        module.getPolymorphic(Filter::class).shouldBeNull()
    }

})