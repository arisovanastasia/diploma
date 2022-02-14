package com.example.retrorally.ui.main.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android6lbr.Autoconnect
import com.example.retrorally.R
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.databinding.DialogLayoutBinding
import com.example.retrorally.databinding.FragmentJudgeBinding
import com.example.retrorally.ui.main.adapters.DataAdapter
import com.example.retrorally.ui.main.adapters.TestAdapter
import com.example.retrorally.ui.main.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class JudgeFragment : Fragment() {

    private var mainBinding: FragmentJudgeBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: DataAdapter
    private lateinit var testAdapter: TestAdapter
    private lateinit var resultList: ArrayList<Participant>
    private lateinit var testList: MutableList<String>
    private var myComment = ""

    private lateinit var autoconnect: Autoconnect

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)

        // TODO: REMOVE IT
        // TEMPORARY, FOR POSTER SESSION ONLY
        autoconnect = Autoconnect(activity)
        // TEMPORARY, FOR POSTER SESSION ONLY
        // TODO: REMOVE IT

        observeData()

        return mainBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        resultList = ArrayList()
        testList = ArrayList()
        val recycler: RecyclerView = view.findViewById(R.id.results_recycler)
        val testRecycler: RecyclerView = view.findViewById(R.id.test_recycler)
        //set adapter
        adapter = DataAdapter(this.requireContext(), resultList)
        testAdapter = TestAdapter(this.requireContext(), testList) {
            mainBinding?.mainView?.resultText?.setText(it)
        }
        //set Recycler view adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        testRecycler.layoutManager = LinearLayoutManager(requireContext())
        testRecycler.adapter = testAdapter

        setupCarNumberInput()
        onClickListeners()
    }

    private fun observeData() {
        viewModel.contestLiveData.observe(this.viewLifecycleOwner) {
            setContestDataToViews(it)
        }
        viewModel.participantsLiveData.observe(this.viewLifecycleOwner) {
            adapter.setData(it)
        }
        autoconnect.liveData.observe(this.viewLifecycleOwner) {
            val timeNow = Calendar.getInstance().time
            val sdf = SimpleDateFormat("HH:mm:ss")
            postTimeToList(sdf.format(timeNow))
        }
    }

    private fun postTimeToList(time: String) {
        testList.add(time)
        val newList = testList
        setDataTimeFromSensors(newList)
    }

    private fun setDataTimeFromSensors(listOfTimes: MutableList<String>) {
        testAdapter.setTestData(listOfTimes)
    }

    private fun setContestDataToViews(data: ContestDataDTO) {
        mainBinding?.startTime?.text = viewModel.getLocalTime(data.timeToStart)
        mainBinding?.endTime?.text = viewModel.getLocalTime(data.timeToEnd)
        mainBinding?.sector?.text = data.nameOfArea
        mainBinding?.descriptionView?.text = data.description
        viewModel.setInitialParticipantLiveData(data.usersProtocol)
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
        if (mainBinding?.mainView?.car?.text.toString() != "") {
            viewModel.addItemToLiveData(
                mainBinding?.mainView?.car?.text.toString(),
                mainBinding?.mainView?.resultText?.text.toString(),
                myComment
            )
            mainBinding?.mainView?.car?.text = ""
            mainBinding?.mainView?.resultText?.text?.clear()
            myComment
        } else {
            createSnack(view)
        }
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