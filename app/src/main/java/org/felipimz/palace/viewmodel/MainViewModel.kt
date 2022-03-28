package org.felipimz.palace.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Owner
import org.felipimz.palace.model.Position
import org.felipimz.palace.util.CardUtil

class MainViewModel : ViewModel() {

    //global deck
    val deck = MutableLiveData<MutableList<Card>>()
    private val cardUtil = CardUtil()
    var currentTurn: Int = 1

    init {
        deck.value = mutableListOf()
    }

    fun distributeCard(deckWithJoker: Boolean) {
        //init deck and shuffle
        deck.value = cardUtil.getDeckDefault(deckWithJoker)

        deck.value = deck.value?.shuffled()?.toMutableList()

        var cardIndex = 0
        for (card in deck.value!!) {
            //distribute on hands
            if (cardIndex < 3) {
                card.position = Position.HAND
                card.owner = Owner.PLAYER1
            } else if (cardIndex < 6) {
                card.position = Position.HAND
                card.owner = Owner.PLAYER2
            } else if (cardIndex < 9) {
                card.position = Position.HAND
                card.owner = Owner.PLAYER3
            } else if (cardIndex < 12) {
                card.position = Position.HAND
                card.owner = Owner.PLAYER4
                //distribute on table
            } else {
                break
            }
            cardIndex++
        }
        //distribute the rest
        cardIndex = distributeTable(cardIndex, Owner.PLAYER1)
        cardIndex = distributeTable(cardIndex, Owner.PLAYER2)
        cardIndex = distributeTable(cardIndex, Owner.PLAYER3)
        distributeTable(cardIndex, Owner.PLAYER4)
    }

    private fun distributeTable(index: Int, owner: Owner): Int {
        deck.value!![index].position = Position.TABLE_LEFT_DOWN
        deck.value!![index].owner = owner
        deck.value!![index + 1].position = Position.TABLE_LEFT_UP
        deck.value!![index + 1].owner = owner
        deck.value!![index + 2].position = Position.TABLE_CENTER_DOWN
        deck.value!![index + 2].owner = owner
        deck.value!![index + 3].position = Position.TABLE_CENTER_UP
        deck.value!![index + 3].owner = owner
        deck.value!![index + 4].position = Position.TABLE_RIGHT_DOWN
        deck.value!![index + 4].owner = owner
        deck.value!![index + 5].position = Position.TABLE_RIGHT_UP
        deck.value!![index + 5].owner = owner

        return index + 6
    }

    fun addToDiscard(card: Card) {
        val target = deck.value!!.single {
            it == card
        }
        val previousTopDiscardedCard = deck.value!!.singleOrNull {
            it.position == Position.ON_TOP
        }
        previousTopDiscardedCard?.position = Position.NONE
        target.position = Position.ON_TOP
        target.owner = Owner.DISCARDED
        deck.postValue(deck.value)
        if (currentTurn == 4) {
            currentTurn = 1
        } else {
            currentTurn++
        }
    }

    fun getCard(player: Int) {

        val target = deck.value!!.filter {
            it.owner == Owner.ON_PILE
        }
        if (target.isEmpty())
            return

        target[0].position = Position.HAND
        target[0].owner = when (player) {
            1 -> Owner.PLAYER1
            2 -> Owner.PLAYER2
            3 -> Owner.PLAYER3
            else -> Owner.PLAYER4
        }
        deck.postValue(deck.value)
    }
}