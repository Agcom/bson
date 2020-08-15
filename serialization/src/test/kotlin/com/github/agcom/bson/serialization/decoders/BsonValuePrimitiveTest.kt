package com.github.agcom.bson.serialization.decoders

import com.github.agcom.bson.serialization.*
import com.github.agcom.bson.serialization.serializers.BsonValueSerializer
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.nio.ByteBuffer
import java.time.Clock
import kotlin.random.Random

class BsonValuePrimitiveTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "fromBson" - {

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

    }

    "load (custom function)" - {

        fun bytes(value: Int): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value).array().reversedArray()
        fun bytes(value: String): ByteArray = byteArrayOf(
            *ByteBuffer.allocate(4).putInt(value.length + 1).array().reversedArray(),
            *value.toByteArray(),
            0
        )

        fun bytes(value: Long): ByteArray = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array().reversedArray()
        fun bytes(value: Double): ByteArray = ByteBuffer.allocate(8).putDouble(value).array().reversedArray()

        "bson binary" {
            val binary = BsonBinary(Random.nextBytes(100))
            bson.load(
                BsonValueSerializer, byteArrayOf(
                    *bytes(binary.data.size),
                    binary.type,
                    *binary.data
                ), type = BsonType.BINARY
            ) shouldBe binary
        }

        "bson boolean" {
            val bool = BsonBoolean(Random.nextBoolean())
            bson.load(
                BsonValueSerializer, byteArrayOf(
                    if (bool.value) 1 else 0
                ), type = BsonType.BOOLEAN
            ) shouldBe bool
        }

        "bson date-time" {
            val t = BsonDateTime(Clock.systemUTC().millis())
            bson.load(BsonValueSerializer, bytes(t.value), type = BsonType.DATE_TIME) shouldBe t
        }

        "bson decimal-128" {
            val decimal = BsonDecimal128(Decimal128(Random.nextLong()))
            bson.load(
                BsonValueSerializer, byteArrayOf(
                    *bytes(decimal.value.low),
                    *bytes(decimal.value.high)
                ), type = BsonType.DECIMAL128
            ) shouldBe decimal
        }

        "bson double" {
            val d = BsonDouble(Random.nextDouble())
            bson.load(BsonValueSerializer, bytes(d.value), type = BsonType.DOUBLE) shouldBe d
        }

        "bson int-32" {
            val i = BsonInt32(Random.nextInt())
            bson.load(BsonValueSerializer, bytes(i.value), type = BsonType.INT32) shouldBe i
        }

        "bson int-64" {
            val l = BsonInt64(Random.nextLong())
            bson.load(BsonValueSerializer, bytes(l.value), type = BsonType.INT64) shouldBe l
        }

        "bson java-script" {
            val js = BsonJavaScript("main() {}")
            bson.load(BsonValueSerializer, bytes(js.code), type = BsonType.JAVASCRIPT) shouldBe js
        }

        "bson null" {
            bson.load(BsonValueSerializer, byteArrayOf(), type = BsonType.NULL) shouldBe BsonNull.VALUE
        }

        "bson object-id" {
            val id = BsonObjectId(ObjectId())
            bson.load(BsonValueSerializer, id.value.toByteArray(), type = BsonType.OBJECT_ID) shouldBe id
        }

        "bson regular expression" - {

            "without options" {
                val regex = BsonRegularExpression("acme.*corp")
                bson.load(
                    BsonValueSerializer, byteArrayOf(
                        *bytes(regex.pattern).let { it.sliceArray(4 until it.size) }, // CString, size omitted
                        0 // Options; Nothing
                    ), type = BsonType.REGULAR_EXPRESSION
                ) shouldBe regex
            }

            "with options" {
                val regex = BsonRegularExpression("acme.*corp", "imdxs")
                bson.load(BsonValueSerializer, byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options).let { it.sliceArray(4 until it.size) }
                ), type = BsonType.REGULAR_EXPRESSION) shouldBe regex
            }

            "a hard one" {
                val regex = BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs")
                bson.load(BsonValueSerializer, byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options).let { it.sliceArray(4 until it.size) }
                ), type = BsonType.REGULAR_EXPRESSION) shouldBe regex
            }

        }

        "bson string" {
            val str = BsonString("hello")
            bson.load(BsonValueSerializer, bytes(str.value), type = BsonType.STRING) shouldBe str
        }

    }

})