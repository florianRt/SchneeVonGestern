package com.sabbelkrabbe.schneevongestern

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import androidx.room.Room
import com.sabbelkrabbe.schneevongestern.database.AppDatabase
import com.sabbelkrabbe.schneevongestern.database.SuggestionCity
import com.sabbelkrabbe.schneevongestern.interfaces.ConnectToMainActivity

class SearchSuggestionProvider: ContentProvider() {
    private val TAG = "SearchSuggestionProvider"

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor {
        val query: String = p0.lastPathSegment?.lowercase().toString()
        val cursor: Cursor

        val suggestions: List<SuggestionCity> = ConnectToMainActivity().getInstance().db.cityDao().getSuggestions(query)//getSuggestions("name")
        val columns = arrayOf("_ID", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA)

        val matrixCursor = MatrixCursor(columns)
        Log.d(TAG, "query: ${suggestions.size}")

        for (i in suggestions){
            matrixCursor.addRow(arrayOf(i.id,
                i.name + ", " + i.countryName!!.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                i.id
            ))
        }

        cursor = matrixCursor

        return cursor
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }
}