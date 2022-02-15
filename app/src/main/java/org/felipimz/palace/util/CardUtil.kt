package org.felipimz.palace.util

import org.felipimz.palace.model.Card
import org.felipimz.palace.model.WildCardEffect

class CardUtil {

    private val deck: MutableList<Card> = mutableListOf(
        Card("two_of_diamonds", 2, WildCardEffect.resetValue),
        Card("three_of_diamonds", 3, WildCardEffect.none),
        Card("four_of_diamonds", 4, WildCardEffect.none),
        Card("five_of_diamonds", 5, WildCardEffect.none),
        Card("six_of_diamonds", 6, WildCardEffect.none),
        Card("seven_of_diamonds", 7, WildCardEffect.forceDown),
        Card("eight_of_diamonds", 8, WildCardEffect.none),
        Card("nine_of_diamonds", 9, WildCardEffect.none),
        Card("ten_of_diamonds", 10, WildCardEffect.burnPile),
        Card("jack_of_diamonds", 11, WildCardEffect.none),
        Card("queen_of_diamonds", 12, WildCardEffect.none),
        Card("king_of_diamonds", 13, WildCardEffect.none),
        Card("ace_of_diamonds", 14, WildCardEffect.none),

        Card("two_of_spades", 2, WildCardEffect.resetValue),
        Card("three_of_spades", 3, WildCardEffect.none),
        Card("four_of_spades", 4, WildCardEffect.none),
        Card("five_of_spades", 5, WildCardEffect.none),
        Card("six_of_spades", 6, WildCardEffect.none),
        Card("seven_of_spades", 7, WildCardEffect.forceDown),
        Card("eight_of_spades", 8, WildCardEffect.none),
        Card("nine_of_spades", 9, WildCardEffect.none),
        Card("ten_of_spades", 10, WildCardEffect.burnPile),
        Card("jack_of_spades", 11, WildCardEffect.none),
        Card("queen_of_spades", 12, WildCardEffect.none),
        Card("king_of_spades", 13, WildCardEffect.none),
        Card("ace_of_spades", 14, WildCardEffect.none),

        Card("two_of_hearts", 2, WildCardEffect.resetValue),
        Card("three_of_hearts", 3, WildCardEffect.none),
        Card("four_of_hearts", 4, WildCardEffect.none),
        Card("five_of_hearts", 5, WildCardEffect.none),
        Card("six_of_hearts", 6, WildCardEffect.none),
        Card("seven_of_hearts", 7, WildCardEffect.forceDown),
        Card("eight_of_hearts", 8, WildCardEffect.none),
        Card("nine_of_hearts", 9, WildCardEffect.none),
        Card("ten_of_hearts", 10, WildCardEffect.burnPile),
        Card("jack_of_hearts", 11, WildCardEffect.none),
        Card("queen_of_hearts", 12, WildCardEffect.none),
        Card("king_of_hearts", 13, WildCardEffect.none),
        Card("ace_of_hearts", 14, WildCardEffect.none),

        Card("two_of_clubs", 2, WildCardEffect.resetValue),
        Card("three_of_clubs", 3, WildCardEffect.none),
        Card("four_of_clubs", 4, WildCardEffect.none),
        Card("five_of_clubs", 5, WildCardEffect.none),
        Card("six_of_clubs", 6, WildCardEffect.none),
        Card("seven_of_clubs", 7, WildCardEffect.forceDown),
        Card("eight_of_clubs", 8, WildCardEffect.none),
        Card("nine_of_clubs", 9, WildCardEffect.none),
        Card("ten_of_clubs", 10, WildCardEffect.burnPile),
        Card("jack_of_clubs", 11, WildCardEffect.none),
        Card("queen_of_clubs", 12, WildCardEffect.none),
        Card("king_of_clubs", 13, WildCardEffect.none),
        Card("ace_of_clubs", 14, WildCardEffect.none)
    )

    fun getDeckDefault(): MutableList<Card> {
        return deck
    }

    fun getDeckWithJoker(): MutableList<Card> {
        deck.add(
            Card("joker", 1, WildCardEffect.reverse)
        )
        deck.add(
            Card("joker", 1, WildCardEffect.reverse)
        )
        return deck
    }
}