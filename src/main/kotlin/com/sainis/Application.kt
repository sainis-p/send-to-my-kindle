package com.sainis

import com.sainis.plugins.configureHTTP
import com.sainis.plugins.configureRouting
import com.sainis.services.MongoDB
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val db = MongoDB.getInstance().getDatabase()
    configureHTTP()
    configureRouting(db)
}
