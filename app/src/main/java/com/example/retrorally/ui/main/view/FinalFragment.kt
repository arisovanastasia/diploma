package com.example.retrorally.ui.main.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.retrorally.R
import com.example.retrorally.databinding.FragmentFinalBinding
import com.example.retrorally.ui.main.viewmodel.SharedViewModel


class FinalFragment : Fragment() {

    private var binding: FragmentFinalBinding? = null
    private val viewModel : SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFinalBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.backButton?.setOnClickListener {
            findNavController().navigate(R.id.action_finalFragment_to_authorizationFragment)
        }
    }
}