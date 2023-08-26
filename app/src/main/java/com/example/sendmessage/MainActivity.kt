package com.example.sendmessage

import android.app.TimePickerDialog
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
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.example.sendmessage.databinding.ActivityMainBinding
import com.example.sendmessage.fragments.ContactsList
import com.example.sendmessage.fragments.Message
import com.example.sendmessage.models.Contact
import com.example.sendmessage.models.DataModel
import com.example.sendmessage.other.Cache
import com.example.sendmessage.other.DataInCache
import com.example.sendmessage.other.Permissions
import com.example.sendmessage.services.SendSmsService
import com.google.gson.Gson
import java.util.Calendar
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dataModel: DataModel by viewModels()
    private var contacts: ArrayList<Contact>? = null
    private var timerIsPressed: Boolean? = null
    private var calendar = Calendar.getInstance()
    private var intentForService: Intent? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Permissions.setPermissions(this)

        DataInCache.loadMessage(applicationContext).apply {
            if (this == null || this == "") {
                openFrag(R.id.message, Message.newInstance())
            } else {
                openFrag(R.id.message, Message.newInstance(this))
            }
        }

        DataInCache.loadContactsFromCache(applicationContext, dataModel).apply {
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

    override fun onResume() {
        super.onResume()
        Cache().loadLong(applicationContext, getString(R.string.timerCache)).apply {
            binding.mailingListMessage?.let {
                if (this == 0L) {
                    TooltipCompat.setTooltipText(
                        it, getString(R.string.mailingListMessageOffToolTrip)
                    )
                    binding.mailingListMessage?.text = getString(R.string.mailingListMessageOff)
                    timerIsPressed = false
                } else {
                    calendar.timeInMillis = this

                    TooltipCompat.setTooltipText(
                        it,
                        getString(R.string.mailingListMessageOnToolTrip) + " ${calendar.get(Calendar.HOUR_OF_DAY)} hours and ${
                            calendar.get(
                                Calendar.MINUTE
                            )
                        } minutes"
                    )
                    binding.mailingListMessage?.text = getString(R.string.mailingListMessageOn)
                    timerIsPressed = true
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (timerIsPressed == true) {
            Cache().saveLong(
                applicationContext, getString(R.string.timerCache), calendar.timeInMillis
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        intentForService?.let {
            stopService(it)
            intentForService = null
        }
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

        binding.mailingListMessage?.setOnClickListener {
            if (timerIsPressed == false) {
                TimePickerDialog(
                    this, { _, hours, minutes ->
                        calendar.set(Calendar.HOUR_OF_DAY, hours)
                        calendar.set(Calendar.MINUTE, minutes)

                        binding.mailingListMessage?.let {
                            TooltipCompat.setTooltipText(
                                it,
                                getString(R.string.mailingListMessageOnToolTrip) + " $hours hours and $minutes minutes"
                            )
                        }
                        // save in cache
                        Cache().saveLong(
                            applicationContext,
                            getString(R.string.timerCache),
                            calendar.timeInMillis
                        )
                        binding.mailingListMessage?.text = getString(R.string.mailingListMessageOn)
                        timerIsPressed = true
                    }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true
                ).show()
                binding.mailingListMessage?.text = getString(R.string.mailingListMessageOff)
            } else {
                intentForService?.let {
                    stopService(it)
                    intentForService = null
                }

                binding.mailingListMessage?.let {
                    TooltipCompat.setTooltipText(
                        it, getString(R.string.mailingListMessageOffToolTrip)
                    )
                }
                // remove from cache
                Cache().remove(applicationContext, getString(R.string.timerCache))
                binding.mailingListMessage?.text = getString(R.string.mailingListMessageOff)
                timerIsPressed = false
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
        intentForService = Intent(this, SendSmsService::class.java)
        intentForService!!.putExtra(getString(R.string.contacts), Gson().toJson(contactsToSend))
        intentForService!!.putExtra(getString(R.string.message), message)

        if (timerIsPressed == true) {
            if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) <= 5) {
                Toast.makeText(
                    applicationContext, getString(R.string.timerError), Toast.LENGTH_SHORT
                ).show()
            } else {
                val hours: Long = calendar.get(Calendar.HOUR_OF_DAY) * 3600000L
                val minutes: Long = calendar.get(Calendar.MINUTE) * 60000L
                val totalMilliseconds: Long = hours + minutes
                intentForService!!.putExtra(getString(R.string.isWithTimer), true)
                intentForService!!.putExtra(getString(R.string.interval), totalMilliseconds)
                startService(intentForService)
            }
        } else {
            startService(intent)
        }
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