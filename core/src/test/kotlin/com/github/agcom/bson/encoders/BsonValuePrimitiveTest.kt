package com.github.agcom.bson.encoders

import com.github.agcom.bson.Bson
import com.github.agcom.bson.BsonConfiguration
import com.github.agcom.bson.serializers.BsonValueSerializer
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

    "toBson" - {

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

        "bson regular expression" - {

            "without options" {
                val regex = BsonRegularExpression("acme.*corp")
                bson.toBson(BsonValueSerializer, regex) shouldBe regex
            }

            "with options" {
                val regex = BsonRegularExpression("acme.*corp", "imdxs")
                bson.toBson(BsonValueSerializer, regex) shouldBe regex
            }

            "a hard one" {
                val regex = BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs")
                bson.toBson(BsonValueSerializer, regex) shouldBe regex
            }

        }

        "bson string" {
            val str = BsonString("hello")
            bson.toBson(BsonValueSerializer, str) shouldBe str
        }

    }

    "dump" - {

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
            bson.dump(BsonValueSerializer, binary) shouldBe byteArrayOf(
                *bytes(binary.data.size),
                binary.type,
                *binary.data
            )
        }

        "bson boolean" {
            val bool = BsonBoolean(Random.nextBoolean())
            bson.dump(BsonValueSerializer, bool) shouldBe byteArrayOf(
                if (bool.value) 1 else 0
            )
        }

        "bson date-time" {
            val t = BsonDateTime(Clock.systemUTC().millis())
            bson.dump(BsonValueSerializer, t) shouldBe bytes(t.value)
        }

        "bson decimal-128" {
            val decimal = BsonDecimal128(Decimal128(Random.nextLong()))
            bson.dump(BsonValueSerializer, decimal) shouldBe byteArrayOf(
                *bytes(decimal.value.low),
                *bytes(decimal.value.high)
            )
        }

        "bson double" {
            val d = BsonDouble(Random.nextDouble())
            bson.dump(BsonValueSerializer, d) shouldBe bytes(d.value)
        }

        "bson int-32" {
            val i = BsonInt32(Random.nextInt())
            bson.dump(BsonValueSerializer, i) shouldBe bytes(i.value)
        }

        "bson int-64" {
            val l = BsonInt64(Random.nextLong())
            bson.dump(BsonValueSerializer, l) shouldBe bytes(l.value)
        }

        "bson java-script" {
            val js = BsonJavaScript("main() {}")
            bson.dump(BsonValueSerializer, js) shouldBe bytes(js.code)
        }

        "bson null" {
            bson.dump(BsonValueSerializer, BsonNull.VALUE) shouldBe byteArrayOf()
        }

        "bson object-id" {
            val id = BsonObjectId(ObjectId())
            bson.dump(BsonValueSerializer, id) shouldBe id.value.toByteArray()
        }

        "bson regular expression" - {

            "without options" {
                val regex = BsonRegularExpression("acme.*corp")
                bson.dump(BsonValueSerializer, regex) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) }, // CString, size omitted
                    0 // Options; Nothing
                )
            }

            "with options" {
                val regex = BsonRegularExpression("acme.*corp", "imdxs")
                bson.dump(BsonValueSerializer, regex) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options).let { it.sliceArray(4 until it.size) }
                )
            }

            "a hard one" {
                val regex = BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs")
                bson.dump(BsonValueSerializer, regex) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options).let { it.sliceArray(4 until it.size) }
                )
            }

        }

        "bson string" {
            val str = BsonString("hello")
            bson.dump(BsonValueSerializer, str) shouldBe bytes(str.value)
        }

    }

})