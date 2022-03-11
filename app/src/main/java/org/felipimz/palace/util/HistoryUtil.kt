package org.felipimz.palace.util

import android.content.Context
import org.felipimz.palace.R

fun getPlayerPosition(context: Context, position: Int): String {
    return when (position) {
        1 -> "$position${context.resources.getString(R.string.first)}"
        2 -> "$position${context.resources.getString(R.string.second)}"
        3 -> "$position${context.resources.getString(R.string.third)}"
        else -> "$position${context.resources.getString(R.string.ordinal)}"
    }
}

fun getColorRes(position: Int): Int {
    return when (position) {
        1 -> R.color.gold
        2 -> R.color.silver
        3 -> R.color.bronze
        else -> R.color.white
    }
}

fun getGameMode(context: Context, gamemode: String): String {
    return when (gamemode) {
        "single" -> context.resources.getString(R.string.singleplayer)
        "multi" -> context.resources.getString(R.string.multiplayer)
        else -> ""
    }
}

fun getMatchDate(matchDate: String): String {
    return matchDate.substring(matchDate.indexOf(" ")).trim()
}