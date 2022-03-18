package org.felipimz.palace.repository

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.felipimz.palace.R
import org.felipimz.palace.model.Preferences

class PreferencesRepository(context: Context) : ViewModel() {

    var preferences: Preferences

    init {
        val preferencesFile = context.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)

        preferences = Preferences(
            preferencesFile.getString("nickname", "")!!,
            preferencesFile.getBoolean("deckWithJoker", false),
            preferencesFile.getBoolean("wildcardAsSpecial", false),
            preferencesFile.getString("rules", "default")!!,
            preferencesFile.getString("card", "blue")!!
        )
    }

    fun loadNickName(): String {
        return preferences.nickname
    }

    fun loadDeckWithJoker(): Boolean {
        return preferences.deckWithJoker
    }

    fun loadWildCardAsSpecial(): Boolean {
        return preferences.wildcardAsSpecial
    }

    fun loadDeck(): Int {
        val deckResource = when (preferences.card) {
            "blue" -> R.drawable.blue_card
            "red" -> R.drawable.red_card
            else -> R.drawable.blue_card
        }
        return deckResource
    }

    fun loadRules(): String {
        return preferences.rules
    }

    fun loadCard(): String {
        return preferences.card
    }
}