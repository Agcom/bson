package com.github.agcom.bson.serialization.utils

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.bson.BsonRegularExpression
import java.util.regex.Pattern

class BsonRegularExpressionTest : FreeSpec({

    "pattern utils" - {

        "flags as embedded" - {

            "with flags int" - {

                "no flags" {
                    val flags = 0
                    PatternUtils.flagsAsEmbedded(flags) shouldBe ""
                }

                "all flags" {
                    val flags =
                        Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
                    PatternUtils.flagsAsEmbedded(flags) shouldBe "cdgimstux"
                }

            }

            "with pattern" - {

                "no flags" {
                    val pattern = Pattern.compile("hello")
                    PatternUtils.flagsAsEmbedded(pattern) shouldBe ""
                }

                "all flags" {
                    val flags =
                        Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
                    val pattern = Pattern.compile("hello", flags)
                    PatternUtils.flagsAsEmbedded(flags) shouldBe "cdgimstux"
                }

            }

        }

        "embedded as flags" - {

            "with embedded string" - {

                "empty" {
                    val embedded = ""
                    PatternUtils.embeddedAsFlags(embedded) shouldBe 0
                }

                "all" {
                    val embedded = "cdgimstux"
                    PatternUtils.embeddedAsFlags(embedded) shouldBe (Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS)
                }

            }

            "with bson regular expression" - {

                "empty" {
                    val bsonValue = BsonRegularExpression("hello")
                    PatternUtils.embeddedAsFlags(bsonValue) shouldBe 0
                }

                "all" {
                    val bsonValue = BsonRegularExpression("hello", "cdgimstux")
                    PatternUtils.embeddedAsFlags(bsonValue) shouldBe (Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS)
                }

            }

        }

    }

    "bson to pattern" {
        val bsonValue = BsonRegularExpression("hello", "cdgimstux").toRegex()
        val flags =
            Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
        val expected = Pattern.compile(bsonValue.pattern, flags)

        val test = bsonValue.toPattern()
        test.flags() shouldBe expected.flags()
        test.pattern() shouldBe expected.pattern()
    }

    "bson to regex" {
        val bsonValue = BsonRegularExpression("hello", "cdgimstux")
        val expected = Regex(bsonValue.pattern, RegexOption.values().toSet())

        val test = bsonValue.toRegex()
        test.pattern shouldBe expected.pattern
        test.options shouldBe expected.options
    }

    "pattern to bson" {
        val flags =
            Pattern.CANON_EQ or Pattern.UNIX_LINES or 256 /* Global flag */ or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL or Pattern.LITERAL or Pattern.UNICODE_CASE or Pattern.COMMENTS
        val pattern = Pattern.compile("hello", flags)
        val expected = BsonRegularExpression(pattern.pattern(), "cdgimstux")

        pattern.toBsonRegularExpression() shouldBe expected
    }

    "regex to bson" {
        val regex = Regex("hello", RegexOption.values().toSet())
        val expected = BsonRegularExpression(regex.pattern, "cdimstux")
        regex.toBsonRegularExpression() shouldBe expected
    }

})