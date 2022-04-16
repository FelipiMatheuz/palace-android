package org.felipimz.palace.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.felipimz.palace.R
import org.felipimz.palace.model.Preferences

class PreferencesViewModel(context: Context) : ViewModel() {

    var preferences: Preferences

    init {
        val preferencesFile = context.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)

        preferences = Preferences(
            preferencesFile.getString("nickname", "Player")!!,
            preferencesFile.getBoolean("deckWithJoker", true),
            preferencesFile.getBoolean("doubleDeck", true),
            preferencesFile.getBoolean("wildcardAsSpecial", false),
            preferencesFile.getString("rules", "default")!!,
            preferencesFile.getInt("card", 0)
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
            0 -> R.drawable.blue_card
            1 -> R.drawable.red_card
            2 -> R.drawable.farwest_card
            3 -> R.drawable.nature_card
            4 -> R.drawable.tech_card
            5 -> R.drawable.royal_card
            else -> R.drawable.blue_card
        }
        return deckResource
    }

    fun loadRules(): String {
        return preferences.rules
    }

    fun loadCard(): Int {
        return preferences.card
    }

    fun loadDoubleDeck(): Boolean {
        return preferences.doubleDeck
    }
}