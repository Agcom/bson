package com.github.agcom.bson.encoders

import com.github.agcom.bson.models.HttpError
import com.github.agcom.bson.serializers.NullSerializer
import com.github.agom.bson.Bson
import com.github.agom.bson.BsonConfiguration
import com.github.agom.bson.serializers.*
import com.github.agom.bson.utils.toBsonBinary
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
    "regular expresion" {
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
    "enum kind" {
        val notFound = HttpError.NOT_FOUND
        val error = HttpError.INTERNAL_SERVER_ERROR

        bson.toBson(HttpError.serializer(), notFound) shouldBe BsonString(notFound.name)
        bson.toBson(HttpError.serializer(), error) shouldBe BsonString(error.name)
    }

})