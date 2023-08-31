package com.example.sendmessage

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import com.example.sendmessage.databinding.ContactInfoBinding
import com.example.sendmessage.databinding.InfoFieldBinding


class ContactInfoDialog(context: Context, contact: Contact) {
    private var context: Context
    private var contact: Contact
    private lateinit var binding: ContactInfoBinding
    private lateinit var bindingInfo: InfoFieldBinding

    init {
        this.context = context
        this.contact = contact
    }

    @SuppressLint("InflateParams")
    fun getDialog(): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.contact_info, null, false)
        binding = ContactInfoBinding.bind(view)

        val dialog = dialog(view)
        setData(dialog)
        return dialog
    }

    @SuppressLint("UseCompatLoadingForDrawables", "InflateParams")
    private fun setData(dialog: Dialog) {
        binding.apply {
            if (contact.photoUri == null) {
                this.contactPhoto?.setImageDrawable(context.getDrawable(R.drawable.no_photo))
            } else {
                this.contactPhoto?.setImageURI(Uri.parse(contact.photoUri))
            }

            this.contactName?.text = contact.name

            this.close?.setOnClickListener {
                dialog.dismiss()
            }

            // region Set numbers
            contact.phones?.let {
                for (item in it) {
                    val infoView =
                        LayoutInflater.from(context).inflate(R.layout.info_field, null, false)
                    bindingInfo = InfoFieldBinding.bind(infoView)

                    bindingInfo.info.text = item.key
                    bindingInfo.infoType.text = item.value

                    binding.contactNumbers?.apply {
                        this.addView(infoView)
                    }
                }
            }
            // endregion

            // region Set emails
            contact.emails?.let {
                for (item in it) {
                    val infoView =
                        LayoutInflater.from(context).inflate(R.layout.info_field, null, false)
                    bindingInfo = InfoFieldBinding.bind(infoView)

                    bindingInfo.info.text = item.key
                    bindingInfo.infoType.text = item.value

                    binding.contactEmails?.apply {
                        this.addView(infoView)
                    }
                }
            } ?: let {
                binding.placeForEmails?.visibility = View.GONE
            }
            // endregion
        }
    }

    private fun dialog(view: View): Dialog {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setContentView(view)
        val window = dialog.window

        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            else -> {
                window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }

        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        return dialog
    }
}