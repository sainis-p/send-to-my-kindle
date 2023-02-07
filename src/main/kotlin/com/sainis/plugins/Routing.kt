package com.sainis.plugins

import com.mongodb.client.MongoDatabase
import com.sainis.models.Account
import com.sainis.sendEmailWithAttachment
import com.sainis.services.GoogleAPI.getDrive
import com.sainis.services.GoogleAPI.getGmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.mindrot.jbcrypt.BCrypt

fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
fun Application.configureRouting(db: MongoDatabase) {
    routing {
        get("/login") {
            call.respondHtml {
                body {
                    h1 { +"Login" }
                    form(method = FormMethod.post, action = "/login") {
                        p {
                            label { +"Username:" }
                            textInput(name = "username")
                        }
                        p {
                            label { +"Password:" }
                            passwordInput(name = "password")
                        }
                        p {
                            submitInput()
                        }
                    }
                    p {
                        a(href = "/register") { +"Create an account" }
                    }
                }
            }
        }

        post("/login") {
            val post = call.receive<Parameters>()
            val username = post["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val password = post["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

            val user = db.getCollection<Account>("Accounts").findOne(Account::username eq username)
            if (user == null) {
                call.respondText("Couldn't find user with email: $username")
            }
            if (BCrypt.checkpw(password, user?.password)) {
                call.respondText("Password for $username is incorrect")
            } else {
                call.respondRedirect("/")
            }
        }

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

        get("/register") {
            call.respondHtml {
                body {
                    h1 { +"Register" }
                    form(method = FormMethod.post, action = "/register") {
                        p {
                            label { +"Username:" }
                            textInput(name = "username")
                        }
                        p {
                            label { +"Password:" }
                            passwordInput(name = "password")
                        }
                        p {
                            label { +"Kindle email:" }
                            textInput(name = "kindleEmail")
                        }
                        p {
                            submitInput()
                        }
                    }
                    p {
                        a(href = "/") { +"Back to login" }
                    }
                }
            }
        }

        post("/register") {
            val formParameters = call.receiveParameters()
            val username = formParameters["username"].toString()
            val password = formParameters["password"].toString()

            if (username == null || password == null) {
                call.respond(HttpStatusCode.BadRequest, "Bad Request")
            }

            val accountCollection = db.getCollection<Account>("Accounts") // get the Accounts collection
            val result = accountCollection.findOne(Account::username eq username)
            if (result != null) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
            } else {
                val newAccount = Account(username = username, password = hashPassword(password))
                accountCollection.insertOne(newAccount)
                call.respond("Success")
            }
        }

    }
}
