package com.github.agcom.bson.decoders

import com.github.agcom.bson.*
import com.github.agcom.bson.serializers.BsonValueSerializer
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
        bson.fromBson(BsonValueSerializer, binary) shouldBe binary
    }

    "bson boolean" {
        val bool = BsonBoolean(Random.nextBoolean())
        bson.fromBson(BsonValueSerializer, bool) shouldBe bool
    }

    "bson date time" {
        val t = BsonDateTime(Clock.systemUTC().millis())
        bson.fromBson(BsonValueSerializer, t) shouldBe t
    }

    "bson decimal 128" {
        val decimal = BsonDecimal128(Decimal128(Random.nextLong()))
        bson.fromBson(BsonValueSerializer, decimal) shouldBe decimal
    }

    "bson double" {
        val d = BsonDouble(Random.nextDouble())
        bson.fromBson(BsonValueSerializer, d) shouldBe d
    }

    "bson int 32" {
        val i = BsonInt32(Random.nextInt())
        bson.fromBson(BsonValueSerializer, i) shouldBe i
    }

    "bson int 64" {
        val l = BsonInt64(Random.nextLong())
        bson.fromBson(BsonValueSerializer, l) shouldBe l
    }

    "bson java script" {
        val js = BsonJavaScript("main() {}")
        bson.fromBson(BsonValueSerializer, js) shouldBe js
    }

    "bson null" {
        bson.fromBson(BsonValueSerializer, BsonNull.VALUE) shouldBe BsonNull.VALUE
    }

    "bson object id" {
        val id = BsonObjectId(ObjectId())
        bson.fromBson(BsonValueSerializer, id) shouldBe id
    }

    "bson regular expresion" - {

        "without options" {
            val regex = BsonRegularExpression("acme.*corp")
            bson.fromBson(BsonValueSerializer, regex) shouldBe regex
        }

        "with options" {
            val regex = BsonRegularExpression("acme.*corp", "imdxs")
            bson.fromBson(BsonValueSerializer, regex) shouldBe regex
        }

        "a hard one" {
            val regex = BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs")
            bson.fromBson(BsonValueSerializer, regex) shouldBe regex
        }
    }

    "bson string" {
        val str = BsonString("hello")
        bson.fromBson(BsonValueSerializer, str) shouldBe str
    }

})