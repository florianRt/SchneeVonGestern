package com.sabbelkrabbe.schneevongestern.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.sql.RowId

@Dao
interface CityDao {

    @Query("SELECT DISTINCT rowid, name, country_name from cities where ascii_name like '%'||:query||'%' order by population DESC limit 5")
    fun getSuggestions(query: String): List<SuggestionCity>

    @Insert
    suspend fun insertAll(vararg cities: City)

    @Delete
    suspend fun delete(user: City)

    @Query("SELECT cities.* FROM cities")
    fun getAll(): List<City>

    @Query("Select ascii_name from cities where rowid = :rowId")
    fun getCity(rowId: Int): String
}