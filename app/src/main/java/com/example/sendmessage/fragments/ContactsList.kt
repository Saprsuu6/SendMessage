package com.example.sendmessage.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sendmessage.services.Cache
import com.example.sendmessage.ContactInfoDialog
import com.example.sendmessage.ContactListViewAdapter
import com.example.sendmessage.R
import com.example.sendmessage.databinding.FragmentContactsListBinding
import com.example.sendmessage.models.Contact
import com.example.sendmessage.models.DataModel
import com.example.sendmessage.services.DataInCache
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


private const val ARG_CONTACTS = "ARG_CONTACTS"

class ContactsList : Fragment(), Filterable {
    private lateinit var binding: FragmentContactsListBinding
    private var contacts: ArrayList<Contact>? = null
    private lateinit var context: Context
    private var adapter: ContactListViewAdapter? = null
    private val dataModel: DataModel by activityViewModels()
    private lateinit var contactDialog: ContactInfoDialog
    private var filter: ContactsFilter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        container?.let {
            context = it.context
        }

        binding = FragmentContactsListBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val json = it.getString(ARG_CONTACTS)
            val typeToken = object : TypeToken<ArrayList<Contact>>() {}.type
            contacts = Gson().fromJson<ArrayList<Contact>>(json, typeToken)
        }
    }

    override fun onPause() {
        super.onPause()
        DataInCache.saveContactsInCache(context, contacts)
        Cache().saveString(context, getString(R.string.filter), binding.search.text.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = contacts?.let { ContactListViewAdapter(context, it, dataModel) }
        binding.contacts.adapter = adapter

        setListeners()
        Cache().loadString(context, getString(R.string.filter)).apply {
            if (this != null && this.trim() != "") {
                binding.search.setText(this)
            }
        }
    }

    private fun setListeners() {
        binding.contacts.setOnItemClickListener { _, _, i, _ ->
            contactDialog = contacts?.get(i)?.let { ContactInfoDialog(context, it) }!!
            contactDialog.getDialog().show()
        }

        binding.uncheckAll.setOnClickListener {
            for (contact in contacts!!) {
                Cache().saveBoolean(context, contact.id.toString(), false)
                contact.chosen = false
            }
            binding.search.text.clear()

            adapter?.notifyDataSetChanged()
            binding.contacts.adapter = adapter
        }

        binding.search.doOnTextChanged { text, _, _, _ ->
            getFilter().filter(text?.toString())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(contacts: ArrayList<Contact>?) = ContactsList().apply {
            arguments = Bundle().apply {
                putString(
                    ARG_CONTACTS, Gson().toJson(contacts)
                )
            }
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = contacts?.let { ContactsFilter(it, adapter, binding, context, dataModel) }
        }
        return filter!!
    }

    private class ContactsFilter(
        private val contacts: ArrayList<Contact>,
        private var adapter: ContactListViewAdapter?,
        private var binding: FragmentContactsListBinding,
        private var context: Context,
        private val dataModel: DataModel
    ) : Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val pattern = p0?.toString()?.trim()?.lowercase()
            val filterResult = FilterResults()

            if (pattern?.length!! > 0) {
                val filteredContacts = ArrayList<Contact>()
                for (contact in contacts) {
                    if (contact.name.trim().lowercase().contains(pattern)) {
                        filteredContacts.add(contact)
                    }
                }
                filterResult.values = filteredContacts
                filterResult.count = filteredContacts.size
            } else {
                synchronized(this) {
                    filterResult.values = contacts
                    filterResult.count = contacts.size
                }
            }

            return filterResult
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            @Suppress("UNCHECKED_CAST") val filteredContacts: ArrayList<Contact> =
                p1?.values as ArrayList<Contact>
            adapter = ContactListViewAdapter(
                context, filteredContacts, dataModel
            )
            binding.contacts.adapter = adapter
        }
    }
}
