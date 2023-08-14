package com.example.sendmessage

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sendmessage.databinding.ActivityMainBinding
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dataModel: DataModel by viewModels()
    private var contacts: ArrayList<Contact>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Permissions.setPermissions(this)

        openFrag(R.id.message, Message.newInstance())

        ContactsInCache.loadContactsFromCache(applicationContext).apply {
            if (this == null) {
                val pool = Executors.newFixedThreadPool(1)
                pool.execute { dataLoad() } // make parallel thread in pool
            } else {
                openFrag(R.id.contactsList, ContactsList.newInstance(this))
            }
        }

        setListeners()
    }

    private fun dataLoad() {
        runOnUiThread { // animation before loading
            binding.shimmerLayout?.startShimmerAnimation()
            binding.shimmerLayout?.visibility = View.VISIBLE
        }

        contacts = Contacts.getContactsList(this)
        openFrag(R.id.contactsList, ContactsList.newInstance(contacts))

        runOnUiThread { // animation after loading
            binding.shimmerLayout?.stopShimmerAnimation()
            binding.shimmerLayout?.visibility = View.GONE
        }
    }

    private fun openFrag(idFragment: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(idFragment, fragment).commit()
    }

    private fun setListeners() {
        binding.sendMessage?.setOnClickListener {
            if (contacts == null || contacts?.size == 0) {
                Toast.makeText(this, getString(R.string.dataLoadingError), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (Permissions.checkSmsPermission(this)) {
                if (dataModel.message.value?.isNotEmpty() == true) {
                    if (dataModel.chosenContacts.isNotEmpty()) {
                        val sentIntent = PendingIntent.getBroadcast(
                            this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
                        )
                        sendSms(
                            dataModel.message.value!!.trim(), sentIntent
                        ) // TODO make in other thread
                        dataModel.clearFunction()
                    } else {
                        Toast.makeText(
                            this, getString(R.string.noContactsChoseError), Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.emptyMessageError), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Permissions.requestSmsPermission(this)
            }
        }
    }

    private fun sendSms(message: String, sentIntent: PendingIntent) {
        @Suppress("DEPRECATION") val smsManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applicationContext.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

        val temp: MutableMap<Int, Boolean> = dataModel.chosenContacts
        for (contact in temp) {
            if (contact.value) {
                val currentContact = contacts?.firstOrNull {
                    it.id == contact.key
                }

                for (phone in currentContact?.phones!!) {
                    try {
                        smsManager.sendTextMessage(phone.key, null, message, sentIntent, null)
                    } catch (e: Exception) {
                        print(e)
                    }
                    Toast.makeText(this, getString(R.string.successSend), Toast.LENGTH_SHORT).show()
                }

                // TODO this in service and add email message
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permissions.requestCode) {
            if (grantResults.isNotEmpty()) {
                when (PackageManager.PERMISSION_GRANTED) {
                    grantResults[0] -> println("Read contacts granted")
                    grantResults[1] -> println("Send sms granted")
                }
            }
        }
    }
}