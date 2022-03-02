package com.example.retrorally.ui.main.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.R
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.databinding.DialogLayoutBinding
import com.example.retrorally.databinding.FragmentJudgeBinding
import com.example.retrorally.ui.main.adapters.DataAdapter
import com.example.retrorally.ui.main.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar


class JudgeFragment : Fragment() {

    private var mainBinding: FragmentJudgeBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: DataAdapter
    private lateinit var resultList: ArrayList<Participant>
    private var myComment = ""
    private var idOfProtocol = 0
    private var origId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)
        observeData()
        return mainBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        resultList = ArrayList()
        val recycler: RecyclerView = view.findViewById(R.id.results_recycler)
        //set adapter
        adapter = DataAdapter(this.requireContext(), resultList) { participant, position ->
            onParticipantChanged(participant, position)
        }
        //set Recycler view adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupCarNumberInput()
        onClickListeners()
    }

    private fun observeData() {
        viewModel.loading.observe(this.viewLifecycleOwner) {
            mainBinding?.progressBar?.isVisible = it
        }
        viewModel.error.observe(this.viewLifecycleOwner) {
            val toast = Toast.makeText(
                this.requireContext(),
                it,
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
        viewModel.contestLiveData.observe(this.viewLifecycleOwner) {
            setContestDataToViews(it)
        }
        viewModel.participantsLiveData.observe(this.viewLifecycleOwner) {
            if (it.isEmpty()) {
                val toast = Toast.makeText(
                    this.requireContext(),
                    "Протокол готов к заполнению!",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                adapter.setData(it)
            }
        }
    }

    private fun setContestDataToViews(data: ContestDataDTO) {
        mainBinding?.startTime?.text = viewModel.getLocalTime(data.timeToStart)
        mainBinding?.endTime?.text = viewModel.getLocalTime(data.timeToEnd)
        mainBinding?.sector?.text =
            getString(R.string.number_of_sector).plus(" ").plus(data.nameOfArea)
        mainBinding?.descriptionView?.text = data.description
        viewModel.setInitialParticipantLiveData(data.usersProtocol)
        idOfProtocol = data.id
    }

    private fun onClickListeners() {
        mainBinding?.mainView?.addNewItemButton?.setOnClickListener {
            setParticipantDataIntoViews(it)
        }
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
                mainBinding?.mainView?.car?.apply {
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
            mainBinding?.mainView?.car?.apply {
                text = text.toString() + num
            }
        }
    }

    private fun setParticipantDataIntoViews(view: View) {
        if (mainBinding?.mainView?.car?.text.toString().isNotEmpty()) {
            viewModel.postParticipant(
                origId,
                idOfProtocol,
                mainBinding?.mainView?.car?.text.toString(),
                mainBinding?.mainView?.resultText?.text.toString(),
                myComment
            )
            mainBinding?.mainView?.car?.text = ""
            mainBinding?.mainView?.resultText?.text?.clear()
            myComment = ""
        } else {
            createSnack(view)
        }
    }

    private fun onParticipantChanged(participant: Participant, targetPosition: Int) {
        viewModel.postParticipant(
            participant.idOfString,
            idOfProtocol,
            participant.participant,
            participant.result,
            participant.comment,
            targetPosition
        )
    }

    private fun createSnack(view: View) {
        val snack = Snackbar.make(view, R.string.error, 2000)
        snack.setBackgroundTint(resources.getColor(R.color.orange_light, null))
        snack.setTextColor(resources.getColor(R.color.green_dark, null))
        val snackView = snack.view
        val params: FrameLayout.LayoutParams =
            snackView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL
        snackView.layoutParams = params
        snack.show()
    }

    private fun writeComment() {
        val dialogBinding = DialogLayoutBinding.inflate(layoutInflater)
        val input = dialogBinding.inputMessage
        val pastComment = myComment

        AlertDialog.Builder(requireContext())
            .setTitle("Комментарий")
            .setView(input)
            .setPositiveButton("OK") { dialog, id ->
                myComment = pastComment + input.text.toString()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, id ->
                dialog.cancel()
            }
            .create()
            .show()
    }


}