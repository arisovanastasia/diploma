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
            server = CoapServer.builder().transport(5683).build()
            val testHandler: CoapHandler = TestCoapResource()
            server?.addRequestHandler("/test", testHandler)

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

    fun reset() {
        val value = _sensorsLiveData.value
        value?.cones_and_buttons?.clear()
        value?.square?.clear()
        value?.stop_line = 0
        _sensorsLiveData.postValue(value!!)
    }

    inner class TestCoapResource : CoapResource() {
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
            // ex.remoteAddress
            // ex.requestBody
            // ex.requestUri

            body = ex.requestBodyString
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class HBCoapResource : CoapResource() {

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {
            val value = _sensorsLiveData.value
            val id = ex.requestUri.split("/").elementAtOrNull(2)

            if (value != null && id != null) {
                value.heartbeats[id] = Calendar.getInstance().time
                _sensorsLiveData.postValue(value!!)
            }

            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class TMCoapResource : CoapResource() {

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value
            val id = ex.requestUri.split("/").elementAtOrNull(2)
            val body = ex.requestBody

            //нам тут хватает и 3-х байтов (0 - 4278255615), а с полными 4-мя будет переполнение
            if(value != null && id != null) {
                val time = body[0].toLong() shl 24 or (body[1].toLong() and 0xFF) shl 16 or
                        (body[2].toLong() and 0xFF) shl 8 or (body[3].toLong() and 0xFF)

                if(id.startsWith("st")) {
                    value.time1 = time
                } else if (id.startsWith("fi")) {
                    value.time2 = time
                }

                _sensorsLiveData.postValue(value!!)
            }
            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class HTCoapResource : CoapResource() {

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value
            val id = ex.requestUri.split("/").elementAtOrNull(2)
            val body = ex.requestBody

            if(value != null && id != null) {
                value.cones_and_buttons[id] = when (body[0].toInt() and 1) {
                    0 -> false
                    else -> true
                }
                _sensorsLiveData.postValue(value!!)
            }

            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class SLCoapResource : CoapResource() {

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value
            val id = ex.requestUri.split("/").elementAtOrNull(2)
            val body = ex.requestBody

            if(value != null && id != null) {
                when {
                    body[0].toInt() and 1 != 0 -> value.stop_line = 1
                    body[0].toInt() and 2 != 0 -> value.stop_line = 2
                    body[0].toInt() and 4 != 0 -> value.stop_line = 3
                }
                _sensorsLiveData.postValue(value!!)
            }

            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    inner class SQCoapResource : CoapResource() {

        @Throws(CoapCodeException::class)
        override fun get(ex: CoapExchange) {
            ex.setResponseCode(Code.C205_CONTENT)
            ex.sendResponse()
        }

        @Throws(CoapCodeException::class)
        override fun put(ex: CoapExchange) {

            val value = _sensorsLiveData.value
            val id = ex.requestUri.split("/").elementAtOrNull(2)
            val body = ex.requestBody

            if(value != null && id != null) {
                value.square[id] = body[0] //body[0].toInt() shl 24
                _sensorsLiveData.postValue(value!!)
            }


            ex.setResponseCode(Code.C204_CHANGED)
            ex.sendResponse()
        }
    }

    class SensorsData {
        val heartbeats : HashMap<String, Date> = HashMap()
        val square : HashMap<String, Byte> = HashMap()
        val cones_and_buttons : HashMap<String, Boolean> = HashMap()

        var stop_line = 0

        var time1 : Long = 0 // первый датчик времени (время старта)
        var time2 : Long = 0 // второй датчик времени (время финиша)
    }
}