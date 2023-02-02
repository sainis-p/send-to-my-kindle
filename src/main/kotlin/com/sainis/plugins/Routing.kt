package com.sainis.plugins

import com.sainis.DriveQuickStart.getDrive
import com.sainis.DriveQuickStart.getGmail
import com.sainis.sendEmailWithAttachment
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val drive = getDrive()
    val gmail = getGmail()

    routing {
        get("/") {
            sendEmailWithAttachment("sainis.panag@gmaill.com", "sainis.panag@gmail.com", "Convert", "Take this", drive, gmail, "1kZHuU927vBvh4PxL1t7No3xLaDyxB6ZH")
            call.respondText("Hello World!")
        }

    }
}
