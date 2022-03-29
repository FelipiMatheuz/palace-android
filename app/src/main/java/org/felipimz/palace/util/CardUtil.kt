package org.felipimz.palace.util

import org.felipimz.palace.model.Card
import org.felipimz.palace.model.WildCardEffect

class CardUtil {

    private val deck: MutableList<Card> = mutableListOf(
        Card("two_of_diamonds", 2, WildCardEffect.RESET),
        Card("three_of_diamonds", 3),
        Card("four_of_diamonds", 4),
        Card("five_of_diamonds", 5),
        Card("six_of_diamonds", 6),
        Card("seven_of_diamonds", 7, WildCardEffect.FORCEDOWN),
        Card("eight_of_diamonds", 8),
        Card("nine_of_diamonds", 9),
        Card("ten_of_diamonds", 10, WildCardEffect.BURNPILE),
        Card("jack_of_diamonds", 11),
        Card("queen_of_diamonds", 12),
        Card("king_of_diamonds", 13),
        Card("ace_of_diamonds", 14),

        Card("two_of_spades", 2, WildCardEffect.RESET),
        Card("three_of_spades", 3),
        Card("four_of_spades", 4),
        Card("five_of_spades", 5),
        Card("six_of_spades", 6),
        Card("seven_of_spades", 7, WildCardEffect.FORCEDOWN),
        Card("eight_of_spades", 8),
        Card("nine_of_spades", 9),
        Card("ten_of_spades", 10, WildCardEffect.BURNPILE),
        Card("jack_of_spades", 11),
        Card("queen_of_spades", 12),
        Card("king_of_spades", 13),
        Card("ace_of_spades", 14),

        Card("two_of_hearts", 2, WildCardEffect.RESET),
        Card("three_of_hearts", 3),
        Card("four_of_hearts", 4),
        Card("five_of_hearts", 5),
        Card("six_of_hearts", 6),
        Card("seven_of_hearts", 7, WildCardEffect.FORCEDOWN),
        Card("eight_of_hearts", 8),
        Card("nine_of_hearts", 9),
        Card("ten_of_hearts", 10, WildCardEffect.BURNPILE),
        Card("jack_of_hearts", 11),
        Card("queen_of_hearts", 12),
        Card("king_of_hearts", 13),
        Card("ace_of_hearts", 14),

        Card("two_of_clubs", 2, WildCardEffect.RESET),
        Card("three_of_clubs", 3),
        Card("four_of_clubs", 4),
        Card("five_of_clubs", 5),
        Card("six_of_clubs", 6),
        Card("seven_of_clubs", 7, WildCardEffect.FORCEDOWN),
        Card("eight_of_clubs", 8),
        Card("nine_of_clubs", 9),
        Card("ten_of_clubs", 10, WildCardEffect.BURNPILE),
        Card("jack_of_clubs", 11),
        Card("queen_of_clubs", 12),
        Card("king_of_clubs", 13),
        Card("ace_of_clubs", 14)
    )

    fun getDeckDefault(hasJoker: Boolean): MutableList<Card> {
        if (hasJoker) {
            val eights = deck.filter { f ->
                f.name.contains("eight")
            }
            eights.forEach {
                val eightWithwildCardEffect = it
                eightWithwildCardEffect.wildCard = WildCardEffect.REVERSE
                deck[deck.indexOf(it)] = eightWithwildCardEffect
            }
        } else {
            deck.add(
                Card("joker", 15, WildCardEffect.REVERSE)
            )
            deck.add(
                Card("joker", 15, WildCardEffect.REVERSE)
            )
        }
        return deck
    }
}
