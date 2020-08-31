package com.github.agcom.bson.serialization

/**
 * The parameters are self explanatory.
 *
 * If you want more information, checkout [kotlinx.serialization.json.JsonConfiguration] documentations for corresponding parameters.
 */
data class BsonConfiguration(
    val encodeDefaults: Boolean = false,
    val ignoreUnknownKeys: Boolean = false,
    internal val allowDuplicateKey: Boolean = false,
    val classDiscriminator: String = "type"
) {

    companion object {
        val DEFAULT = BsonConfiguration()

        /**
         * Won't change across different releases.
         */
        val STABLE = BsonConfiguration(
            encodeDefaults = false,
            ignoreUnknownKeys = false,
            allowDuplicateKey = false,
            classDiscriminator = "type"
        )
    }

}