package com.example.retrorally.ui.main.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.retrorally.R
import com.example.retrorally.databinding.FragmentAuthorizationBinding
import com.example.retrorally.ui.main.viewmodel.SharedViewModel

class AuthorizationFragment : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()
    private var binding: FragmentAuthorizationBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        startObserve()
        binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.nextButton?.setOnClickListener { checkPassword() }
    }

    private fun startObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner) {
            if (it) {
                binding?.progressBar?.isVisible = it
            } else {
                binding?.progressBar?.isVisible = it
            }
        }
        viewModel.error.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.requireContext(), it, Toast.LENGTH_SHORT).show()
            binding?.password?.setText("")
        }
        viewModel.contestData.observe(this.viewLifecycleOwner) {
            //set data in new fragment
            goToNextScreen()
        }
    }

    private fun checkPassword() {
        if (binding?.password?.text.toString() == "") {
            Toast.makeText(this.requireContext(), "Введите пароль!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.onButtonClick()
        }
    }

    private fun goToNextScreen() {
        findNavController().navigate(R.id.action_authorizationFragment_to_judgeFragment)
    }
}