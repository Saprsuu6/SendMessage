package com.example.sendmessage.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sendmessage.services.Cache
import com.example.sendmessage.R
import com.example.sendmessage.databinding.FragmentMessageBinding
import com.example.sendmessage.models.DataModel

private const val ARG_MESSAGE = "ARG_MESSAGE"

class Message : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private val dataModel: DataModel by activityViewModels()
    private lateinit var context: Context
    private var message: String? = null
    private val clearFunction: () -> Unit = {
        binding.message.text.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        container?.let {
            context = it.context
        }

        binding = FragmentMessageBinding.inflate(inflater)
        message?.let { binding.message.setText(it) }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
        }
    }

    override fun onPause() {
        super.onPause()
        Cache().saveString(
            context, context.resources.getString(R.string.message), binding.message.text.toString()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataModel.clearFunction = clearFunction
        setListeners()
    }

    private fun setListeners() {
        binding.message.doOnTextChanged { text, _, _, _ ->
            dataModel.message.value = text?.toString()
        }

        binding.clearAll.setOnClickListener {
            clearFunction()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(message: String?) = Message().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }

        fun newInstance() = Message()
    }
}