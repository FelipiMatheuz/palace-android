package org.felipimz.palace.model

data class Pilar(
    var position: Position = Position.LEFT,
    var card: Card = Card(),
    var faced: Faced = Faced.UP
)
