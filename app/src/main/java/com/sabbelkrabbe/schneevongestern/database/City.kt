package com.sabbelkrabbe.schneevongestern.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class City(
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "ascii_name") val asciiName: String?,
    @ColumnInfo(name = "alternate_names") val altNames: String?,
    @ColumnInfo(name = "country_name") val countryName: String?,
    @ColumnInfo(name = "population") val population: Int?
){
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid")
    var rowid: Int? = null;

    fun setId(id: Int) = run { rowid=id }
}

