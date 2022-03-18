package org.felipimz.palace.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.felipimz.palace.model.History

class HistoryRepository(context: Context) : ViewModel() {

    private var historyList: MutableList<History>
    private var historyFile: SharedPreferences

    init {
        historyFile = context.getSharedPreferences("history", AppCompatActivity.MODE_PRIVATE)
        val history = historyFile.getString("records", "")!!
        historyList = if (history.isNotEmpty()) {
            jacksonObjectMapper().readValue(history)
        } else {
            mutableListOf()
        }
    }

    fun getHistoryList(): List<History> {
        return historyList
    }

    fun setHistoryList(history: History) {
        historyList.add(history)
        val editor = historyFile.edit()
        editor.putString("records", jacksonObjectMapper().writeValueAsString(historyList))
        editor.apply()
    }
}