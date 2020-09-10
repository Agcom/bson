package com.github.agcom.bson.serialization.utils

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.bson.BsonRegularExpression

class BsonRegularExpressionTest : FreeSpec({

    "options as embedded" - {

        "test 1" {
            emptySet<RegexOption>().asEmbedded() shouldBe ""
        }

        "test 2" {
            setOf(
                RegexOption.IGNORE_CASE,
                RegexOption.MULTILINE,
                RegexOption.LITERAL,
                RegexOption.UNIX_LINES,
                RegexOption.COMMENTS,
                RegexOption.DOT_MATCHES_ALL,
                RegexOption.CANON_EQ
            ).asEmbedded() shouldBe "imdxs"
        }

    }

    "string as options" - {

        "test 1" {
            "".toRegexOptions() shouldBe emptySet()
        }

        "test 2" {
            "imdxs".toRegexOptions() shouldBe setOf(
                RegexOption.IGNORE_CASE,
                RegexOption.MULTILINE,
                RegexOption.UNIX_LINES,
                RegexOption.COMMENTS,
                RegexOption.DOT_MATCHES_ALL
            )
        }

    }

    "bson to regex" {
        val toRegex = BsonRegularExpression("acme.*corp", "imdxs").toRegex()
        val expected = Regex("acme.*corp", setOf(
            RegexOption.IGNORE_CASE,
            RegexOption.MULTILINE,
            RegexOption.UNIX_LINES,
            RegexOption.COMMENTS,
            RegexOption.DOT_MATCHES_ALL
        ))

        toRegex shouldBe expected
        toRegex.options shouldBe expected.options
    }

    "regex to bson" {
        val regex = Regex("acme.*corp", setOf(
            RegexOption.IGNORE_CASE,
            RegexOption.MULTILINE,
            RegexOption.UNIX_LINES,
            RegexOption.COMMENTS,
            RegexOption.DOT_MATCHES_ALL
        ))
        regex.toBsonRegularExpression() shouldBe BsonRegularExpression("acme.*corp", "imdxs")
    }

})