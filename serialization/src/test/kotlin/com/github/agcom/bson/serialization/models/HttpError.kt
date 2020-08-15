package com.github.agcom.bson.serialization.models

import kotlinx.serialization.Serializable

@Serializable
enum class HttpError {
    
    NOT_FOUND, INTERNAL_SERVER_ERROR
    
}