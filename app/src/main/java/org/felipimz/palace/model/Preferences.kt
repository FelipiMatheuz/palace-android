package org.felipimz.palace.model

data class Preferences(
    var nickname: String = "",
    var deckWithJoker: Boolean = false,
    var rules: String = "default",
    var card: String = "blue"
)
