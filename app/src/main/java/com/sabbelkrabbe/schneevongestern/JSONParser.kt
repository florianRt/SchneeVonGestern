package com.sabbelkrabbe.schneevongestern

import com.sabbelkrabbe.schneevongestern.dataClasses.WeatherData

class JSONParser {

    private val keywords: Array<String> = arrayOf(
        "\"address\"",
        "\"datetime\"",
        "\"tempmax\"",
        "\"tempmin\"",
        "\"temp\"",
        "\"feelslike\"",
        "\"dew\"",
        "\"humidity\"",
        "\"precip\"",
        "\"preciptype\"",
        "\"snow\"",
        "\"snowdepth\"",
        "\"windgust\"",
        "\"windspeed\"",
        "\"winddir\"",
        "\"pressure\"",
        "\"cloudcover\"",
        "\"visibility\"",
        "\"uvindex\"",
        "\"sunrise\"",
        "\"sunset\"",
        "\"conditions\"",
        "\"icon\"",
        "\"moonphase\""
    )
    private var json: String? = null

    fun parse(json: String): WeatherData {
        this.json = json
        return WeatherData(
            filterForInfo(keywords[0]),
            filterForInfo(keywords[1]),
            filterForDouble(keywords[2]),
            filterForDouble(keywords[3]),
            filterForDouble(keywords[4]),
            filterForDouble(keywords[5]),
            filterForDouble(keywords[6]),
            filterForDouble(keywords[7]),
            filterForDouble(keywords[8]),
            filterForList(keywords[9]),
            filterForDouble(keywords[10]),
            filterForDouble(keywords[11]),
            filterForDouble(keywords[12]),
            filterForDouble(keywords[13]),
            filterForDouble(keywords[14]),
            filterForDouble(keywords[15]),
            filterForDouble(keywords[16]),
            filterForDouble(keywords[17]),
            filterForDouble(keywords[18]),
            filterForInfo(keywords[19]),
            filterForInfo(keywords[20]),
            filterForInfo(keywords[21]),
            filterForInfo(keywords[22]),
            filterForDouble(keywords[23])
        )
    }

    private fun filterForInfo(key: String, default: String? = ""): String {
        val offset = 2
        if (json != null) {
            when {
                json!!.substring(json!!.indexOf(key) + key.length + offset).contains("\"") -> {
                    val r = json!!.substring(
                        json!!.indexOf(key) + key.length + offset,
                        json!!.substring(json!!.indexOf(key) + key.length + offset)
                            .indexOf("\"") + json!!.indexOf(key) + key.length + offset
                    )
                    return r
                }
                else -> {
                    return json!!.substring(
                        json!!.indexOf(key) + key.length + offset,
                        json!!.substring(json!!.indexOf(key) + key.length + offset)
                            .indexOf("}") + json!!.indexOf(key) + key.length + offset
                    ).replace("\"", "")
                }
            }
        }
        return default ?: ""
    }

    private fun filterForDouble(key: String, default: Double? = 0.0): Double {
        val offset = 1
        if (json != null) {
            when {
                json!!.substring(json!!.indexOf(key) + key.length + offset).contains(",") -> {
                    val r = json!!.substring(
                        json!!.indexOf(key) + key.length + offset,
                        json!!.substring(json!!.indexOf(key) + key.length + offset)
                            .indexOf(",") + json!!.indexOf(key) + key.length + offset
                    ).toDoubleOrNull()
                    return r ?: 0.0
                }
                else -> {
                    val r = json!!.substring(
                        json!!.indexOf(key) + key.length + offset,
                        json!!.substring(json!!.indexOf(key) + key.length + offset)
                            .indexOf("}") + json!!.indexOf(key) + key.length + offset
                    ).replace("\"", "").toDoubleOrNull()
                    return r ?: 0.0
                }
            }
        }
        return default ?: 0.0
    }

    private fun filterForList(key: String, default: List<String>? = null): List<String>? {
        val offset = 2
        val list = mutableListOf("")

        if (json != null) {
            var txt = json!!.substring(
                json!!.indexOf(key) + key.length + offset,
                json!!.indexOf("\"stations\"")
            )
            if (txt.contains("[") || txt.contains("]")) {
                txt = txt.substring(txt.indexOf("[") + 1, txt.indexOf("]"))
                while (txt.contains(",")) {
                    list.add(
                        txt.substring(0, txt.indexOf(",") + 1).replace("\"", "").replace(",", "")
                    )
                    txt = txt.substring(txt.indexOf(",") + 1)
                }
                list[0] = txt.replace("\"", "")
                return list
            }
        }
        return default
    }
}