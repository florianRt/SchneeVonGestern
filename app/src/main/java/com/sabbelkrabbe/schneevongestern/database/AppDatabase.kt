package com.sabbelkrabbe.schneevongestern.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sabbelkrabbe.schneevongestern.interfaces.ConnectToMainActivity

@Database(entities = [City::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}