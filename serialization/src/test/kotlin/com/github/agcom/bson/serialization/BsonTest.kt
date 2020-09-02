package com.github.agcom.bson.serialization

import com.github.agcom.bson.serialization.models.*
import com.github.agcom.bson.serialization.serializers.*
import com.github.agcom.bson.serialization.utils.toBsonBinary
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.*
import org.bson.*
import org.bson.types.Binary
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.time.Clock
import java.util.*
import kotlin.random.Random

class BsonTest : FreeSpec({

    val bson = Bson(BsonConfiguration.DEFAULT)

    "to bson" - {

        "primitives" - {

            "bson primitive types" {

                forAll(*testBsonValuePrimitives.map { row(it) }.toTypedArray()) { bsonValue ->
                    bson.toBson(BsonValueSerializer, bsonValue) shouldBe bsonValue
                }

            }

            "platform types" - {

                "double" {
                    val d = Random.nextDouble()
                    bson.toBson(Double.serializer(), d) shouldBe BsonDouble(d)
                }

                "string" {
                    val str = UUID.randomUUID().toString()
                    bson.toBson(String.serializer(), str) shouldBe BsonString(str)
                }

                "binary" {
                    val binary =
                        Binary(Random.nextBytes(Random.nextInt(1024, 1048576 + 1))) // [1 Kilobytes, 1 Megabytes]
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
                    bson.toBson(UnitSerializer().nullable, null) shouldBe BsonNull.VALUE
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
                    val decimal = Decimal128.fromIEEE754BIDEncoding(Random.nextLong(), Random.nextLong())
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
                }

                "enum kind" {
                    val enum = HttpError.NOT_FOUND
                    bson.toBson(HttpError.serializer(), enum) shouldBe BsonString(enum.name)
                }

            }

        }

        "document" - {

            "bson document type" {
                val doc = testBsonDocument()

                bson.toBson(BsonDocumentSerializer, doc) shouldBe doc
                bson.toBson(BsonValueSerializer, doc) shouldBe doc
            }

            "platform types" - {

                "a class" {
                    val location = Location(
                        name = "name",
                        filters = setOf(
                            Location.Filter.Distance(km = 10),
                            Location.Filter.Duration(ms = 100000)
                        )
                    )

                    bson.toBson(Location.serializer(), location) shouldBe BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    }
                }

                "object" {
                    bson.toBson(UnitSerializer(), Unit) shouldBe BsonDocument()
                }

                "map" {
                    val map = mapOf("hello" to "yes", "world" to "yes")

                    bson.toBson(
                        MapSerializer(
                            String.serializer(),
                            String.serializer()
                        ), map
                    ) shouldBe BsonDocument("hello", BsonString("yes")).append("world", BsonString("yes"))
                }

            }

        }

        "array" - {

            "bson array type" {
                val array = testBsonArray()

                bson.toBson(BsonArraySerializer, array) shouldBe array
                bson.toBson(BsonValueSerializer, array) shouldBe array
            }

            "platform type" {
                val location = Location(
                    name = "name",
                    filters = setOf(
                        Location.Filter.Distance(km = 10),
                        Location.Filter.Duration(ms = 100000)
                    )
                )
                val list = listOf(location, location)

                bson.toBson(Location.serializer().list, list) shouldBe BsonArray().apply {
                    add(BsonDocument().also { doc ->
                        doc["name"] = BsonString("name")
                        doc["filters"] = BsonArray().apply {
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("distance")
                                dis["km"] = BsonInt32(10)
                            })
                            add(BsonDocument().also { dis ->
                                dis["type"] = BsonString("duration")
                                dis["ms"] = BsonInt64(100000)
                            })
                        }
                    })
                    add(get(0))
                }
            }

        }

    }

    "from bson" - {

        "primitives" - {
            TODO()
        }

        "document" - {
            TODO()
        }

        "array" - {
            TODO()
        }

    }

//    TODO("other functions")

})