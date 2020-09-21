package com.github.agcom.bson.mongodb.utils

import com.github.agcom.bson.mongodb.models.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.modules.SerializersModule

class PolymorphicSerializerTest : FreeSpec({

    val module = SerializersModule {
        polymorphic<Message> {
            subclass(IntMessage.serializer())
            subclass(StringMessage.serializer())
            subclass(DualBase.serializer())
        }
        polymorphic<Filter> {
            subclass(DualBase.serializer())
        }
    }

    val polymorphicsStructure = dumpPolymorphicsStructure(module)

    "registered as a subclass; Safe" {
        polymorphicsStructure[IntMessage::class].let {
            it.shouldNotBeNull()
            it.size shouldBe 1
            it.first() shouldBe Message::class
        }
    }

    "registered as a base class; Unsafe" {
        polymorphicsStructure[Message::class].let {
            it.shouldNotBeNull()
            it.size shouldBe 1
            it.first() shouldBe Message::class
        }
    }

    "dual base; It's safe if the right parent is chosen" {
        polymorphicsStructure[DualBase::class].let {
            it.shouldNotBeNull()
            it.size shouldBe 2
            it shouldContain Filter::class
            it shouldContain Message::class
        }
    }

    "not registered; Fails" {
        polymorphicsStructure[NoSerializableAnnotationData::class].shouldBeNull()
    }

    /* // Old getPolymorphic tests

    val module = SerializersModule {
        polymorphic<Message> {
            subclass(IntMessage.serializer())
            subclass(StringMessage.serializer())
        }
    }

    "registered as a subclass; Safe" {
        module.getPolymorphic(IntMessage::class).let {
            it.shouldNotBeNull()
            it.baseClass shouldBe Message::class
        }
    }

    "registered as a base class; Unsafe" {
        module.getPolymorphic(Message::class).let {
            it.shouldNotBeNull()
            it.baseClass shouldBe Message::class
        }
    }

    "not registered; Fails" {
        module.getPolymorphic(Filter::class).shouldBeNull()
    }
    */

})