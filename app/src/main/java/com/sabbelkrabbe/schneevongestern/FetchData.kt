package com.sabbelkrabbe.schneevongestern

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sabbelkrabbe.schneevongestern.dataClasses.WeatherData
import com.sabbelkrabbe.schneevongestern.interfaces.ConnectToMainActivity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FetchData(private val activity: Activity, private val mainActivity: MainActivity) :
    Activity() {

    private fun isDataStored(city: String): Boolean {

        //check if city file exists
        val files: Array<String> = ConnectToMainActivity().getInstance().context!!.fileList()
        return files.contains("${city.lowercase()}-${getCurrentTime()}")
    }

    fun getData(city: String) {
        if (isDataStored(city)) {
            val file = File(ConnectToMainActivity().getInstance().context!!.filesDir,
                "${city.lowercase()}-${getCurrentTime()}")

            val mapper = jacksonObjectMapper()

            val wData: WeatherData = mapper.readValue(file)
            ConnectToMainActivity().getInstance().setStrings(wData)
            ConnectToMainActivity().getInstance().updateText()
            Log.d(TAG, "getData: loaded file from storage")
        } else {
            getPerHttpRequest(city)
        }
        val sharedPref = ConnectToMainActivity().getInstance().sharedPref!!

        with(sharedPref.edit()) {
            putString(ConnectToMainActivity().getInstance().context!!.getString(R.string.settings_key_last_city), city)
            apply()
        }

    }

    private fun getPerHttpRequest(city: String) {
        val TAG = "HttpRequest"

        val apiKey = "X6SUR2ZV2A7ZN38MDUBECAVRD"
        val url =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/yesterday?" +
                    "unitGroup=metric&include=days&key=$apiKey&contentType=json&lang=id&values=icons2"

        Log.d(TAG, "URL: $url")
        val queue = Volley.newRequestQueue(activity.applicationContext)
        val queueTag = "weatherTag-$city"

        val conn = ConnectToMainActivity().getInstance()

        conn.queueTag = queueTag
        conn.requestQueue = queue

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            Log.d(TAG, "getPerHttpRequest: test")
            val wData = JSONParser().parse(response.toString())

            conn.setStrings(wData)
            conn.updateText()
            saveWeatherData(city, wData)
            Log.d(ContentValues.TAG, "getData: downloaded file")
        },
            {
                //https://stackoverflow.com/a/52816937/15796676 error handling
                Log.d(TAG, "Volley failed")
                Log.d(TAG, "getPerHttpRequest: ${it.networkResponse.statusCode}")

                Toast.makeText(ConnectToMainActivity().getInstance().context!!,
                    ConnectToMainActivity().getInstance().context!!.getString(R.string.city_not_existing),
                    Toast.LENGTH_LONG).show()
            }
        )

        jsonObjectRequest.tag = queueTag
        queue.add(jsonObjectRequest)
    }

    private fun saveWeatherData(city: String, wData: WeatherData) {
        val mapper = jacksonObjectMapper()
        val serialized = mapper.writeValueAsString(wData)

        ConnectToMainActivity().getInstance().context!!.openFileOutput("${city.lowercase()}-${getCurrentTime()}",
            MODE_PRIVATE).use {
            it.write(serialized.toByteArray())
        }
        cleanUpStorage()
    }

    private fun getCurrentTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return current.format(formatter)
    }

    private fun cleanUpStorage() {
        val files: Array<String> = ConnectToMainActivity().getInstance().context!!.fileList()
        val time = getCurrentTime()

        for (file in files)
            if (file.substring(file.indexOf("-") + 1) < time)
                File(ConnectToMainActivity().getInstance().context!!.filesDir, file).delete()
    }
}