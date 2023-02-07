package com.sainis.services

import org.litote.kmongo.KMongo

class MongoDB private constructor() {

        private val client = KMongo.createClient("mongodb://root:rootpassword@localhost:27017")

        private val database = client.getDatabase("SendToKindle")

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