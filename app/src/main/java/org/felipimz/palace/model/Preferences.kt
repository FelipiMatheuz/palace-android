package org.felipimz.palace.model

data class Preferences(
    var nickname: String = "Player",
    var deckWithJoker: Boolean = true,
    var rules: String = "default",
    var card: String = "blue"
)
