package org.felipimz.palace.model

data class Preferences(
    var nickname: String = "Player",
    var deckWithJoker: Boolean = true,
    var doubleDeck: Boolean = true,
    var wildcardAsSpecial: Boolean = false,
    var rules: String = "default",
    var card: Int = 0,
)
