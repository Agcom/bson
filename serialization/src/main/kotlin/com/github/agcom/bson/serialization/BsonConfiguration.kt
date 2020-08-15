package com.github.agcom.bson.serialization

data class BsonConfiguration(
        val encodeDefaults: Boolean = false,
        val ignoreUnknownKeys: Boolean = false,
        internal val allowDuplicateKey: Boolean = false,
        val classDiscriminator: String = "type"
) {

    companion object {
        val DEFAULT = BsonConfiguration()
        val STABLE = BsonConfiguration(
                encodeDefaults = false,
                ignoreUnknownKeys = false,
                allowDuplicateKey = false,
                classDiscriminator = "type"
        )
    }

}