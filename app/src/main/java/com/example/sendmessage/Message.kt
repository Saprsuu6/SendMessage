package com.example.sendmessage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sendmessage.databinding.FragmentMessageBinding

class Message : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private val dataModel: DataModel by activityViewModels()
    private val clearFunction: () -> Unit = {
        binding.message.text.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater)
        return binding.root
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
        fun newInstance() = Message()
    }
}