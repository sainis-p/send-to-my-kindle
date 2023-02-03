package com.sainis

//import com.google.api.services.drive.model.File
import com.google.api.services.drive.Drive
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

fun sendEmailWithAttachment(
    from: String,
    to: String,
    subject: String,
    body: String,
    drive: Drive,
    gmail: Gmail,
    fileId: String
) {
    val file = drive.files().get(fileId).execute()
    val fileName = file.name
    val fileExtension = fileName.substringAfterLast(".")
    val tempFile = File.createTempFile("temp", ".$fileExtension")
    val outputStream = FileOutputStream(tempFile)
    drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)


    val message = MimeMessage(Session.getDefaultInstance(System.getProperties(), null))
    message.setFrom(InternetAddress(from))
    message.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
    message.subject = subject

    val mimeBodyPart = MimeBodyPart()
    mimeBodyPart.setText(body)

    val attachmentBodyPart = MimeBodyPart()
    val source = FileDataSource(tempFile)
    attachmentBodyPart.dataHandler = DataHandler(source)
    attachmentBodyPart.fileName = fileName


    val multipart = MimeMultipart()
    multipart.addBodyPart(mimeBodyPart)
    multipart.addBodyPart(attachmentBodyPart)

    message.setContent(multipart)

    val baos = ByteArrayOutputStream()
    message.writeTo(baos)
    val encodedEmail = Base64.getEncoder().encodeToString(baos.toByteArray())

    val email = Message().setRaw(encodedEmail)
    gmail.users().messages().send("me", email).execute()

}