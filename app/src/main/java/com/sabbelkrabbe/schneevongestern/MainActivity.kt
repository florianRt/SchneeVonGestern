package com.sabbelkrabbe.schneevongestern

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.sabbelkrabbe.schneevongestern.databinding.ActivityMainBinding
import com.sabbelkrabbe.schneevongestern.interfaces.ConnectToMainActivity
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), ConnectToMainActivity.OnChangeListener {

    private lateinit var binding: ActivityMainBinding

    var fa: Activity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEARCH)
            Intent("finish_activity").apply { sendBroadcast(this) }
        startReceiver()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        ConnectToMainActivity().getInstance().init(this, this, this, sharedPreferences)

        setContentView(R.layout.loading_screen)

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        if (sharedPref.getBoolean(getString(R.string.key_first_start), true)) {
            firstStart()
        } else {
            startWithIntent(intent)
        }

    }

    private fun startReceiver() {
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish_activity") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish_activity"))
    }

    private fun startWithIntent(intent: Intent) {
        when {
            Intent.ACTION_SEARCH == intent.action -> {
                Log.d(TAG, "startWithIntent: User Search")
                intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                    FetchData(this, this).getData(query)
                }
            }
            Intent.ACTION_VIEW == intent.action -> {
                Log.d(TAG, "startWithIntent: View Search")
                FetchData(this, this).getData(
                    ConnectToMainActivity().getInstance().db.cityDao()
                        .getCity(intent.data.toString().toInt())
                )
            }
            else -> {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val city =
                    if (sharedPreferences.getBoolean(getString(R.string.settings_key_use_standard),
                            false)
                    ) {
                        sharedPreferences.getString(getString(R.string.settings_key_last_city),
                            getString(R.string.settings_standard_city))
                    } else {
                        sharedPreferences.getString(getString(R.string.settings_cityKey),
                            "Berlin")
                    }
                FetchData(this, this).getData(city!!)
            }
        }
    }

    private fun firstStart() {
        startActivity(Intent(this, SettingsActivity::class.java))
        val tempLayout = findViewById<ConstraintLayout>(R.id.loading_screen)
        tempLayout.post {
            val height = tempLayout.height
            val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt(getString(R.string.key_height), height)
                putBoolean(getString(R.string.key_first_start), false)
                apply()
            }
            Log.d(TAG, "onCreate: firstStart")
            FetchData(this, this).getData(getString(R.string.settings_standard_city))
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.menu_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
//            setTheme(R.style.SearchView)
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            maxWidth = width
            isIconifiedByDefault = true // Do not iconify the widget; expand it by default
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        val conn = ConnectToMainActivity().getInstance()
        if (conn.requestQueue != null && conn.queueTag != null)
            conn.requestQueue!!.cancelAll(conn.queueTag)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun updateText() {
        Log.d(TAG, "updateText: Doing this")
        val data = ConnectToMainActivity().getInstance().getStrings()
        loadMainWindow()

        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(data.datetime, pattern)

        binding.txtDate.text =
            date.dayOfMonth.toString() + ". " + getMonthString(date.monthValue) + " " + date.year + ", " + data.address
        binding.txtTavg.text = data.temp.roundToInt().toString()
        binding.txtTmin.text = "${getString(R.string.at_night)} ${data.tempmin.roundToInt()}°"
        binding.txtTmax.text = "${getString(R.string.at_day)} ${data.tempmax.roundToInt()}°"
        binding.txtFeelslike.text =
            "${getString(R.string.feelslike_temp)}${data.feelslike.roundToInt()}°"
        binding.txtWspd.text = "${data.windspeed.roundToInt()}"
        binding.txtWpgt.text =
            "${getString(R.string.max_wind_speed)}${data.windgust.roundToInt()}${getString(R.string.velocity_unit)}"
        binding.txtPrcp.text =
            if (data.precip >= 10) "${data.precip.roundToInt()}" else "${data.precip}"
        binding.txtConditions.text = getWeatherConditionString(data.conditions)
        binding.imgWdir.rotation = data.winddir.toFloat() + 180f
        binding.txtHumidity.text = data.humidity.toString() + "%"
        binding.txtPres.text = data.pressure.toString() + " mbar"
        binding.txtDew.text = data.dew.toString() + "°C"
        binding.txtUvIndex.text =
            getUVIndexText(data.uvindex) + ", " + data.uvindex.roundToInt().toString()
        binding.txtVisibility.text =
            data.visibility.roundToInt().toString() + " " + getString(R.string.distance_unit)

        binding.txtSunrise.text = data.sunrise.substring(0, 5)
        binding.txtSunset.text = data.sunset.substring(0, 5)

        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        val time1 = simpleDateFormat.parse(data.sunrise)
        val time2 = simpleDateFormat.parse(data.sunset)
        val diff: Duration = Duration.between(time1!!.toInstant(), time2!!.toInstant())
        val formattedDiff: String = java.lang.String.format(
            Locale.ENGLISH,
            "%d Std. %d Min. ", diff.toHoursPart(), diff.toMinutesPart()
        )
        binding.txtDayLength.text = formattedDiff

        binding.imgWeatherIcon.setImageDrawable(
            getWeatherIconDrawable(
                data.conditions,
                data.snowdepth
            )
        )

        calcMoonPhase(data.moonphase)
    }

    private fun getWeatherConditionString(conditions: String): String {

        val cons = getStringList(conditions)
        var r = ""
        if ("type_18" in cons || "type_37" in cons) {
            r = getString(R.string.thunderstorm)
        } else if ("type_38" in cons) {
            r = getString(R.string.thunderstorm_less_precip)
        } else if ("type_1" in cons || "type_39" in cons || "type_35" in cons || "type_34" in cons || "type_33" in cons || "type_31" in cons) {
            r = getString(R.string.snow)
        } else if ("type_9" in cons || "type_10" in cons || "type_11" in cons || "type_13" in cons || "type_14" in cons || "type_22" in cons || "type_23" in cons || "type_32" in cons) {
            r = getString(R.string.sleet)
        } else if ("type_5" in cons || "type_6" in cons || "type_20" in cons || "type_21" in cons || "type_24" in cons || "type_25" in cons) {
            r = getString(R.string.rain)
        } else if ("type_2" in cons || "type_3" in cons || "type_4" in cons) {
            r = getString(R.string.drizzle)
        } else if ("type_17" in cons || "type_16" in cons || "type_40" in cons) {
            r = getString(R.string.hail)
        } else if ("type_7" in cons || "type_15" in cons || "type_36" in cons) {
            r = getString(R.string.storm)
        } else if ("type_28" in cons || "type_41" in cons) {
            r = getString(R.string.cloudy)
        } else if ("type_27" in cons) {
            r = getString(R.string.mostly_cloudy)
        } else if ("type_42" in cons) {
            r = getString(R.string.partly_cloudy)
        } else if ("type_43" in cons) {
            r = getString(R.string.clear)
        } else if ("type_8" in cons || "type_12" in cons || "type_19" in cons || "type_30" in cons) {
            r = getString(R.string.fog)
        }

        return r
    }

    private fun getStringList(conditions: String): List<String> {
        var con = conditions
        val list = mutableListOf("")
        while (con.isNotEmpty() && con.contains(",")) {
            list.add(con.substring(0, con.indexOf(",")))
            con = con.substring(con.indexOf(",") + 2)
        }
        list[0] = con
        return list
    }

    private fun getWeatherIconDrawable(conditions: String, snowdepth: Double): Drawable? {
        var id = R.drawable.unknown
        val cons = getStringList(conditions)
        if ("type_18" in cons || "type_37" in cons) {
            id = R.drawable.tstorms
        } else if ("type_38" in cons) {
            id = R.drawable.chancetstorms
        } else if ("type_1" in cons || "type_39" in cons || "type_35" in cons || "type_34" in cons || "type_33" in cons || "type_31" in cons) {
            id = R.drawable.chancesnow
            binding.txtPrcp.text = snowdepth.roundToInt().toString()
            binding.imgPrcp.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.snow,
                    null
                )
            )
        } else if ("type_9" in cons || "type_10" in cons || "type_11" in cons || "type_13" in cons || "type_14" in cons || "type_22" in cons || "type_23" in cons || "type_32" in cons) {
            id = R.drawable.snow_rain
        } else if ("type_5" in cons || "type_6" in cons || "type_20" in cons || "type_21" in cons || "type_24" in cons || "type_25" in cons) {
            id = R.drawable.rain
        } else if ("type_2" in cons || "type_3" in cons || "type_4" in cons) {
            id = R.drawable.chancerain
        } else if ("type_17" in cons || "type_16" in cons || "type_40" in cons) {
            id = R.drawable.flurries
        } else if ("type_7" in cons || "type_15" in cons || "type_36" in cons) {
            id = R.drawable.wind
        } else if ("type_28" in cons || "type_41" in cons) {
            id = R.drawable.cloudy
        } else if ("type_27" in cons) {
            id = R.drawable.mostlycloudy
        } else if ("type_42" in cons) {
            id = R.drawable.mostlysunny
        } else if ("type_43" in cons) {
            id = R.drawable.clear
        } else if ("type_8" in cons || "type_12" in cons || "type_19" in cons || "type_30" in cons) {
            id = R.drawable.fog
        }

        return ResourcesCompat.getDrawable(resources, id, null)
    }

    private fun getUVIndexText(uvIndex: Double): String {
        return when (uvIndex.roundToInt()) {
            in 0..2 -> getString(R.string.uvindex_low)
            in 3..5 -> getString(R.string.uvindex_moderate)
            6, 7 -> getString(R.string.uvindex_high)
            in 8..9 -> getString(R.string.uvindex_very_high)
            else -> getString(R.string.uvindex_extreme)
        }
    }

    private fun getMonthString(monthValue: Int): String {
        return when (monthValue) {
            1 -> getString(R.string.january)
            2 -> getString(R.string.february)
            3 -> getString(R.string.march)
            4 -> getString(R.string.april)
            5 -> getString(R.string.may)
            6 -> getString(R.string.june)
            7 -> getString(R.string.july)
            8 -> getString(R.string.august)
            9 -> getString(R.string.september)
            10 -> getString(R.string.october)
            11 -> getString(R.string.november)
            12 -> getString(R.string.december)
            else -> ""
        }
    }

    private fun calcMoonPhase(moonphase: Double) {

        if (moonphase == 0.0) {
            binding.txtMoon.text = getString(R.string.new_moon)
        } else if (moonphase < 0.25) {
            binding.txtMoon.text = getString(R.string.waxing_crescent)
        } else if (moonphase == 0.25) {
            binding.txtMoon.text = getString(R.string.first_quarter)
        } else if (moonphase < 0.5) {
            binding.txtMoon.text = getString(R.string.waxing_gibbous)
        } else if (moonphase == 0.5) {
            binding.txtMoon.text = getString(R.string.full_moon)
        } else if (moonphase < 0.75) {
            binding.txtMoon.text = getString(R.string.waning_gibbous)
        } else if (moonphase == 0.75) {
            binding.txtMoon.text = getString(R.string.last_quarter)
        } else if (moonphase < 1.0) {
            binding.txtMoon.text = getString(R.string.waning_crescent)
        } else {
            binding.txtMoon.text = getString(R.string.error)
        }

        val drawable = if (moonphase == 0.0) ResourcesCompat.getDrawable(
            resources,
            R.drawable.moon_outline,
            null
        ) else ResourcesCompat.getDrawable(resources, R.drawable.moon, null)
        var bitmap = (drawable as BitmapDrawable).bitmap

        val newLayoutParams = ConstraintLayout.LayoutParams(binding.imgMoon.layoutParams)
        val width: Int
        var marginLeft = getPxForDp(20f)
        if (moonphase == 0.0) {
            width = 400
        } else if (moonphase < 0.5) {
            val tempMP = moonphase * 2
            Log.d(
                TAG,
                "calcMoonPhase: $moonphase, $tempMP, ${(bitmap.width * tempMP).roundToInt()}"
            )
            bitmap = Bitmap.createBitmap(
                bitmap!!,
                0,
                0,
                (bitmap.width * tempMP).roundToInt(),
                bitmap.height
            )
            width = (400 * tempMP).roundToInt()
        } else if (moonphase == 0.5) {
            width = 400
        } else {
            val tempMP = (moonphase - 0.5) * 2
            val tempWidth = bitmap.width - bitmap.width * tempMP
            Log.d(
                TAG,
                "calcMoonPhase: $moonphase, $tempMP, ${(tempWidth).roundToInt()}, ${(bitmap.width * tempMP).roundToInt()}"
            )
            bitmap = Bitmap.createBitmap(
                bitmap!!,
                (bitmap.width * tempMP).roundToInt(),
                0,
                tempWidth.roundToInt(),
                bitmap.height
            )
            width = (400 - 400 * tempMP).roundToInt()

            marginLeft = (getPxForDp(20f) + (400 * tempMP)).roundToInt()
            Log.d(
                TAG,
                "calcMoonPhase: ${(400 * tempMP).roundToInt()}, ${getPxForDp(20 + (400 * tempMP).toFloat())}"
            )
        }
        val img: Drawable = BitmapDrawable(resources, bitmap)

        newLayoutParams.leftToLeft = ConstraintSet.PARENT_ID
        newLayoutParams.topToBottom = binding.lineMoonInfo.id
        newLayoutParams.leftMargin = marginLeft
        newLayoutParams.topMargin = getPxForDp(50f)
        newLayoutParams.height = 400
        newLayoutParams.width = width
        binding.imgMoon.layoutParams = newLayoutParams
        binding.imgMoon.background = img
    }

    private fun getPxForDp(messurement: Float): Int {
        val r: Resources = this.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            messurement,
            r.displayMetrics
        ).toInt()
    }

    private fun loadMainWindow() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.root.apply {
            setContentView(this)
        }

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return

        Log.d(TAG, "loadMainWindow: doing this")
        binding.backgroundMainInfo.layoutParams.height =
            sharedPref.getInt(getString(R.string.key_height), 500)
        binding.backgroundDetailedInfo.layoutParams.height =
            sharedPref.getInt(getString(R.string.key_height), 500)

        binding.backgroundMainInfo.post {
            binding.backgroundMainInfo.layoutParams.height =
                sharedPref.getInt(getString(R.string.key_height), 500)
        }

        binding.backgroundDetailedInfo.post {
            binding.backgroundDetailedInfo.layoutParams.height =
                sharedPref.getInt(getString(R.string.key_height), 500)
        }
    }

}


