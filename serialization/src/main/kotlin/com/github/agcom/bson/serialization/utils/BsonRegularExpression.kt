package com.github.agcom.bson.serialization.utils

import org.bson.BsonRegularExpression
import java.util.regex.Pattern

/**
 * Constructor like builder for [BsonRegularExpression] using Java [Pattern].
 */
operator fun BsonRegularExpression.invoke(pattern: Pattern): BsonRegularExpression = pattern.toBsonRegularExpression()

/**
 * Constructor like builder for [BsonRegularExpression] using Kotlin [Regex].
 */
operator fun BsonRegularExpression.invoke(regex: Regex): BsonRegularExpression = regex.toBsonRegularExpression()

/**
 * Convert a Java [Pattern] into it's bson type class [BsonRegularExpression].
 */
fun Pattern.toBsonRegularExpression(): BsonRegularExpression =
    BsonRegularExpression(pattern(), PatternUtils.flagsAsEmbedded(this))

/**
 * Convert a Kotlin [Regex] into it's bson type class [BsonRegularExpression].
 */
fun Regex.toBsonRegularExpression(): BsonRegularExpression = toPattern().toBsonRegularExpression()

/**
 * Convert a [BsonRegularExpression] into Java [Pattern].
 */
fun BsonRegularExpression.toPattern(): Pattern = Pattern.compile(pattern, PatternUtils.embeddedAsFlags(options))

/**
 * Convert a [BsonRegularExpression] into Kotlin [Regex].
 */
fun BsonRegularExpression.toRegex(): Regex = toPattern().toRegex()

// Deals with the pattern options' conversations
internal object PatternUtils {

    fun flagsAsEmbedded(pattern: Pattern): String = flagsAsEmbedded(pattern.flags())

    fun embeddedAsFlags(regex: BsonRegularExpression): Int = embeddedAsFlags(regex.options)

    fun flagsAsEmbedded(flags: Int): String {
        var processedFlags = flags
        val embedded = StringBuilder()

        for (flag in PatternFlag.values()) {
            if (flags and flag.mask > 0) {
                embedded.append(flag.embedded)
                processedFlags -= flag.mask
            }
        }

        require(processedFlags <= 0) { "some flags couldn't be recognized" }
        return embedded.toString()
    }

    fun embeddedAsFlags(embedded: String): Int {
        if (embedded.isEmpty()) return 0

        var flags = 0

        for (c in embedded.toLowerCase()) {
            val flag = PatternFlag(c) ?: throw IllegalArgumentException("Unrecognized flag 'c'")
            flags = flags or flag.mask
        }

        return flags
    }

    private enum class PatternFlag(
        val mask: Int,
        val embedded: Char
    ) {
        CANON_EQ(Pattern.CANON_EQ, 'c'),
        UNIX_LINES(Pattern.UNIX_LINES, 'd'),
        GLOBAL(GLOBAL_FLAG, 'g'),
        CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE, 'i'),
        MULTILINE(Pattern.MULTILINE, 'm'),
        DOTALL(Pattern.DOTALL, 's'),
        LITERAL(Pattern.LITERAL, 't'),
        UNICODE_CASE(Pattern.UNICODE_CASE, 'u'),
        COMMENTS(Pattern.COMMENTS, 'x');

        companion object {
            private val BY_EMBEDDED: Map<Char, PatternFlag> =
                values().associateByTo(HashMap(values().size, 1.0f)) { it.embedded }

            operator fun invoke(embedded: Char): PatternFlag? {
                return BY_EMBEDDED[embedded]
            }
        }
    }

    private const val GLOBAL_FLAG = 256

}