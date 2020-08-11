package com.github.agcom.bson.encoders

import com.github.agom.bson.Bson
import com.github.agom.bson.BsonConfiguration
import com.github.agom.bson.serializers.BsonValueSerializer
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.time.Clock
import kotlin.random.Random

class BsonValuePrimitiveTest : FreeSpec({
    
    val bson = Bson(BsonConfiguration.DEFAULT)
    
    "bson binary" {
        val binary = BsonBinary(Random.nextBytes(100))
        bson.toBson(BsonValueSerializer, binary) shouldBe binary
    }
    "bson boolean" {
        val bool = BsonBoolean(Random.nextBoolean())
        bson.toBson(BsonValueSerializer, bool) shouldBe bool
    }
    "bson date-time" {
        val t = BsonDateTime(Clock.systemUTC().millis())
        bson.toBson(BsonValueSerializer, t) shouldBe t
    }
    "bson decimal-128" {
        val decimal = BsonDecimal128(Decimal128(Random.nextLong()))
        bson.toBson(BsonValueSerializer, decimal) shouldBe decimal
    }
    "bson double" {
        val d = BsonDouble(Random.nextDouble())
        bson.toBson(BsonValueSerializer, d) shouldBe d
    }
    "bson int-32" {
        val i = BsonInt32(Random.nextInt())
        bson.toBson(BsonValueSerializer, i) shouldBe i

    }
    "bson int-64" {
        val l = BsonInt64(Random.nextLong())
        bson.toBson(BsonValueSerializer, l) shouldBe l
    }
    "bson java-script" {
        val js = BsonJavaScript("main() {}")
        bson.toBson(BsonValueSerializer, js) shouldBe js
    }
    "bson null" {
        bson.toBson(BsonValueSerializer, BsonNull.VALUE) shouldBe BsonNull.VALUE
    }
    "bson object-id" {
        val id = BsonObjectId(ObjectId())
        bson.toBson(BsonValueSerializer, id) shouldBe id
    }
    "bson regular expression" {
        val regex = BsonRegularExpression("foo")
        bson.toBson(BsonValueSerializer, regex) shouldBe regex
    }
    "bson string" {
        val str = BsonString("hello")
        bson.toBson(BsonValueSerializer, str) shouldBe str
    }

})