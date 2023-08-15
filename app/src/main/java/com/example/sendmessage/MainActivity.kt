package com.example.sendmessage

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sendmessage.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dataModel: DataModel by viewModels()
    private var contacts: ArrayList<Contact>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Permissions.setPermissions(this)
        openFrag(R.id.message, Message.newInstance())

        ContactsInCache.loadContactsFromCache(applicationContext, dataModel).apply {
            if (this == null) {
                Executors.newFixedThreadPool(1).execute {
                    dataLoad()
                } // make parallel thread in pool
            } else {
                contacts = this
                openFrag(R.id.contactsList, ContactsList.newInstance(contacts))
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setListeners() {
        binding.sendMessage?.setOnClickListener {
            if (contacts == null || contacts?.size == 0) {
                Toast.makeText(this, getString(R.string.dataLoadingError), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // region check permissions
            if (!Permissions.checkSmsPermission(this)) {
                Permissions.requestSmsPermission(this)
                return@setOnClickListener
            }
            if (!Permissions.checkNotificationsPermission(this)) {
                Permissions.requestNotificationsPermission(this)
                return@setOnClickListener
            }
            // endregion

            if (dataModel.message.value?.isNotEmpty() == true) {
                if (dataModel.chosenContacts.isNotEmpty()) {
                    Executors.newFixedThreadPool(1).execute {
                        sendMessage()
                    } // make parallel thread in pool
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
        }
    }

    private fun sendMessage() {
        val contactsToSend: ArrayList<Contact> = arrayListOf()
        val temp: MutableMap<Int, Boolean> = dataModel.chosenContacts
        for (contact in temp) {
            if (contact.value) {
                val currentContact = contacts?.firstOrNull {
                    it.id == contact.key
                }

                if (currentContact != null) {
                    contactsToSend.add(currentContact)
                }
            }
        }

        val message = dataModel.message.value?.toString()?.trim()
        val intent = Intent(this, SendSmsService::class.java)
        intent.putExtra(getString(R.string.contactsToService), Gson().toJson(contactsToSend))
        intent.putExtra(getString(R.string.messageToService), message)
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permissions.requestCode) {
            if (grantResults.isNotEmpty()) {
                when (PackageManager.PERMISSION_GRANTED) {
                    grantResults[0] -> Log.println(
                        Log.INFO, "READ_CONTACTS", "read contacts granted"
                    )

                    grantResults[1] -> Log.println(Log.INFO, "SEND_SMS", "sms granted")
                    grantResults[2] -> Log.println(
                        Log.INFO, "POST_NOTIFICATIONS", "notification granted"
                    )
                }
            }
        }
    }
}