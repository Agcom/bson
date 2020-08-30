package com.github.agcom.bson.serialization.utils

import org.bson.BsonRegularExpression
import kotlin.text.RegexOption.*

operator fun BsonRegularExpression.invoke(regex: Regex): BsonRegularExpression = regex.toBsonRegularExpression()

fun Regex.toBsonRegularExpression(): BsonRegularExpression = BsonRegularExpression(pattern, options.asEmbedded())
fun BsonRegularExpression.toRegex(): Regex = Regex(pattern, options.toRegexOptions())

/**
 * Ignores [LITERAL] and [CANON_EQ].
 * @return E.g. 'im' for [IGNORE_CASE] + [MULTILINE], empty string for empty set.
 */
internal fun Set<RegexOption>.asEmbedded(): String {
    val builder = StringBuilder(size)
    forEach {
        when(it) {
            IGNORE_CASE -> 'i'
            MULTILINE -> 'm'
            LITERAL -> null // Runtime flag. Only used for parsing the pattern in specific way if needed.
            UNIX_LINES -> 'd'
            COMMENTS -> 'x'
            DOT_MATCHES_ALL -> 's'
            CANON_EQ -> null // Runtime flag. Used to restrict match algorithm if needed.
        }?.let(builder::append)
    }
    return if(builder.isEmpty()) "" else builder.toString()
}
internal fun String.toRegexOptions(): Set<RegexOption> {
    return map {
        when(it) {
            'i' -> IGNORE_CASE
            'm' -> MULTILINE
            'd' -> UNIX_LINES
            'x' -> COMMENTS
            's' -> DOT_MATCHES_ALL
            else -> error("Invalid character '$it' for a regex option")
        }
    }.toSet()
}