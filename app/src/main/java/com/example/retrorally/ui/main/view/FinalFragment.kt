package com.example.retrorally.ui.main.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.retrorally.R
import com.example.retrorally.databinding.FragmentFinalBinding


class FinalFragment : Fragment() {

    private var binding: FragmentFinalBinding? = null

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