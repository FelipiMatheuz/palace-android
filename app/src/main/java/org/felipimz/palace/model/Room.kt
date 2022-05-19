package org.felipimz.palace.model

data class Room(
    var name: String = "",
    var password: String = "",
    var members: MutableList<String> = mutableListOf(),
    var deckWithJokerMultiplayer: Boolean = true,
    var wildcardAsSpecialMultiplayer: Boolean = false,
    var status: Status = Status.WAIT
)
