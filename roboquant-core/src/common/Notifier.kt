/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.common

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.*

/**
 * Interface for sending notifications to the outside world. This can be for example emails, but also social media
 * like Twitter and Telegram.
 */
interface Notifier {

    /**
     * Send out notifications using the provided [topic] and [msg]
     */
    fun send(topic: String, msg: String)
}

/**
 * Notifier that prints the message to the console
 */
class ConsoleNotifier: Notifier {
    override fun send(topic: String, msg: String) {
        println("topic: $topic\n $msg\n")
        println("##############################################")
    }
}

/**
 * Send out email notifications
 */
class EmailNotifier(
    host: String,
    username: String,
    password: String,
    to: String = username,
    from: String = username,
    port: Int = 587
) : Notifier {

    private val session: Session

    init {
        val props = Properties()
        props["mail.smtp.host"] = host
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.port"] = "$port"

        session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
    }

    /**
     * To address
     */
    var toAddress = InternetAddress(to)

    /**
     * From address
     */
    var fromAddress = InternetAddress(from)

    companion object {

        /**
         * Using Gmail for sending emails.  You'll need to enable access to Gmail by allowing username/password
         * authentication (called less secure apps in Google terms). Do this at your own risk.
         *
         * [Click here to change setting](https://www.google.com/settings/security/lesssecureapps)
         *
         * @param username
         * @param password
         * @return
         */
        fun usingGmail(
            username: String,
            password: String,
            to: String = username,
            from: String = username
        ) = EmailNotifier("smtp.gmail.com", username, password, to, from)


        /**
         * Using Outlook/Office 365 for sending the emails.
         *
         * @param username
         * @param password
         * @param to
         * @param from
         */
        fun usingOutlook(
            username: String,
            password: String,
            to: String = username,
            from: String = username
        ) = EmailNotifier("smtp.office365.com", username, password, to, from)


        /**
         * Using Yahoo! Mail for sending emails. You'll need enable access to Yahoo! Mail by generating a
         * third-party app password and use that. Your regular Yahoo! Mail password won't work. Do this at your own risk.
         *
         * Steps to follow:
         *
         * - Sign in to your Yahoo Account Security page
         * - Click Generate app password or Generate and manage app passwords
         * - Enter your app's name in the text field, f.e roboquant
         * - Click Generate password
         * - Paste that password and use it in roboquant
         * - Click Done
         *
         * @param username
         * @param password
         * @param to
         * @return
         */
        fun usingYahoo(username: String, password: String, to: String = username, from: String = username) =
            EmailNotifier("smtp.mail.yahoo.com", username, password, to, from)

    }

    /**
     * Send a test email to validate if notifications are working. You should receive an email in your inbox soon
     * after calling this method if the email settings are correct.
     *
     */
    fun test() {
        val subject = "roboquant test message"
        val body = """
            Dear Trader, 
            
            This is a test e-mail to validate that sending notifications is working correctly. 
            
            Used configuration:
            ===============
            from: ${fromAddress.address}
            to: ${toAddress.address}
            subject: $subject
            properties: ${session.properties}
            
            You can safely delete this message.
            
            Kind regards,
            roboquant
            """.trimIndent()
        send(subject, body)
    }

    override fun send(topic: String, msg: String) {
        val mime = MimeMessage(session)
        mime.setFrom(fromAddress)
        mime.addRecipient(Message.RecipientType.TO, toAddress)
        mime.subject = topic
        mime.setText(msg)
        Transport.send(mime)
    }

}