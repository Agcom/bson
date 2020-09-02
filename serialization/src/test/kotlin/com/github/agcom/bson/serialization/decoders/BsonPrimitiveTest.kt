package com.github.agcom.bson.serialization.decoders

import com.github.agcom.bson.serialization.Bson
import com.github.agcom.bson.serialization.BsonConfiguration
import com.github.agcom.bson.serialization.models.HttpError
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.utils.toBsonBinary
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import org.bson.*
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
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

            bson.fromBson(HttpError.serializer(), BsonString(notFound.name)) shouldBe notFound
        }

    }

})