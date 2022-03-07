package com.example.retrorally.ui.main.view

import android.os.Bundle
import android.view.Gravity
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
        binding?.nextButton?.setOnClickListener { checkPassword(binding?.passwordEditText?.text.toString()) }
    }

    private fun startObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner) {
            binding?.progressBar?.isVisible = it
        }
        viewModel.error.observe(this.viewLifecycleOwner) {
            val toast = Toast.makeText(this.requireContext(), "Пользователь не найден, повторите попытку!", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            binding?.passwordEditText?.setText("")
        }
        viewModel.contestLiveData.observe(this.viewLifecycleOwner) {
            goToNextScreen()
        }
    }

    private fun checkPassword(password: String) {
        if (password == "") {
            val toast = Toast.makeText(this.requireContext(), "Введите пароль!", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()
        } else {
            viewModel.onButtonClick(password)
        }
    }

    private fun goToNextScreen() {
        findNavController().navigate(R.id.action_authorizationFragment_to_judgeFragment)
    }
}