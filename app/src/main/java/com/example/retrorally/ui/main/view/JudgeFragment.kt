package com.example.retrorally.ui.main.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.retrorally.ui.main.viewmodel.SharedViewModel
import com.example.retrorally.ui.main.viewmodel.SensorsViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.content.Intent
import android.content.BroadcastReceiver
import android.bluetooth.BluetoothAdapter
import android.os.Looper
import android.os.Looper.*
import android.widget.*
import androidx.core.view.children
import com.example.retrorally.data.models.dto.ResultsDTO
import com.example.retrorally.databinding.LineDataLayoutBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap

class JudgeFragment : Fragment() {

    private var mainBinding: FragmentJudgeBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()
    private val sensorsViewModel: SensorsViewModel by activityViewModels()
    private lateinit var adapter: DataAdapter
    private lateinit var resultList: ArrayList<Participant>

    private var idOfProtocol = 0
    private var origId = 0

    private lateinit var inputs: List<String>
    private lateinit var fastComments: List<String>

    private lateinit var mLbrBinder: LbrService.LbrBinder
    private var mLbrBound: Boolean = false
    private var scanJob: Job? = null

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            mLbrBinder = binder as LbrService.LbrBinder
            mLbrBound = true

            scanJob = CoroutineScope(Dispatchers.IO).launch {
                val foundList : HashMap<BluetoothDevice, Short> = HashMap()
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent) {
                        val action = intent.action

                        //Finding devices
                        if (BluetoothDevice.ACTION_FOUND == action) {
                            // Get the BluetoothDevice object from the Intent
                            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                            if (device?.name?.startsWith("MIEM") == true) {
                                foundList.put(device, rssi)
                            }
                        }
                    }
                }
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                context?.registerReceiver(
                    receiver,
                    filter
                )

                while(true) {
                    foundList.clear()
                    bluetoothAdapter.startDiscovery()
                    Thread.sleep(1000);
                    bluetoothAdapter.cancelDiscovery() // we are advised to do so before attempting to connect

                    // find the device with strongest signal
                    var best = foundList.maxByOrNull { it.value }
                    if (best != null){
                        mLbrBinder.connectBluetoothDevice(best.key);
                    }

                    Thread.sleep(20 * 1000)
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            scanJob?.cancel()
            mLbrBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentJudgeBinding.inflate(inflater, container, false)

        observeData()

        return mainBinding?.root
    }

    private fun getServiceIntent(): Intent {
        return Intent(context, LbrService::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            context?.startService(getServiceIntent().setAction(LbrService.ACTION_CONNECT))

            // Also bind to the service to get control of it
            context?.bindService(getServiceIntent(), connection, Context.BIND_AUTO_CREATE);
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        context?.startService(getServiceIntent().setAction(LbrService.ACTION_DISCONNECT))
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        resultList = ArrayList()

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
        sensorsViewModel.sensorsLiveData.observe(this.viewLifecycleOwner) {
            // TODO: do something
        }
    }

    companion object { // TODO: seems that this must become a fragment
        fun adjustVisibility(inputs: List<String>, fastComments: List<String>, lineData: LineDataLayoutBinding?, showButtons: Boolean = true) {
            if (inputs.contains("time_hm")) {
                lineData?.timeHmInputs?.visibility = View.VISIBLE
            } else {
                lineData?.timeHmInputs?.visibility = View.GONE
            }

            if (inputs.contains("time_hms")) {
                lineData?.timeHmsInputs?.visibility = View.VISIBLE
            } else {
                lineData?.timeHmsInputs?.visibility = View.GONE
            }

            if (inputs.contains("set_time")) {
                lineData?.setTimeInputs?.visibility = View.VISIBLE
            } else {
                lineData?.setTimeInputs?.visibility = View.GONE
            }

            if (inputs.contains("start_time")) {
                lineData?.startTimeInputs?.visibility = View.VISIBLE
            } else {
                lineData?.startTimeInputs?.visibility = View.GONE
            }

            if (inputs.contains("finish_time")) {
                lineData?.finishTimeInputs?.visibility = View.VISIBLE
            } else {
                lineData?.finishTimeInputs?.visibility = View.GONE
            }

            if (inputs.contains("cones")) {
                lineData?.conesInputs?.visibility = View.VISIBLE
            } else {
                lineData?.conesInputs?.visibility = View.GONE
            }

            if (inputs.contains("buttons")) {
                lineData?.buttonsInputs?.visibility = View.VISIBLE
            } else {
                lineData?.buttonsInputs?.visibility = View.GONE
            }

            if (inputs.contains("stop_line")) {
                lineData?.stopLineInputs?.visibility = View.VISIBLE
            } else {
                lineData?.stopLineInputs?.visibility = View.GONE
            }

            if (inputs.contains("base")) {
                lineData?.base?.visibility = View.VISIBLE
            } else {
                lineData?.base?.visibility = View.GONE
            }

            if (inputs.contains("scheme")) {
                lineData?.scheme?.visibility = View.VISIBLE
            } else {
                lineData?.scheme?.visibility = View.GONE
            }

            for (comment in fastComments) {
                val commentCheckBox = CheckBox(lineData?.root?.context)
                commentCheckBox.text = comment
                commentCheckBox.isChecked = false
                lineData?.fastComments?.addView(commentCheckBox)
            }

            if (!showButtons) {
                lineData?.timeHmsEnter?.visibility = View.INVISIBLE
                lineData?.startTimeEnter?.visibility = View.INVISIBLE
                lineData?.finishTimeEnter?.visibility = View.INVISIBLE

                lineData?.fastComments?.visibility = View.GONE
            }
        }

        fun getResult(lineData: LineDataLayoutBinding?): ResultsDTO {
            var time_hm : String? = null
            if(lineData?.timeHmInputs?.visibility == View.VISIBLE) {
                time_hm = lineData.timeHm.text.toString()
            }

            var time_hms : String? = null
            if(lineData?.timeHmsInputs?.visibility == View.VISIBLE) {
                time_hms= lineData.timeHms.text.toString()
            }

            var set_time : String? = null
            if(lineData?.setTimeInputs?.visibility == View.VISIBLE) {
                set_time= lineData.setTime.text.toString()
            }

            var start_time : String? = null
            if(lineData?.startTimeInputs?.visibility == View.VISIBLE) {
                start_time= lineData.startTime.text.toString()
            }

            var finish_time : String? = null
            if(lineData?.finishTimeInputs?.visibility == View.VISIBLE) {
                finish_time= lineData.finishTime.text.toString()
            }

            var cones : Int? = null
            if(lineData?.conesInputs?.visibility == View.VISIBLE) {
                cones= lineData.cones.value
            }

            var buttons : Int? = null
            if(lineData?.buttonsInputs?.visibility == View.VISIBLE) {
                buttons = lineData.buttons.value
            }

            var stop_line : Int? = null
            if(lineData?.stopLineInputs?.visibility == View.VISIBLE) {
                stop_line = lineData.stopLine.selectedItemPosition
            }

            var base : Int? = null
            if(lineData?.base?.visibility == View.VISIBLE) {
                if (lineData.base.isChecked ) {
                    base = 1
                } else {
                    base = 0
                }
            }

            var scheme : Int? = null
            if(lineData?.scheme?.visibility == View.VISIBLE) {
               if (lineData.scheme.isChecked) {
                   scheme = 1
               } else {
                   scheme = 0
               }
            }

            return ResultsDTO(
                time_hm,
                time_hms,
                set_time,
                start_time,
                finish_time,
                cones,
                buttons,
                null,
                stop_line,
                base,
                scheme
            );
        }

        fun getCarNumber(lineData: LineDataLayoutBinding?): String {
            return lineData?.car?.text.toString()
        }

        fun getComment(lineData: LineDataLayoutBinding?): String {
            var r = lineData?.comment?.text.toString()

            for(commentView in lineData?.fastComments?.children!!){
                val commentCheckBox = commentView as CheckBox

                if (commentCheckBox.isChecked) {
                    r += " " + commentCheckBox.text
                }
            }

            return r;
        }

        fun clearLineData(lineData: LineDataLayoutBinding?) {
            lineData?.car?.text?.clear()
            lineData?.timeHm?.text?.clear()
            lineData?.timeHms?.text?.clear()
            lineData?.setTime?.text?.clear()
            lineData?.startTime?.text?.clear()
            lineData?.finishTime?.text?.clear()

            lineData?.cones?.text?.clear()
            lineData?.buttons?.text?.clear()

            lineData?.stopLine?.setSelection(0)

            lineData?.base?.setChecked(false)
            lineData?.scheme?.setChecked(false)

            lineData?.comment?.text?.clear()

            for(commentView in lineData?.fastComments?.children!!){
                val commentCheckBox = commentView as CheckBox
                commentCheckBox.setChecked(false)
            }
        }

        fun fillInputs(participant: Participant, lineData: LineDataLayoutBinding?) {
            lineData?.car?.setText(participant.participant)

            if(participant.result.time_hm != null){
                lineData?.timeHm?.setText(participant.result.time_hm)
            }

            if(participant.result.time_hms != null){
                lineData?.timeHms?.setText(participant.result.time_hms)
            }

            if(participant.result.set_time != null){
                lineData?.setTime?.setText(participant.result.set_time)
            }

            if(participant.result.start_time != null){
                lineData?.startTime?.setText(participant.result.start_time)
            }

            if(participant.result.finish_time != null){
                lineData?.finishTime?.setText(participant.result.finish_time)
            }

            if(participant.result.cones != null){
                lineData?.cones?.setValue(participant.result.cones)
            }

            if(participant.result.buttons != null){
                lineData?.buttons?.setValue(participant.result.buttons)
            }

            if(participant.result.stop_line != null){
                lineData?.stopLine?.setSelection(participant.result.stop_line!!)
            }

            if(participant.result.base != null){
                lineData?.base?.setChecked(participant.result.base!! != 0)
            }

            if(participant.result.scheme != null){
                lineData?.scheme?.setChecked(participant.result.scheme!! != 0)
            }

            lineData?.comment?.setText(participant.comment)

            for(commentView in lineData?.fastComments?.children!!){
                val commentCheckBox = commentView as CheckBox
                commentCheckBox.setChecked(false)
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

        inputs = data.inputs
        fastComments = data.fastComments

        adjustVisibility(inputs, fastComments, mainBinding?.lineData);

        if(data.hasSensors) {
            // Start a service to work with sensors
            // We need this for BLE scan permissions;
            // Setup bluetooth beacon detection and automatic connection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    context?.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    val builder = AlertDialog.Builder(this.context)
                    builder.setTitle("This app needs location access")
                    builder.setMessage("Please grant location access so this app can detect beacons")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), 1
                        )
                    }
                    builder.show()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (context?.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    val builder = AlertDialog.Builder(this.context)
                    builder.setTitle("This app needs BLE scan access")
                    builder.setMessage("Please grant BLE scan access so this app can detect beacons")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                            1
                        )
                    }
                    builder.show()
                }
            }

            // Start a 6LoWPAN VPN-like service here
            // TODO: should it be run here, or in some more convenient class? where we should create it?
            val intent = VpnService.prepare(context)
            if (intent != null) {
                startActivityForResult(intent, 0)
            } else {
                onActivityResult(0, Activity.RESULT_OK, null)
            }

            sensorsViewModel.startCoAPServer() // does nothing if server already started

            // Показать кнопки сброса и вывода таблицы маршрутизации
        }

        val recycler: RecyclerView? = mainBinding?.mainView?.resultsRecycler

        //set adapter
        adapter = DataAdapter(this.requireContext(), resultList, inputs, fastComments) { participant, position ->
            onParticipantChanged(participant, position)
        }
        //set Recycler view adapter
        recycler?.layoutManager = LinearLayoutManager(requireContext())
        recycler?.adapter = adapter
    }

    private fun onClickListeners() {
        mainBinding?.addNewItemButton?.setOnClickListener {
            if (mainBinding?.lineData?.car?.text.toString().isNotEmpty()) {
                setParticipantDataIntoViews(it)
            } else {
                createSnack(it)
            }
        }
        mainBinding?.lineData?.timeHmsEnter?.setOnClickListener {
            val time = Calendar.getInstance().time
            val sdf = SimpleDateFormat("HH:mm:ss")
            mainBinding?.lineData?.timeHms?.setText(sdf.format(time))
            //setParticipantDataIntoViews(it) // TODO: Кириллу не надо ругаться на приход данных без partcipant ID (или просто пока не отправлять?)
        }
        mainBinding?.lineData?.startTimeEnter?.setOnClickListener {
            val time = Calendar.getInstance().time
            val sdf = SimpleDateFormat("HH:mm:ss")
            mainBinding?.lineData?.startTime?.setText(sdf.format(time))
        }
        mainBinding?.lineData?.finishTimeEnter?.setOnClickListener {
            val time = Calendar.getInstance().time
            val sdf = SimpleDateFormat("HH:mm:ss")
            mainBinding?.lineData?.finishTime?.setText(sdf.format(time))
        }
        mainBinding?.submitButton?.setOnClickListener {
            findNavController().navigate(R.id.action_judgeFragment_to_finalFragment)

            /* val dialogView = TextView(context)
            dialogView.setText(mLbrBinder?.printRoutingTable())
            AlertDialog.Builder(context)
                .setTitle("Таблица маршрутизации")
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, id ->
                    dialog.dismiss()
                }
                .create()
                .show() */
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
                mainBinding?.lineData?.car?.apply {
                    if (text.isNotEmpty()) {
                        setText( getText().toString()
                            .substring(startIndex = 0, endIndex = getText().toString().length - 1)
                        )
                    }
                }
            }
        }
    }

    private fun Button.setupNumberInputButton(num: Int) {
        setOnClickListener {
            mainBinding?.lineData?.car?.apply {
                setText( getText().toString() + num )
            }
        }
    }

    private fun setParticipantDataIntoViews(view: View) {
        viewModel.postParticipant(
            origId,
            idOfProtocol,
            getCarNumber(mainBinding?.lineData),
            getResult(mainBinding?.lineData),
            getComment(mainBinding?.lineData)
        )
        clearLineData(mainBinding?.lineData)
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

}