package org.roboquant.common

import com.sun.mail.smtp.SMTPMessage
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMultipart
import java.util.*

class MimeMessage {

    fun getSession(
        host: String? = null,
        port: Int? = null,
        username: String? = null,
        password: String? = null
    ): Session {
        val props = Properties()
        props["mail.smtp.host"] = host ?: Config.getProperty("mail.host")
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.port"] = if (port != null) "$port" else Config.getProperty("mail.port") ?: "587"

        val fUsername = username ?: Config.getProperty("mail.username")
        val fPassword = password ?: Config.getProperty("mail.password")

        return Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(fUsername, fPassword)
                }
            })
    }

    fun send(to: String, session: Session, html: String, svg: String, from: String? = null) {
        val content = MimeMultipart("related")
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(html, "text/html")
        content.addBodyPart(htmlPart)
        val imagePart = MimeBodyPart()
        imagePart.setContent(svg, "image/svg+xml")
        imagePart.fileName = "chart.svg"
        imagePart.contentID = "ChartImage"
        imagePart.disposition = MimeBodyPart.INLINE
        content.addBodyPart(imagePart)
        val m = SMTPMessage(session)
        m.setContent(content)
        m.subject = "Mail with embedded image"

        val ffrom = from ?: Config.getProperty("mail.from")
        if (ffrom != null) m.setFrom(ffrom)
        m.setRecipient(Message.RecipientType.TO, InternetAddress(to))
        Transport.send(m)
    }

}