package com.sabbelkrabbe.schneevongestern.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.room.Room
import com.android.volley.RequestQueue
import com.sabbelkrabbe.schneevongestern.dataClasses.WeatherData
import com.sabbelkrabbe.schneevongestern.database.AppDatabase

open class ConnectToMainActivity {
    interface OnChangeListener {
        fun updateText()
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: ConnectToMainActivity? = null
    }

    private lateinit var wData: WeatherData
    private lateinit var mListener: OnChangeListener
    lateinit var db: AppDatabase
    var context: Context? = null
    var activity: Activity? = null
    var sharedPref: SharedPreferences? = null
    var queueTag: String? = null
    var requestQueue: RequestQueue? = null

    fun init(context: Context, listener: OnChangeListener, activity: Activity, sharedPref: SharedPreferences) {
        this.context = context
        setListener(listener)
        this.activity = activity
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "cities.db"
        )
            .createFromAsset("cities.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        this.sharedPref = sharedPref
    }

    fun getInstance(): ConnectToMainActivity {
        if (mInstance == null) {
            mInstance = ConnectToMainActivity()
        }
        return mInstance as ConnectToMainActivity
    }

    fun setListener(listener: OnChangeListener) {
        mListener = listener
    }

    fun setStrings(wData: WeatherData) {
        this.wData = wData
    }

    fun getStrings(): WeatherData {
        return wData
    }

    fun updateText() {
        mListener.updateText()
    }
}
