package com.github.agcom.bson.serialization.encoders

import com.github.agcom.bson.serialization.Bson
import com.github.agcom.bson.serialization.BsonConfiguration
import com.github.agcom.bson.serialization.models.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import org.bson.*
import org.bson.io.BasicOutputBuffer
import kotlin.random.Random

class BsonArrayTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "dump" - {

        fun BsonDocument.bytes(): ByteArray {
            val reader = BsonDocumentReader(this)
            return BasicOutputBuffer().use { out ->
                BsonBinaryWriter(out).use {
                    it.pipe(reader)
                }
                out.toByteArray()
            }
        }

        fun BsonArray.bytes(): ByteArray {
            val doc = BsonDocument()
            forEachIndexed { i, value ->
                doc[i.toString()] = value
            }
            return doc.bytes()
        }

        "simple" {
            val list = listOf("zero", "one", "two")
            bson.dump(String.serializer().list, list) shouldBe BsonArray(
                listOf(
                    BsonString(list[0]),
                    BsonString(list[1]),
                    BsonString(list[2])
                )
            ).bytes()
        }

        "list of objects" {
            val list = listOf(Country("one", "one"), Country("two", "two"))
            bson.dump(Country.serializer().list, list) shouldBe BsonArray(
                listOf(
                    BsonDocument("name", BsonString(list[0].name)).append("capital", BsonString(list[0].capital)),
                    BsonDocument("name", BsonString(list[1].name)).append("capital", BsonString(list[1].capital))
                )
            ).bytes()
        }

        "list of polymorphic objects" {
            val list = listOf(Tag("tag"), Duration(Random.nextLong()), Distance(Random.nextInt()))
            bson.dump(Filter.serializer().list, list) shouldBe BsonArray(
                listOf(
                    BsonDocument(
                        bson.configuration.classDiscriminator,
                        BsonString(Tag.serializer().descriptor.serialName)
                    ).append("tag", BsonString((list[0] as Tag).tag)),
                    BsonDocument(
                        bson.configuration.classDiscriminator,
                        BsonString(Duration.serializer().descriptor.serialName)
                    ).append("duration", BsonInt64((list[1] as Duration).duration)),
                    BsonDocument(
                        bson.configuration.classDiscriminator,
                        BsonString(Distance.serializer().descriptor.serialName)
                    ).append("distance", BsonInt32((list[2] as Distance).distance))
                )
            ).bytes()
        }

    }

})