package com.sainis.models

data class Account(
    val username: String,
    val password: String,
    val kindleEmail: String? = null
)