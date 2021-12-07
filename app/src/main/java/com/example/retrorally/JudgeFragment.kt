package com.example.retrorally

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.retrorally.databinding.FragmentJudgeBinding


class JudgeFragment : Fragment() {

    private var mainBinding: FragmentJudgeBinding? = null
    private val one = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)
        return mainBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCarNumberInput()
    }

    private fun setupCarNumberInput() {
        mainBinding?.keyboard?.apply {
            one.setupNumberInputButton(1)
            two.setupNumberInputButton(2)
            three.setupNumberInputButton(3)
            four.setupNumberInputButton(4)
            five.setupNumberInputButton(5)
            six.setupNumberInputButton(6)
            seven.setupNumberInputButton(7)
            eight.setupNumberInputButton(8)
            nine.setupNumberInputButton(9)
            nul.setupNumberInputButton(0)
            cancel.setOnClickListener {
                mainBinding?.enterCar?.number?.apply {
                    if (text.isNotEmpty()) {
                        text = text.toString()
                            .substring(startIndex = 0, endIndex = text.toString().length - 1)
                    }
                }
            }
        }
    }

    private fun Button.setupNumberInputButton(num: Int) {
        setOnClickListener {
            mainBinding?.enterCar?.number?.apply {
                text = text.toString() + num
            }
        }
    }

}