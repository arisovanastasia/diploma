package com.example.retrorally.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mbed.coap.exception.CoapCodeException
import com.mbed.coap.packet.Code
import com.mbed.coap.server.CoapExchange
import com.mbed.coap.server.CoapHandler
import com.mbed.coap.server.CoapServer
import com.mbed.coap.utils.CoapResource
import java.util.*

class SensorsViewModel : ViewModel() {
    private val _sensorsLiveData = MutableLiveData<SensorsData>()
    val sensorsLiveData: MutableLiveData<SensorsData> = _sensorsLiveData
    private var server: CoapServer? = null

    override fun onCleared() {
        super.onCleared()
        server?.stop()
    }

    fun startCoAPServer() {
        // put initial empty value
        _sensorsLiveData.value = SensorsData()

        if(server == null){
            // create a CoAP server and listen to incoming data
            // TODO: add some more handlers
            server = CoapServer.builder().transport(5683).build()
            val timeHandler: CoapHandler = TimeCoapResource()
            server?.addRequestHandler("/time/*", timeHandler)
            server?.start()
        }
    }

    inner class TimeCoapResource : CoapResource() {
        // here we should do something with sensors data
        private var body = "Hello World"

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseBody(body)
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {
            // here we can use anything from ex, such as:
            ex.remoteAddress
            ex.requestBody
            ex.requestUri

            val value = _sensorsLiveData.value

            if(value != null) {
                value.time2 = Calendar.getInstance().time
                _sensorsLiveData.postValue(value!!)
            }

            body = ex.requestBodyString
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    class SensorsData {
        var cones = 0 // количество сбитых конусов
        var buttons = 0 // количество нажатых кнопок

        var time1 : Date = Date(0) // первый датчик времени (время старта)
        var time2 : Date = Date(0) // второй датчик времени (время финиша)
    }
}