package com.github.agcom.bson.encoders

import com.github.agcom.bson.models.HttpError
import com.github.agcom.bson.serializers.NullSerializer
import com.github.agcom.bson.Bson
import com.github.agcom.bson.BsonConfiguration
import com.github.agcom.bson.serializers.*
import com.github.agcom.bson.utils.asEmbedded
import com.github.agcom.bson.utils.toBsonBinary
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import org.bson.*
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.nio.ByteBuffer
import java.time.Clock
import java.util.*
import kotlin.random.Random

class BsonPrimitiveTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "toBson" - {

        "double" {
            val d = Random.nextDouble()
            bson.toBson(Double.serializer(), d) shouldBe BsonDouble(d)
        }

        "string" {
            val str = UUID.randomUUID().toString()
            bson.toBson(String.serializer(), str) shouldBe BsonString(str)
        }

        "binary" {
            val binary = Binary(Random.nextBytes(100))
            bson.toBson(BinarySerializer, binary) shouldBe binary.toBsonBinary()
        }

        "object id" {
            val id = ObjectId()
            bson.toBson(ObjectIdSerializer, id) shouldBe BsonObjectId(id)
        }

        "boolean" {
            val bool = Random.nextBoolean()
            bson.toBson(Boolean.serializer(), bool) shouldBe BsonBoolean(bool)
        }

        "date time" {
            val t = Clock.systemUTC().millis()
            bson.toBson(DateTimeSerializer, t) shouldBe BsonDateTime(t)
        }

        "null" {
            bson.toBson(NullSerializer, null) shouldBe BsonNull.VALUE
        }

        "java script" {
            val code = "main() {}"
            bson.toBson(JavaScriptSerializer, code) shouldBe BsonJavaScript(code)
        }

        "int 32" {
            val i = Random.nextInt()
            bson.toBson(Int.serializer(), i) shouldBe BsonInt32(i)
        }

        "int 64" {
            val l = Random.nextLong()
            bson.toBson(Long.serializer(), l) shouldBe BsonInt64(l)
        }

        "decimal 128" {
            val decimal = Decimal128(Random.nextLong())
            bson.toBson(Decimal128Serializer, decimal) shouldBe BsonDecimal128(decimal)
        }

        "regular expresion" - {

            "without options" {
                val regex = Regex("acme.*corp")
                bson.toBson(RegexSerializer, regex) shouldBe BsonRegularExpression(regex.pattern)
            }

            "with options" {
                val regex =
                    Regex(
                        "acme.*corp", setOf(
                            RegexOption.IGNORE_CASE,
                            RegexOption.MULTILINE,
                            RegexOption.LITERAL,
                            RegexOption.UNIX_LINES,
                            RegexOption.COMMENTS,
                            RegexOption.DOT_MATCHES_ALL,
                            RegexOption.CANON_EQ
                        )
                    )
                bson.toBson(RegexSerializer, regex) shouldBe BsonRegularExpression(regex.pattern, "imdxs")
            }

            "a hard one" {
                bson.toBson(
                    RegexSerializer, Regex(
                        "[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", setOf(
                            RegexOption.IGNORE_CASE,
                            RegexOption.MULTILINE,
                            RegexOption.UNIX_LINES,
                            RegexOption.COMMENTS,
                            RegexOption.DOT_MATCHES_ALL
                        )
                    )
                ) shouldBe BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs")
            }
        }

        "enum kind" {
            val notFound = HttpError.NOT_FOUND
            val error = HttpError.INTERNAL_SERVER_ERROR

            bson.toBson(HttpError.serializer(), notFound) shouldBe BsonString(notFound.name)
            bson.toBson(HttpError.serializer(), error) shouldBe BsonString(error.name)
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

        "double" {
            val d = Random.nextDouble()
            bson.dump(Double.serializer(), d) shouldBe bytes(d)
        }

        "string" {
            val str = UUID.randomUUID().toString()
            bson.dump(String.serializer(), str) shouldBe bytes(str)
        }

        "binary" {
            val binary = Binary(Random.nextBytes(100))
            bson.dump(BinarySerializer, binary) shouldBe byteArrayOf(
                *bytes(binary.data.size),
                binary.type,
                *binary.data
            )
        }

        "object id" {
            val id = ObjectId()
            bson.dump(ObjectIdSerializer, id) shouldBe id.toByteArray()
        }

        "boolean" {
            val bool = Random.nextBoolean()
            bson.dump(Boolean.serializer(), bool) shouldBe byteArrayOf(
                if (bool) 1 else 0
            )
        }

        "date time" {
            val t = Clock.systemUTC().millis()
            bson.dump(DateTimeSerializer, t) shouldBe bytes(t)
        }

        "null" {
            bson.dump(NullSerializer, null) shouldBe byteArrayOf()
        }

        "java script" {
            val code = "main() {}"
            bson.dump(JavaScriptSerializer, code) shouldBe bytes(code)
        }

        "int 32" {
            val i = Random.nextInt()
            bson.dump(Int.serializer(), i) shouldBe bytes(i)
        }

        "int 64" {
            val l = Random.nextLong()
            bson.dump(Long.serializer(), l) shouldBe bytes(l)
        }

        "decimal 128" {
            val decimal = Decimal128(Random.nextLong())
            bson.dump(Decimal128Serializer, decimal) shouldBe byteArrayOf(
                *bytes(decimal.low),
                *bytes(decimal.high)
            )
        }

        "regular expresion" - {

            "without options" {
                val regex = Regex("acme.*corp")
                bson.dump(RegexSerializer, regex) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) }, // CString, size omitted
                    0 // Options; Nothing
                )
            }

            "with options" {
                val regex =
                    Regex(
                        "acme.*corp", setOf(
                            RegexOption.IGNORE_CASE,
                            RegexOption.MULTILINE,
                            RegexOption.LITERAL,
                            RegexOption.UNIX_LINES,
                            RegexOption.COMMENTS,
                            RegexOption.DOT_MATCHES_ALL,
                            RegexOption.CANON_EQ
                        )
                    )
                bson.dump(RegexSerializer, regex) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options.asEmbedded().toSortedSet().joinToString("")).let { it.sliceArray(4 until it.size) }
                )
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
                bson.dump(
                    RegexSerializer, regex
                ) shouldBe byteArrayOf(
                    *bytes(regex.pattern).let { it.sliceArray(4 until it.size) },
                    *bytes(regex.options.asEmbedded().toSortedSet().joinToString("")).let { it.sliceArray(4 until it.size) }
                )
            }
        }

        "enum kind" {
            val notFound = HttpError.NOT_FOUND
            val error = HttpError.INTERNAL_SERVER_ERROR

            bson.dump(HttpError.serializer(), notFound) shouldBe bytes(notFound.name)
            bson.dump(HttpError.serializer(), error) shouldBe bytes(error.name)
        }

    }

})