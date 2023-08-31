package com.example.sendmessage.services

import android.annotation.SuppressLint
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.example.sendmessage.MainActivity
import com.example.sendmessage.models.Contact


class Contacts {
    companion object {
        private val PROJECTION_CONTACTS = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
        )

        private val PROJECTION_PHONE = arrayOf(
            Phone.NUMBER, Phone.TYPE
        )

        private val PROJECTION_EMAIL = arrayOf(
            Email.ADDRESS, Email.TYPE
        )

        @SuppressLint("Range")
        private fun getPhones(activity: MainActivity, contactId: Int): MutableMap<String, String> {
            val numbers: MutableMap<String, String> = mutableMapOf()
            activity.contentResolver.query(
                Phone.CONTENT_URI,
                PROJECTION_PHONE,
                Phone.CONTACT_ID + " = " + contactId,
                null,
                null
            )?.use { cursorPhones ->
                while (cursorPhones.moveToNext()) {
                    var type = ""
                    when (cursorPhones.getInt(cursorPhones.getColumnIndex(Phone.TYPE))) {
                        Phone.TYPE_CUSTOM -> type = "Custom"
                        Phone.TYPE_HOME -> type = "Home"
                        Phone.TYPE_MOBILE -> type = "Mobile"
                        Phone.TYPE_OTHER -> type = "Other"
                        Phone.TYPE_WORK -> type = "Work"
                    }

                    val phoneNumber = cursorPhones.getString(
                        cursorPhones.getColumnIndex(Phone.NUMBER)
                    )
                    numbers[phoneNumber] = type
                }
            }

            return numbers
        }

        @SuppressLint("Range")
        private fun getEmails(activity: MainActivity, contactId: Int): MutableMap<String, String> {
            val emails: MutableMap<String, String> = mutableMapOf()
            activity.contentResolver.query(
                Email.CONTENT_URI,
                PROJECTION_EMAIL,
                Email.CONTACT_ID + " = " + contactId,
                null,
                null
            )?.use { cursorEmails ->
                while (cursorEmails.moveToNext()) {
                    var type = ""
                    when (cursorEmails.getInt(cursorEmails.getColumnIndex(Email.TYPE))) {
                        Email.TYPE_CUSTOM -> type = "Custom"
                        Email.TYPE_HOME -> type = "Home"
                        Email.TYPE_MOBILE -> type = "Mobile"
                        Email.TYPE_OTHER -> type = "Other"
                        Email.TYPE_WORK -> type = "Work"
                    }

                    val emailAddress = cursorEmails.getString(
                        cursorEmails.getColumnIndex(Email.ADDRESS)
                    )
                    emails[emailAddress] = type
                }
            }

            return emails
        }

        @SuppressLint("Recycle", "Range")
        fun getContactsList(activity: MainActivity): ArrayList<Contact> {
            val contentResolver = activity.contentResolver
            val contacts: ArrayList<Contact> = ArrayList()

            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, PROJECTION_CONTACTS, null, null, null
            )?.use { cursorContacts ->
                while (cursorContacts.moveToNext()) {
                    if (cursorContacts.getInt(cursorContacts.getColumnIndex(Phone.HAS_PHONE_NUMBER)) > 0) {
                        val contactId =
                            cursorContacts.getInt(cursorContacts.getColumnIndex(ContactsContract.Contacts._ID))
                        val contactName =
                            cursorContacts.getString(cursorContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                        val photoUri =
                            cursorContacts.getString(cursorContacts.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))

                        val phones = getPhones(activity, contactId)
                        val emails = getEmails(activity, contactId)
                        val contact = Contact(
                            contactId, // add contact id
                            contactName, // add contact name
                            when (phones.size) {
                                0 -> null
                                else -> phones
                            }, // add contact photo
                            when (emails.size) {
                                0 -> null
                                else -> emails
                            }, // add contact phones
                            photoUri, // add contact emails
                        )
                        contacts.add(contact)
                    }
                }
            }

            return contacts
        }
    }
}