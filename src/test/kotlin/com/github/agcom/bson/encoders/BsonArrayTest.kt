package com.github.agcom.bson.encoders

import com.github.agcom.bson.models.*
import com.github.agom.bson.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import org.bson.*
import kotlin.random.Random

class BsonArrayTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

     "simple" {
        val list = listOf("zero", "one", "two")
         bson.toBson(String.serializer().list, list) shouldBe BsonArray(listOf(BsonString(list[0]), BsonString(list[1]), BsonString(list[2])))
    }

     "list of objects" {
        val list = listOf(Country("one", "one"), Country("two", "two"))
        bson.toBson(Country.serializer().list, list) shouldBe BsonArray(listOf(
            BsonDocument("name", BsonString(list[0].name)).append("capital", BsonString(list[0].capital)),
            BsonDocument("name", BsonString(list[1].name)).append("capital", BsonString(list[1].capital))
        ))

    }

     "list of polymorphic objects" {
        val list = listOf(Tag("tag"), Duration(Random.nextLong()), Distance(Random.nextInt()))
         bson.toBson(Filter.serializer().list, list) shouldBe BsonArray(listOf(
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
        ))
    }
    
})