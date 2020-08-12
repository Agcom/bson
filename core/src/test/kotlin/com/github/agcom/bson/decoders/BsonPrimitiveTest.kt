package com.github.agcom.bson.decoders

import com.github.agcom.bson.models.HttpError
import com.github.agcom.bson.serializers.NullSerializer
import com.github.agcom.bson.*
import com.github.agcom.bson.serializers.*
import com.github.agcom.bson.utils.asEmbedded
import com.github.agcom.bson.utils.toBsonBinary
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import org.bson.*
import org.bson.types.*
import java.nio.ByteBuffer
import java.time.Clock
import java.util.*
import kotlin.random.Random

class BsonPrimitiveTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "fromBson" - {

        "double" {
            val d = Random.nextDouble()
            bson.fromBson(Double.serializer(), BsonDouble(d)) shouldBe d
        }

        "string" {
            val str = UUID.randomUUID().toString()
            bson.fromBson(String.serializer(), BsonString(str)) shouldBe str
        }

        "binary" {
            val binary = Binary(Random.nextBytes(100))
            bson.fromBson(BinarySerializer, binary.toBsonBinary()) shouldBe binary
        }

        "object id" {
            val id = ObjectId()
            bson.fromBson(ObjectIdSerializer, BsonObjectId(id)) shouldBe id
        }

        "boolean" {
            val bool = Random.nextBoolean()
            bson.fromBson(Boolean.serializer(), BsonBoolean(bool)) shouldBe bool
        }

        "date time" {
            val t = Clock.systemUTC().millis()
            bson.fromBson(DateTimeSerializer, BsonDateTime(t)) shouldBe t
        }

        "null" {
            bson.fromBson(NullSerializer, BsonNull.VALUE) shouldBe null
        }

        "java script" {
            val code = "main() {}"
            bson.fromBson(JavaScriptSerializer, BsonJavaScript(code)) shouldBe code
        }

        "int 32" {
            val i = Random.nextInt()
            bson.fromBson(Int.serializer(), BsonInt32(i)) shouldBe i
        }

        "int 64" {
            val l = Random.nextLong()
            bson.fromBson(Long.serializer(), BsonInt64(l)) shouldBe l
        }

        "decimal 128" {
            val decimal = Decimal128(Random.nextLong())
            bson.fromBson(Decimal128Serializer, BsonDecimal128(decimal)) shouldBe decimal
        }

        "regular expresion" - {

            "without options" {
                val expected = Regex("acme.*corp")
                val regex = bson.fromBson(RegexSerializer, BsonRegularExpression(expected.pattern))

                regex.pattern shouldBe expected.pattern
                regex.options shouldBe expected.options
            }

            "with options" {
                val expected = Regex(
                    "acme.*corp", setOf(
                        RegexOption.IGNORE_CASE,
                        RegexOption.MULTILINE,
                        RegexOption.UNIX_LINES,
                        RegexOption.COMMENTS,
                        RegexOption.DOT_MATCHES_ALL
                    )
                )
                val regex = bson.fromBson(
                    RegexSerializer,
                    BsonRegularExpression(expected.pattern, "imdxs")
                )

                regex.pattern shouldBe expected.pattern
                regex.options shouldBe expected.options
            }

            "a hard one" {
                val expected = Regex(
                    "[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", setOf(
                        RegexOption.IGNORE_CASE,
                        RegexOption.MULTILINE,
                        RegexOption.UNIX_LINES,
                        RegexOption.COMMENTS,
                        RegexOption.DOT_MATCHES_ALL
                    )
                )
                val regex = bson.fromBson(
                    RegexSerializer,
                    BsonRegularExpression(expected.pattern, "imdxs")
                )

                regex.pattern shouldBe expected.pattern
                regex.options shouldBe expected.options
            }
        }

        "enum kind" {
            val notFound = HttpError.NOT_FOUND
            val error = HttpError.INTERNAL_SERVER_ERROR

            bson.fromBson(HttpError.serializer(), BsonString(notFound.name)) shouldBe notFound
            bson.fromBson(HttpError.serializer(), BsonString(error.name)) shouldBe error
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

        "double" {
            val d = Random.nextDouble()
            bson.load(Double.serializer(), bytes(d), type = BsonType.DOUBLE) shouldBe d
        }

        "string" {
            val str = UUID.randomUUID().toString()
            bson.load(String.serializer(), bytes(str), type = BsonType.STRING) shouldBe str
        }

        "binary" {
            val binary = Binary(Random.nextBytes(100))
            bson.load(
                BinarySerializer, byteArrayOf(
                    *bytes(binary.data.size),
                    binary.type,
                    *binary.data
                ), type = BsonType.BINARY
            ) shouldBe binary
        }

        "object id" {
            val id = ObjectId()
            bson.load(ObjectIdSerializer, id.toByteArray(), type = BsonType.OBJECT_ID) shouldBe id
        }

        "boolean" {
            val bool = Random.nextBoolean()
            bson.load(
                Boolean.serializer(), byteArrayOf(
                    if (bool) 1 else 0
                ), type = BsonType.BOOLEAN
            ) shouldBe bool
        }

        "date time" {
            val t = Clock.systemUTC().millis()
            bson.load(DateTimeSerializer, bytes(t), type = BsonType.DATE_TIME) shouldBe t
        }

        "null" {
            bson.load(NullSerializer, byteArrayOf(), type = BsonType.NULL) shouldBe null
        }

        "java script" {
            val code = "main() {}"
            bson.load(JavaScriptSerializer, bytes(code), type = BsonType.JAVASCRIPT) shouldBe code
        }

        "int 32" {
            val i = Random.nextInt()
            bson.load(Int.serializer(), bytes(i), type = BsonType.INT32) shouldBe i
        }

        "int 64" {
            val l = Random.nextLong()
            bson.load(Long.serializer(), bytes(l), type = BsonType.INT64) shouldBe l
        }

        "decimal 128" {
            val decimal = Decimal128(Random.nextLong())
            bson.load(
                Decimal128Serializer, byteArrayOf(
                    *bytes(decimal.low),
                    *bytes(decimal.high)
                ), type = BsonType.DECIMAL128
            ) shouldBe decimal
        }

        "regular expresion" - {

            "without options" {
                val regex = Regex("acme.*corp")
                bson.load(
                    RegexSerializer, byteArrayOf(
                        *bytes(regex.pattern).let { it.sliceArray(4 until it.size) }, // CString, size omitted
                        0 // Options; Nothing
                    ), type = BsonType.REGULAR_EXPRESSION
                ) shouldBe regex
            }

            "with options" {
                val regex =
                    Regex(
                        "acme.*corp", setOf(
                            RegexOption.IGNORE_CASE,
                            RegexOption.MULTILINE,
                            RegexOption.UNIX_LINES,
                            RegexOption.COMMENTS,
                            RegexOption.DOT_MATCHES_ALL
                        )
                    )
                bson.load(RegexSerializer, byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(
                        regex.options.asEmbedded().toSortedSet().joinToString("")
                    ).let { it.sliceArray(4 until it.size) }
                ), type = BsonType.REGULAR_EXPRESSION) shouldBe regex
            }

            "a hard one" {
                val regex = Regex(
                    "[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", setOf(
                        RegexOption.IGNORE_CASE,
                        RegexOption.MULTILINE,
                        RegexOption.UNIX_LINES,
                        RegexOption.COMMENTS,
                        RegexOption.DOT_MATCHES_ALL
                    )
                )
                bson.load(
                    RegexSerializer, byteArrayOf(
                        *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                        *bytes(
                            regex.options.asEmbedded().toSortedSet().joinToString("")
                        ).let { it.sliceArray(4 until it.size) }
                    ), type = BsonType.REGULAR_EXPRESSION
                ) shouldBe regex
            }
        }

        "enum kind" - {

            "test 1" {
                val notFound = HttpError.NOT_FOUND
                bson.load(HttpError.serializer(), bytes(notFound.name), type = BsonType.STRING) shouldBe notFound
            }

            "test 2" {
                val error = HttpError.INTERNAL_SERVER_ERROR
                bson.load(HttpError.serializer(), bytes(error.name), type = BsonType.STRING) shouldBe error
            }
        }

    }

})