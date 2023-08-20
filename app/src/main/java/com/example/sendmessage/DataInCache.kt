package com.example.sendmessage

import android.content.Context

class DataInCache {
    companion object {
        fun saveContactsInCache(context: Context, contacts: ArrayList<Contact>?) {
            Cache().saveBoolean(context, context.getString(R.string.isExist), true)
            for ((contactCounter, contact) in contacts!!.withIndex()) {
                contacts.size.let {
                    Cache().saveInt(
                        context, context.getString(R.string.amount), it
                    )
                }

                Cache().saveInt(
                    context, context.getString(R.string.id) + contactCounter, contact.id
                )
                Cache().saveString(
                    context, context.getString(R.string.name) + contactCounter, contact.name
                )
                contact.photoUri?.let {
                    Cache().saveString(
                        context, context.getString(R.string.contactPhoto) + contactCounter, it
                    )
                }

                // region phones
                contact.phones?.size?.let {
                    Cache().saveInt(
                        context, context.getString(R.string.phonesAmount) + contactCounter, it
                    )
                }
                var phoneCounter = 0
                contact.phones?.let {
                    for (phone in it) {
                        Cache().saveString(
                            context,
                            context.getString(R.string.phonesKey) + contactCounter + phoneCounter,
                            phone.key
                        )
                        Cache().saveString(
                            context,
                            context.getString(R.string.phonesValue) + contactCounter + phoneCounter,
                            phone.value
                        )
                        phoneCounter++
                    }
                }
                // endregion

                // region emails
                contact.emails?.size?.let {
                    Cache().saveInt(
                        context, context.getString(R.string.emailsAmount) + contactCounter, it
                    )
                }
                var emailCounter = 0
                contact.emails?.let {
                    for (email in it) {
                        Cache().saveString(
                            context,
                            context.getString(R.string.emailsKey) + contactCounter + emailCounter,
                            email.key
                        )
                        Cache().saveString(
                            context,
                            context.getString(R.string.emailsValue) + contactCounter + emailCounter,
                            email.value
                        )
                        emailCounter++
                    }
                }
                // endregion

                Cache().saveBoolean(
                    context, context.getString(R.string.chose) + contactCounter, contact.chosen
                )
            }
        }

        fun loadContactsFromCache(context: Context, dataModel: DataModel): ArrayList<Contact>? {
            Cache().loadBoolean(context, context.getString(R.string.isExist)).apply {
                return if (this) {
                    val contacts = ArrayList<Contact>()
                    val amountContacts =
                        Cache().loadInt(context, context.getString(R.string.amount))

                    for (contactCounter in 0 until amountContacts) {
                        val contactId = Cache().loadInt(
                            context, context.getString(R.string.id) + contactCounter
                        )
                        val contactName = Cache().loadString(
                            context, context.getString(R.string.name) + contactCounter
                        )
                        val contactPhoto = Cache().loadString(
                            context, context.getString(R.string.contactPhoto) + contactCounter
                        )

                        // region phones
                        val amountPhones = Cache().loadInt(
                            context, context.getString(R.string.phonesAmount) + contactCounter
                        )
                        val phones: MutableMap<String, String> = mutableMapOf()

                        if (amountPhones > 0) {
                            for (phoneCounter in 0 until amountPhones) {
                                val phone = Cache().loadString(
                                    context,
                                    context.getString(R.string.phonesKey) + contactCounter + phoneCounter
                                )
                                val type = Cache().loadString(
                                    context,
                                    context.getString(R.string.phonesValue) + contactCounter + phoneCounter
                                )
                                phones[phone!!] = type!!
                            }
                        }
                        // endregion

                        // region emails
                        val amountEmails = Cache().loadInt(
                            context, context.getString(R.string.emailsAmount) + contactCounter
                        )
                        val emails: MutableMap<String, String> = mutableMapOf()

                        if (amountEmails > 0) {
                            for (emailCounter in 0 until amountEmails) {
                                val email = Cache().loadString(
                                    context,
                                    context.getString(R.string.emailsKey) + contactCounter + emailCounter
                                )
                                val type = Cache().loadString(
                                    context,
                                    context.getString(R.string.emailsValue) + contactCounter + emailCounter
                                )
                                emails[email!!] = type!!
                            }
                        }
                        // endregion

                        val isChosen = Cache().loadBoolean(
                            context, context.getString(R.string.chose) + contactCounter
                        )

                        if (isChosen) {
                            dataModel.chosenContacts[contactId] = true
                        }

                        contacts.add(
                            Contact(
                                contactId, contactName!!, phones, emails, contactPhoto, isChosen
                            )
                        )
                    }

                    contacts
                } else {
                    null
                }
            }
        }

        fun loadMessage(context: Context): String? {
            return Cache().loadString(context, context.resources.getString(R.string.message))
        }
    }
}