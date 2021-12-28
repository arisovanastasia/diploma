package com.example.retrorally

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.data.models.Participant
import com.example.retrorally.databinding.DialogLayoutBinding
import com.example.retrorally.databinding.FragmentJudgeBinding


class JudgeFragment : Fragment() {

    private var mainBinding: FragmentJudgeBinding? = null
    private lateinit var adapter: DataAdapter
    private lateinit var resultList: ArrayList<Participant>
    private var myComment: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)
        return mainBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        resultList = ArrayList()
        val recycler: RecyclerView = view.findViewById(R.id.results_recycler)

        //set adapter
        adapter = DataAdapter(this.requireContext(), resultList)

        //set Recycler view adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupCarNumberInput()
        mainBinding?.mainView?.addNewItemButton?.setOnClickListener { addNewItem() }

        mainBinding?.submitButton?.setOnClickListener {
            findNavController().navigate(R.id.action_judgeFragment_to_finalFragment)
        }
        mainBinding?.mainView?.commentButton?.setOnClickListener {
            writeComment()
        }
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
            sending.setOnClickListener {
                mainBinding?.mainView?.car?.text = mainBinding?.enterCar?.number?.text
                mainBinding?.enterCar?.number?.text = ""
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

    private fun addNewItem() {
        val num = mainBinding?.mainView?.car?.text.toString()
        val res = mainBinding?.mainView?.resultText?.text.toString()
        val com = myComment
        if (num != "") {
            resultList.add(0, Participant(num, res, com))
            adapter.notifyDataSetChanged()

            mainBinding?.mainView?.car?.text = ""
            mainBinding?.mainView?.resultText?.text?.clear()
            myComment = ""
        } else {
            val toast = Toast.makeText(context, R.string.error, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun writeComment() {

        val dialogBinding = DialogLayoutBinding.inflate(layoutInflater)
        val input = dialogBinding.inputMessage

        AlertDialog.Builder(requireContext())
            .setTitle("Комментарий")
            .setView(input)
            .setPositiveButton("OK") { dialog, id ->
                myComment = input.text.toString()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, id ->
                dialog.cancel()
            }
            .create()
            .show()
    }


}