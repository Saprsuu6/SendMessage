package com.example.sendmessage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.sendmessage.databinding.ContactsListItemBinding

class ContactListViewAdapter(
    private val context: Context,
    private val arrayList: ArrayList<Contact>,
    private var dataModel: DataModel
) : BaseAdapter() {
    private var binding: ContactsListItemBinding? = null

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView?.apply {
            binding?.apply { ContactsListItemBinding.bind(convertView) }
            setData(position)
        } ?: LayoutInflater.from(context).inflate(R.layout.contacts_list_item, parent, false)
            .apply {
                binding = ContactsListItemBinding.bind(this)
                setData(position)
            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setData(position: Int) {
        binding?.apply {
            if (arrayList[position].photoUri == null) {
                this.contactPhoto.setImageDrawable(context.getDrawable(R.drawable.no_photo))
            } else {
                this.contactPhoto.setImageURI(Uri.parse(arrayList[position].photoUri))
            }

            this.contactName.text = arrayList[position].name

            val temp = arrayList[position].chosen
            this.isChosen.isChecked = temp

            setListeners(this, position)
        }
    }

    private fun setListeners(binder: ContactsListItemBinding, position: Int) {
        binder.isChosen.setOnCheckedChangeListener { _, isChecked ->
            if (binder.isChosen.isPressed) {
                arrayList[position].chosen = isChecked

                if (isChecked) {
                    dataModel.chosenContacts[arrayList[position].id] = true
                } else {
                    dataModel.chosenContacts.remove(arrayList[position].id)
                }
            }
        }
    }
}