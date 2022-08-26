package org.felipimz.palace.model

data class Room(
    var id: String = "",
    var name: String = "",
    var password: String = "",
    var members: MutableList<Member> = mutableListOf(),
    var deckWithJokerMultiplayer: Boolean = true,
    var wildcardAsSpecialMultiplayer: Boolean = false,
    var status: Status = Status.OPEN,
    var deck: MutableList<Card> = mutableListOf()
)
