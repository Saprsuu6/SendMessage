package com.example.sendmessage

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.StrictMode
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendSmsService : Service() {
    private lateinit var send: Send
    private val channelId: String = "smsChanel"
    private val notifyId = 395
    private val defaultInterval: Long = 1000
    private var realInterval: Long? = null
    private lateinit var mainHandler: Handler
    private val updateTask = object : Runnable {
        override fun run() {
            send.run()
            realInterval?.let { mainHandler.postDelayed(this, it) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra(getString(R.string.message))
        val extras = intent?.getStringExtra(getString(R.string.contacts))
        val timer = intent?.getBooleanExtra(getString(R.string.isWithTimer), false)
        realInterval = intent?.getLongExtra(getString(R.string.interval), defaultInterval)

        val typeToken = object : TypeToken<ArrayList<Contact>>() {}.type
        val contacts: ArrayList<Contact>? = Gson().fromJson(extras, typeToken)
        send = contacts?.let { Send(applicationContext, it, message!!) }!!

        mainHandler = Handler(Looper.getMainLooper())

        if (timer == true && realInterval != null) {
            sendStartNotification()
            mainHandler.post(updateTask)
        } else {
            send.run()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendStartNotification() {
        createChannel()
        val notification = buildNotification()
        try {
            startForeground(notifyId, notification)
        } catch (e: Exception) {
            Log.println(Log.ERROR, "StartForegroundException", e.message.toString())
        }
    }

    private fun buildNotification(): Notification {
        val builder = Notification.Builder(applicationContext, channelId)

        builder.setSmallIcon(R.drawable.icon)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("describe process").setShowWhen(true).setOngoing(true)
        return builder.build()
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.smsChannelName)
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        channel.description = getString(R.string.smsChannelDescription)
        manager.createNotificationChannel(channel)
    }

    @Suppress("DEPRECATION")
    class Send(
        private val context: Context,
        private val contacts: ArrayList<Contact>,
        private val message: String
    ) : Runnable {
        private var gson: Gson = GsonBuilder().create()
        private val reportIntent: Intent = Intent("sendMessageReport")
        private var smsPendingIntent: PendingIntent

        init {
            val smsIntent = Intent(Intent.ACTION_SEND)
            smsIntent.type = context.getString(R.string.sendSmsMmsType)
            smsPendingIntent = PendingIntent.getBroadcast(
                context, 0, smsIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

        private var smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        private fun sendSms(phone: String) {
            try {
                smsManager.sendTextMessage(phone, null, message, smsPendingIntent, null)
            } catch (e: Exception) {
                Log.println(Log.ERROR, "Send sms error", e.message.toString())
            }
        }

        private fun sendEmail(email: String) {
            val myMail = context.getString(R.string.appEmail)
            val myPassword = context.getString(R.string.appEmailPassword)

            fun getSession(): Session {
                val properties = Properties()
                properties[context.getString(R.string.mailSmtpAuth)] =
                    context.getString(R.string.flagTrue)
                properties[context.getString(R.string.mailSmtpStarttlsEnable)] =
                    context.getString(R.string.flagTrue)
                properties[context.getString(R.string.mailSmtpHost)] =
                    context.getString(R.string.smtpGmailCom)
                properties[context.getString(R.string.mailSmtpPort)] =
                    context.getString(R.string.smtpGmailComPort)

                return Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(myMail, myPassword)
                    }
                })
            }

            // region prepare email
            try {
                val message = MimeMessage(getSession())
                message.setFrom(InternetAddress(myMail))
                message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(email)
                )
                message.subject = context.getString(R.string.subject)
                message.setText(this.message)

                Thread {
                    try {
                        Transport.send(message)
                    } catch (e: Exception) {
                        throw e
                    }
                }.start()
            } catch (e: Exception) {
                Log.println(Log.ERROR, "Send email error", e.message.toString())
            }
            // endregion
        }

        @SuppressLint("IntentReset")
        override fun run() {
            for (contact in contacts) {
                try {
                    contact.phones?.let {
                        for (phone in it) {
                            sendSms(phone.key)
                        }
                    }

                    contact.emails?.let {
                        for (email in it) {
                            sendEmail(email.key)
                        }
                    }

                    // region send broadcast
                    val phones = gson.toJson(contact.phones)
                    val emails = gson.toJson(contact.emails)
                    val report = Report(
                        name = contact.name,
                        phoneNumbers = phones,
                        emails = emails,
                        photoUri = contact.photoUri,
                        dateSend = Date()
                    )

                    val reportToJson = gson.toJson(report)
                    reportIntent.putExtra("report", reportToJson)
                    try {
                        context.sendBroadcast(reportIntent) // send broadcast to another app
                    } catch (e: Exception) {
                        Log.println(Log.ERROR, "Send broadcast error", e.message.toString())
                    }
                    context.sendBroadcast(reportIntent) // send broadcast to another app
                    // endregion
                } catch (e: java.lang.Exception) {
                    println(e.message)
                }

                Toast.makeText(
                    context, context.getString(R.string.successSend), Toast.LENGTH_SHORT
                ).show()
            }

            val policy: StrictMode.ThreadPolicy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
    }
}