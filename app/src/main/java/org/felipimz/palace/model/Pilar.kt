package org.felipimz.palace.model

data class Pilar(
    var position: Position = Position.left,
    var card: Card = Card(),
    var faced: Faced = Faced.up
)
