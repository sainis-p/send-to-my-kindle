package com.sainis.services

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClients

class MongoDB private constructor() {
        private val username = "root"
        private val password = "rootpassword"
        private val host = "0.0.0.0"
        private val port = 27017
        private val databaseName = "send_to_kindle"

        private val credential = MongoCredential.createScramSha1Credential(username, databaseName, password.toCharArray())

        private val clientSettings = MongoClientSettings.builder()
            .credential(credential)
            .applyToSslSettings { it.enabled(false) }
            .applyToClusterSettings { it.hosts(listOf(ServerAddress(host, port))) }
            .build()

        private val client = MongoClients.create(clientSettings)

        private val database = client.getDatabase(databaseName)

        companion object {
            private var instance: MongoDB? = null

            fun getInstance(): MongoDB {
                if (instance == null) {
                    instance = MongoDB()
                }
                return instance as MongoDB
            }
        }

        fun getDatabase() = database
    }