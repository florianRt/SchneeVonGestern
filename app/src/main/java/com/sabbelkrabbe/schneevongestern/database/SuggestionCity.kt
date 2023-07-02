package com.sabbelkrabbe.schneevongestern.database

import androidx.room.ColumnInfo

data class SuggestionCity(
    @ColumnInfo(name = "rowid") val id: Int?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "country_name") val countryName: String?
)
