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
        binary shouldBe bson.toBson(BsonValueSerializer, binary)
    }
    "bson boolean" {
        val bool = BsonBoolean(Random.nextBoolean())
        bool shouldBe bson.toBson(BsonValueSerializer, bool)
    }
    "bson date-time" {
        val t = BsonDateTime(Clock.systemUTC().millis())
        t shouldBe bson.toBson(BsonValueSerializer, t)
    }
    "bson decimal-128" {
        val decimal = BsonDecimal128(Decimal128(Random.nextLong()))
        decimal shouldBe bson.toBson(BsonValueSerializer, decimal)
    }
    "bson double" {
        val d = BsonDouble(Random.nextDouble())
        d shouldBe bson.toBson(BsonValueSerializer, d)
    }
    "bson int-32" {
        val i = BsonInt32(Random.nextInt())
        i shouldBe  bson.toBson(BsonValueSerializer, i)

    }
    "bson int-64" {
        val l = BsonInt64(Random.nextLong())
        l shouldBe bson.toBson(BsonValueSerializer, l)
    }
    "bson java-script" {
        val js = BsonJavaScript("main() {}")
        js shouldBe bson.toBson(BsonValueSerializer, js)
    }
    "bson null" {
        BsonNull.VALUE shouldBe bson.toBson(BsonValueSerializer, BsonNull.VALUE)
    }
    "bson object-id" {
        val id = BsonObjectId(ObjectId())
        id shouldBe bson.toBson(BsonValueSerializer, id)
    }
    "bson regular expression" {
        val regex = BsonRegularExpression("foo")
        regex shouldBe bson.toBson(BsonValueSerializer, regex)
    }
    "bson string" {
        val str = BsonString("hello")
        str shouldBe bson.toBson(BsonValueSerializer, str)
    }

})