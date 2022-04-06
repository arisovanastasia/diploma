package com.example.retrorally.ui.main.viewmodel

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

            //TODO: uri */<sensor id>
            val hbHandler: CoapHandler = HBCoapResource()
            server?.addRequestHandler("/hb/*", hbHandler)

            val tmHandler: CoapHandler = TMCoapResource()
            server?.addRequestHandler("/tm/*", tmHandler)

            val htHandler: CoapHandler = HTCoapResource()
            server?.addRequestHandler("/ht/*", htHandler)

            val slHandler: CoapHandler = SLCoapResource()
            server?.addRequestHandler("/sl/*", slHandler)

            val sqHandler: CoapHandler = SQCoapResource()
            server?.addRequestHandler("/sq/*", sqHandler)

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

    inner class HBCoapResource : CoapResource() {
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
            body = ex.requestBodyString
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class TMCoapResource : CoapResource() {
        private var body = byteArrayOf(0x00, 0x00, 0x00, 0x00)

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {

            val value = _sensorsLiveData.value

            if(value != null) {
                value.time = body[0].toInt() shl 24 or (body[1].toInt() and 0xFF) shl 16 or
                        (body[2].toInt() and 0xFF) shl 8 or (body[3].toInt() and 0xFF)
                _sensorsLiveData.postValue(value!!)
            }

            ex.setResponseBody(body)
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {
            body = ex.requestBody
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class HTCoapResource : CoapResource() {
        private var body = byteArrayOf(0x00, 0x00, 0x00, 0x00)

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseBody(body)
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value

            if(value != null) {
                value.time = 0
                _sensorsLiveData.postValue(value!!)
            }

            body = ex.requestBody
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class SLCoapResource : CoapResource() {
        private var body = byteArrayOf(0x00)

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseBody(body)
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value

            if(value != null) {
                when {
                    body[0].toInt() and (1 shl 1) == 1 -> value.stop_line = 1
                    body[0].toInt() and (1 shl 2) == 1 -> value.stop_line = 2
                    body[0].toInt() and (1 shl 3) == 1 -> value.stop_line = 3
                }
                _sensorsLiveData.postValue(value!!)
            }

            body = ex.requestBody
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class SQCoapResource : CoapResource() {
        private var body = byteArrayOf(0x00)

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseBody(body)
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value

            if(value != null) {
                when {
                    body[0].toInt() and (1 shl 1) == 1 -> value.inner_s = true
                    body[0].toInt() and (1 shl 2) == 1 -> value.outer_s = true
                }
                _sensorsLiveData.postValue(value!!)
            }

            body = ex.requestBody
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    class SensorsData {
        var cones = 0 // количество сбитых конусов
        var buttons = 0 // количество нажатых кнопок
        var stop_line = 0
        var inner_s = false
        var outer_s = false

        var time = 0
        var time1 : Date = Date(0) // первый датчик времени (время старта)
        var time2 : Date = Date(0) // второй датчик времени (время финиша)
    }
}