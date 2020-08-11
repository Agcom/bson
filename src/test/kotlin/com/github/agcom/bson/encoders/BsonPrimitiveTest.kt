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
        BsonDouble(d) shouldBe bson.toBson(Double.serializer(), d)
    }
    "string" {
        val str = UUID.randomUUID().toString()
        BsonString(str) shouldBe bson.toBson(String.serializer(), str)
    }
    "binary" {
        val binary = Binary(Random.nextBytes(100))
        binary.toBsonBinary() shouldBe bson.toBson(BinarySerializer, binary)
    }
    "object id" {
        val id = ObjectId()
        BsonObjectId(id) shouldBe bson.toBson(ObjectIdSerializer, id)
    }
    "boolean" {
        val bool = Random.nextBoolean()
        BsonBoolean(bool) shouldBe bson.toBson(Boolean.serializer(), bool)
    }
    "date time" {
        val t = Clock.systemUTC().millis()
        BsonDateTime(t) shouldBe bson.toBson(DateTimeSerializer, t)
    }
    "null" {
        BsonNull.VALUE shouldBe bson.toBson(NullSerializer, null)
    }
    "java script" {
        val code = "main() {}"
        BsonJavaScript(code) shouldBe bson.toBson(JavaScriptSerializer, code)
    }
    "int 32" {
        val i = Random.nextInt()
        BsonInt32(i) shouldBe bson.toBson(Int.serializer(), i)
    }
    "int 64" {
        val l = Random.nextLong()
        BsonInt64(l) shouldBe bson.toBson(Long.serializer(), l)
    }
    "decimal 128" {
        val decimal = Decimal128(Random.nextLong())
        BsonDecimal128(decimal) shouldBe bson.toBson(Decimal128Serializer, decimal)
    }
    "regular expresion" {
        BsonRegularExpression("[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", "imdxs") shouldBe bson.toBson(
            RegexSerializer, Regex(
                "[+-]?(\\d+(\\.\\d+)?|\\.\\d+)([eE][+-]?\\d+)?", setOf(
                    RegexOption.IGNORE_CASE,
                    RegexOption.MULTILINE,
                    RegexOption.UNIX_LINES,
                    RegexOption.COMMENTS,
                    RegexOption.DOT_MATCHES_ALL
                )
            )
        )
    }
    "enum kind" {
        val notFound = HttpError.NOT_FOUND
        val error = HttpError.INTERNAL_SERVER_ERROR

        BsonString(notFound.name) shouldBe bson.toBson(HttpError.serializer(), notFound)
        BsonString(error.name) shouldBe bson.toBson(HttpError.serializer(), error)
    }

})