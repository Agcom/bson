package com.github.agcom.bson.codecs.models.models

import com.github.agcom.bson.codecs.SerializationCodec
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import org.bson.*
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import com.github.agcom.bson.serialization.*

@Suppress("NAME_SHADOWING")
class SerializationCodecTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "encoding" - {

        "simple" {
            val codec =
                SerializationCodec(bson, StringMessage.serializer())
            val value = StringMessage("hello")
            val expected = BsonDocument("value", BsonString("hello"))
            BsonDocument().also {
                codec.encode(BsonDocumentWriter(it), value, EncoderContext.builder().build())
            } shouldBe expected
        }

        "polymorphic" {
            val bson = Bson(BsonConfiguration.DEFAULT, SerializersModule {
                polymorphic<Message> {
                    subclass(IntMessage.serializer())
                    subclass(StringMessage.serializer())
                    subclass(object : KSerializer<EmptyMessage> {

                        override val descriptor: SerialDescriptor = SerialDescriptor("common.models.EmptyMessage") {
                            element("value", String.serializer().descriptor)
                        }

                        override fun serialize(encoder: Encoder, value: EmptyMessage) {
                            encoder.beginStructure(descriptor).apply {
                                encodeStringElement(descriptor, 0, "empty")
                            }.endStructure(descriptor)
                        }

                        override fun deserialize(decoder: Decoder): EmptyMessage {
                            decoder.beginStructure(descriptor).apply {
                                val value = decodeStringElement(descriptor, 0)
                                require(value == "empty") { "Can't read EmptyMessage, message was '$value'" }
                            }.endStructure(descriptor)
                            return EmptyMessage
                        }

                    })
                }
            })
            val codec = SerializationCodec(
                bson,
                PolymorphicSerializer(Message::class)
            )
            val value = IntMessage(10)
            val expected = BsonDocument("value", BsonInt32(10)).append("type", BsonString(IntMessage.serializer().descriptor.serialName))
            BsonDocument().also {
                codec.encode(BsonDocumentWriter(it), value, EncoderContext.builder().build())
            } shouldBe expected
        }

        "null" {
            val codec = SerializationCodec(bson, Data.serializer())
            val expected = BsonDocument("value", BsonNull.VALUE)
            val value = Data(null)
            BsonDocument().also {
                codec.encode(BsonDocumentWriter(it), value, EncoderContext.builder().build())
            } shouldBe expected
        }

    }

    "decoding" - {

        "simple" {
            val codec =
                SerializationCodec(bson, StringMessage.serializer())
            val expected = StringMessage("hello")
            val value = BsonDocument("value", BsonString("hello"))
            codec.decode(BsonDocumentReader(value).apply { readBsonType() }, DecoderContext.builder().build()) shouldBe expected
        }

        "polymorphic" {
            val bson = Bson(BsonConfiguration.DEFAULT, SerializersModule {
                polymorphic<Message> {
                    subclass(IntMessage.serializer())
                    subclass(StringMessage.serializer())
                    subclass(object : KSerializer<EmptyMessage> {

                        override val descriptor: SerialDescriptor = SerialDescriptor("common.models.EmptyMessage") {
                            element("value", String.serializer().descriptor)
                        }

                        override fun serialize(encoder: Encoder, value: EmptyMessage) {
                            encoder.beginStructure(descriptor).apply {
                                encodeStringElement(descriptor, 0, "empty")
                            }.endStructure(descriptor)
                        }

                        override fun deserialize(decoder: Decoder): EmptyMessage {
                            decoder.beginStructure(descriptor).apply {
                                val value = decodeStringElement(descriptor, 0)
                                require(value == "empty") { "Can't read EmptyMessage, message was '$value'" }
                            }.endStructure(descriptor)
                            return EmptyMessage
                        }

                    })
                }
            })
            val codec = SerializationCodec(
                bson,
                PolymorphicSerializer(Message::class)
            )
            val expected = IntMessage(10)
            val value = BsonDocument("value", BsonInt32(10)).append("type", BsonString(IntMessage.serializer().descriptor.serialName))
            codec.decode(BsonDocumentReader(value).apply { readBsonType() }, DecoderContext.builder().build()) shouldBe expected
        }

        "null" {
            val codec = SerializationCodec(bson, Data.serializer())
            val value = BsonDocument("value", BsonNull.VALUE)
            val expected = Data(null)
            codec.decode(BsonDocumentReader(value).apply { readBsonType() }, DecoderContext.builder().build()) shouldBe expected
        }

    }

})