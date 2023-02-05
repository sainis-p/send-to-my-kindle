package com.sainis.plugins

import com.sainis.DriveQuickStart.getDrive
import com.sainis.DriveQuickStart.getGmail
import com.sainis.sendEmailWithAttachment
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondHtml {
                body {
                    form(action = "/send-to-kindle", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                        p {
                            +"GoogleDrive Id:"
                            textInput(name = "driveId")
                        }
                        p {
                            +"My Kindle email:"
                            emailInput(name = "kindleEmail")
                        }
                        p {
                            submitInput() { value = "Send" }
                        }
                    }
                }
            }

        }
//1kZHuU927vBvh4PxL1t7No3xLaDyxB6ZH
        post("/send-to-kindle") {
            val drive = getDrive()
            val gmail = getGmail()
            val formParameters = call.receiveParameters()
            val driveId = formParameters["driveId"].toString()
            val kindleEmail = formParameters["kindleEmail"].toString()
            sendEmailWithAttachment("sainis.panag@gmaill.com", kindleEmail, "Convert", "Take this", drive, gmail, driveId)
            call.respondText("All Done")
        }

    }
}
