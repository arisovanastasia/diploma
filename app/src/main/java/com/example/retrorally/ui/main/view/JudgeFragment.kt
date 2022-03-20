package com.example.retrorally.ui.main.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.util.Log
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
import com.example.ipspapplication.LbrService
import com.example.retrorally.R
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.databinding.DialogLayoutBinding
import com.example.retrorally.databinding.FragmentJudgeBinding
import com.example.retrorally.ui.main.adapters.DataAdapter
import com.example.retrorally.ui.main.adapters.TestAdapter
import com.example.retrorally.ui.main.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet6Address
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
    private var idOfProtocol = 0
    private var origId = 0

    //private lateinit var bestNetworkCallback: BestNetworkCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)

        // Start a 6LoWPAN VPN-like service here
        // TODO: should it be run here, or in some more convenient class? where we should create it?
        val intent = VpnService.prepare(context)
        if (intent != null) {
            startActivityForResult(intent, 0)
        } else {
            onActivityResult(0, Activity.RESULT_OK, null)
        }

        // run a separate thread for networking
        CoroutineScope(Dispatchers.IO).launch{
            // Add a socket to test passing packets to VPN
            //Thread.sleep(10000)
            val s = DatagramSocket(1234)
            s.soTimeout = 20; // a 20 ms timeout to receive something

            var i = 0;

            while(true) {
                val byteArray = ("test" + i.toString()).toByteArray()
                val addr = Inet6Address.getByName("2001:d8::2")
                val p = DatagramPacket(byteArray, byteArray.size, addr, 666)

                try {
                    s.send(p)
                } catch (e : java.io.IOException){
                    // do nothing
                    Log.i("udp", "Send failed " + e.message)
                }

                try {
                    s.receive(p)
                } catch (e : java.net.SocketTimeoutException) {
                    // do nothing
                    Log.i("udp", "Receive timeout expired")
                } catch (e : java.io.IOException) {
                    // do nothing
                    Log.i("udp", "Receive failed " + e.message)
                }

                Thread.sleep(1000)
                i++
            }
        }

        viewModel.startCoAPServer() // does nothing if server already started
        observeData()

        return mainBinding?.root
    }

    private fun getServiceIntent(): Intent {
        return Intent(context, LbrService::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            context?.startService(getServiceIntent().setAction(LbrService.ACTION_CONNECT))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        context?.startService(getServiceIntent().setAction(LbrService.ACTION_DISCONNECT))
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        resultList = ArrayList()
        testList = ArrayList()
        val recycler: RecyclerView = view.findViewById(R.id.results_recycler)
        val testRecycler: RecyclerView = view.findViewById(R.id.test_recycler)
        //set adapter
        adapter = DataAdapter(this.requireContext(), resultList) { participant, position ->
            onParticipantChanged(participant, position)
        }
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
        viewModel.sensorsLiveData.observe(this.viewLifecycleOwner) {
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