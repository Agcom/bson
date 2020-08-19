package com.github.agcom.bson.mongodb.codecs

import com.github.agcom.bson.mongodb.models.*
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.modules.SerializersModule
import com.github.agcom.bson.serialization.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.modules.contextual
import org.bson.*
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.types.ObjectId

class SerializationCodecRegistryTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT, SerializersModule {
        contextual(NoSerializableAnnotationData.Companion)
        polymorphic<Message> {
            subclass(IntMessage.serializer())
            subclass(StringMessage.serializer())
        }
    })
    val registry = SerializationCodecRegistry(bson)

    "class annotated with @Serializable" {
        registry[Filter::class.java].encoderClass shouldBe Filter::class.java
    }

    "built-in type" {
        registry[String::class.java].encoderClass shouldBe String::class.java
    }

    "contextual" {
        registry[NoSerializableAnnotationData::class.java].encoderClass shouldBe NoSerializableAnnotationData::class.java
    }

    "polymorphic" - {

        "base class" {
            registry[Message::class.java].encoderClass shouldBe Message::class.java
        }

        "sub class" {
            registry[IntMessage::class.java].encoderClass shouldBe IntMessage::class.java
        }

    }

    "fail; No serializer" {
        shouldThrow<CodecConfigurationException> {
            registry[Field::class.java]
        }
    }

    "bson types" - {

        "BsonValue" {
            registry[BsonValue::class.java].encoderClass shouldBe BsonValue::class.java
        }

        "BsonDocument" {
            registry[BsonDocument::class.java].encoderClass shouldBe BsonDocument::class.java
        }

        "BsonArray" {
            registry[BsonArray::class.java].encoderClass shouldBe BsonArray::class.java
        }

        "BsonObjectId" {
            registry[BsonObjectId::class.java].encoderClass shouldBe BsonObjectId::class.java
        }

        "ObjectId" {
            registry[ObjectId::class.java].encoderClass shouldBe ObjectId::class.java
        }

    }

    // encode/decode tests; Counts as black-box in this context.
    "black box tests" - {

        "BsonDocument" - {
            val codec = registry[BsonDocument::class.java]

            "encode" {
                val expected = BsonDocument("hello", BsonString("world"))
                val doc = BsonDocument()
                codec.encode(BsonDocumentWriter(doc), expected, EncoderContext.builder().build())
                doc shouldBe expected
            }

            "decode" {
                val expected = BsonDocument("hello", BsonString("world"))
                val doc = codec.decode(BsonDocumentReader(expected).apply { readBsonType() }, DecoderContext.builder().build())
                doc shouldBe expected
            }

        }

        "a model" - {
            val codec = registry[Message::class.java]

            "encode" {
                val msg = StringMessage("hello")
                val expected = BsonDocument("value", BsonString(msg.value)).append(bson.configuration.classDiscriminator, BsonString(StringMessage.serializer().descriptor.serialName))
                val written = BsonDocument()
                codec.encode(BsonDocumentWriter(written), msg, EncoderContext.builder().build())
                written shouldBe expected
            }

            "decode" {
                val expected = StringMessage("hello")
                val doc = BsonDocument("value", BsonString(expected.value)).append(bson.configuration.classDiscriminator, BsonString(StringMessage.serializer().descriptor.serialName))
                val msg = codec.decode(BsonDocumentReader(doc).apply { readBsonType() }, DecoderContext.builder().build())
                msg shouldBe expected
            }

        }

    }

})