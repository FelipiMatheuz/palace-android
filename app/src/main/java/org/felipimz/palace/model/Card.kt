package org.felipimz.palace.model

data class Card(
    var name: String = "",
    var value: Int = 0,
    var wildCard: WildCardEffect = WildCardEffect.NONE,
    var clicked: Boolean = false
)
