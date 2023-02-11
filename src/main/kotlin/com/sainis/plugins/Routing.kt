package com.sainis.plugins

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.sainis.models.Account
import com.sainis.models.UserSession
import com.sainis.sendEmailWithAttachment
import com.sainis.services.GoogleAPI.getDrive
import com.sainis.services.GoogleAPI.getGmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt

fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
fun Application.configureRouting(db: MongoDatabase) {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.x/dist/tailwind.min.css")
                }
                body {
                    header(classes = "bg-indigo-800 py-6") {
                        div(classes = "container mx-auto") {
                            nav(classes = "flex items-center justify-between") {
                                h1(classes = "text-white text-2xl font-medium") { +"Google Drive to Kindle" }
                                a(href = "/register", classes = "inline-block bg-white py-3 px-6 text-indigo-800 font-medium rounded hover:bg-indigo-200") { +"Sign Up" }
                                a(href = "/login", classes = "inline-block bg-white py-3 px-6 text-indigo-800 font-medium rounded hover:bg-indigo-200") { +"Log In" }
                            }
                        }
                    }
                    main(classes = "py-12") {
                        div(classes = "container mx-auto") {
                            h2(classes = "text-3xl font-medium text-center") { +"Easily convert your Google Drive docs to Kindle" }
                            p(classes = "text-lg text-center mt-6") { +"With our app, you can quickly send your Google Drive documents to your Kindle, so you can read them on the go. Simply sign up and start converting!" }
                        }
                    }
                }
            }
        }

        get("/login") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.x/dist/tailwind.min.css")
                }
                body {
                    h1 { +"Login" }
                    form(method = FormMethod.post, action = "/login", classes = "max-w-sm mx-auto p-6 bg-white rounded-lg shadow-xl") {
                        p {
                            label(classes = "block text-sm font-medium leading-5 text-gray-700") { +"Username:" }
                            textInput(name = "username", classes = "mt-1 form-input block w-full py-2 px-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:shadow-outline-blue focus:border-blue-300 transition duration-150 ease-in-out sm:text-sm sm:leading-5")
                        }
                        p {
                            label(classes = "block text-sm font-medium leading-5 text-gray-700") { +"Password:" }
                            passwordInput(name = "password", classes = "mt-1 form-input block w-full py-2 px-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:shadow-outline-blue focus:border-blue-300 transition duration-150 ease-in-out sm:text-sm sm:leading-5")
                        }
                        p {
                            submitInput(classes = "mt-6 bg-indigo-600 text-white active:bg-indigo-700 text-sm font-medium leading-5 rounded-md hover:bg-indigo-700 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 transition duration-150 ease-in-out")
                        }
                    }
                    p {
                        a(href = "/register", classes = "mt-6 inline-block text-indigo-600 hover:text-indigo-900") { +"Create an account" }
                    }
                }
            }
        }

        post("/login") {
            val post = call.receive<Parameters>()
            val username = post["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val password = post["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

            val user = db.getCollection("Accounts").find(Filters.eq("username", username)).first()
            if (user == null) {
                call.respondText("Couldn't find user with email: $username")
            }
            if (BCrypt.checkpw(password, user["password"].toString())) {
                call.respondText("Password for $username is incorrect")
            } else {
                val account = Account(
                    username = user["username"].toString(),
                    password = user["password"].toString(),
                    kindleEmail = user["kindleEmail"].toString()
                )
                call.sessions.set(UserSession(user["_id"].toString(), account))
                call.respondRedirect("/app")
            }
        }

        get("/app") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.x/dist/tailwind.min.css")
                }
                body {
                    form(action = "/send-to-kindle", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post, classes = "bg-white p-6 rounded-lg shadow-md") {
                        p {
                            +"GoogleDrive Id:"
                            textInput(name = "driveId", classes = "block border border-gray-400 p-2 rounded-lg w-full")
                        }
                        p {
                            +"My Kindle email:"
                            emailInput(name = "kindleEmail", classes = "block border border-gray-400 p-2 rounded-lg w-full")
                        }
                        p {
                            submitInput(classes = "bg-indigo-500 text-white py-2 px-4 rounded-lg hover:bg-indigo-600") { value = "Send" }
                        }
                    }
                }
            }

        }
//1kZHuU927vBvh4PxL1t7No3xLaDyxB6ZH
        post("/send-to-kindle") {
            val session = call.sessions.get<UserSession>()
            session?.let { s ->
                val drive = getDrive(s.userId)
                val gmail = getGmail(s.userId)
                val formParameters = call.receiveParameters()
                val driveId = formParameters["driveId"].toString()
                val kindleEmail = formParameters["kindleEmail"].toString()
                sendEmailWithAttachment("sainis.panag@gmaill.com", kindleEmail, "Convert", "Take this", drive, gmail, driveId)
            }
            call.respondText("All Done")
        }

        get("/register") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.x/dist/tailwind.min.css")
                }
                body {
                    h1 { +"Register" }
                    form(method = FormMethod.post, action = "/register", classes = "bg-white p-6 rounded-lg shadow-md") {
                        p {
                            label { +"Username:" }
                            textInput(name = "username", classes = "block border border-gray-400 p-2 rounded-lg w-full")
                        }
                        p {
                            label { +"Password:" }
                            passwordInput(name = "password", classes = "block border border-gray-400 p-2 rounded-lg w-full")
                        }
                        p {
                            label { +"Kindle email:" }
                            textInput(name = "kindleEmail", classes = "block border border-gray-400 p-2 rounded-lg w-full")
                        }
                        p {
                            submitInput(classes = "bg-indigo-500 text-white py-2 px-4 rounded-lg hover:bg-indigo-600")
                        }
                    }
                    p {
                        a(href = "/", classes = "text-indigo-500 underline") { +"Back to login" }
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

            val accountCollection = db.getCollection("Accounts") // get the Accounts collection
            val result = accountCollection.find(Filters.eq("username",username)).first()
            if (result != null) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
            } else {
                val newAccount = Account(username = username, password = hashPassword(password))
                val document = Document("username", newAccount.username).append("password",newAccount.password)
                accountCollection.insertOne(document)
                call.respondRedirect("/app")
            }
        }

    }
}
