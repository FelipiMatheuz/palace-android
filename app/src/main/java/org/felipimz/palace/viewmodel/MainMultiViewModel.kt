package org.felipimz.palace.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.felipimz.palace.R
import org.felipimz.palace.activity.MainMultiActivity
import org.felipimz.palace.model.*
import org.felipimz.palace.util.CardUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainMultiViewModel : ViewModel() {

    //global deck
    private val cardUtil = CardUtil()
    internal var room = MutableLiveData<Room?>()
    var currentTurn: Int = 1
    private var lastPlayer: Int = 4
    private val additionalInfo: MutableList<Int> = mutableListOf(0, 0)
    private var reverse: Boolean = false
    private var firestore = FirebaseFirestore.getInstance()
    private var init = false

    fun distributeCard() {
        //init deck and shuffle
        firestore.collection("rooms").document(room.value?.id!!).get()
            .addOnSuccessListener {
                val room = it.toObject(Room::class.java)
                val doubleDeck = room!!.members.size == 4
                val deck = cardUtil.getDefaultDeck(room.deckWithJokerMultiplayer, doubleDeck)
                deck.shuffled().toMutableList()

                var cardIndex = 0
                for (card in deck) {
                    //distribute on hands
                    if (cardIndex < 3) {
                        card.position = Position.HAND
                        card.owner = Owner.PLAYER1
                    } else if (cardIndex < 6) {
                        card.position = Position.HAND
                        card.owner = Owner.PLAYER2
                    } else if (doubleDeck && cardIndex < 9) {
                        card.position = Position.HAND
                        card.owner = Owner.PLAYER3
                    } else if (doubleDeck && cardIndex < 12) {
                        card.position = Position.HAND
                        card.owner = Owner.PLAYER4
                        //distribute on table
                    } else {
                        break
                    }
                    cardIndex++
                }
                this.room.value!!.deck = deck
                //distribute the rest
                cardIndex = distributeTable(cardIndex, Owner.PLAYER1)
                cardIndex = distributeTable(cardIndex, Owner.PLAYER2)
                if (doubleDeck) {
                    cardIndex = distributeTable(cardIndex, Owner.PLAYER3)
                    distributeTable(cardIndex, Owner.PLAYER4)
                } else {
                    lastPlayer = 2
                }
                updateDeck(this.room.value!!.deck)
            }
    }

    private fun distributeTable(index: Int, owner: Owner): Int {
        room.value!!.deck[index].position = Position.TABLE_LEFT_DOWN
        room.value!!.deck[index].owner = owner
        room.value!!.deck[index + 1].position = Position.TABLE_LEFT_UP
        room.value!!.deck[index + 1].owner = owner
        room.value!!.deck[index + 2].position = Position.TABLE_CENTER_DOWN
        room.value!!.deck[index + 2].owner = owner
        room.value!!.deck[index + 3].position = Position.TABLE_CENTER_UP
        room.value!!.deck[index + 3].owner = owner
        room.value!!.deck[index + 4].position = Position.TABLE_RIGHT_DOWN
        room.value!!.deck[index + 4].owner = owner
        room.value!!.deck[index + 5].position = Position.TABLE_RIGHT_UP
        room.value!!.deck[index + 5].owner = owner

        return index + 6
    }

    fun addToDiscard(listCard: List<Card>, ignoreValueWildCards: Boolean) {

        var burned = false

        val listTarget = room.value!!.deck.filter {
            listCard.contains(it)
        }
        val previousTopDiscardedCard = room.value!!.deck.singleOrNull {
            it.position == Position.ON_TOP
        }

        if (validateDiscard(listTarget[0], previousTopDiscardedCard, ignoreValueWildCards)) {
            previousTopDiscardedCard?.position = Position.NONE
            listTarget.forEach { target ->
                target.position = Position.NONE
                target.owner = Owner.DISCARDED
            }
            listTarget[0].position = Position.ON_TOP

            if (listTarget[0].wildCard == WildCardEffect.REVERSE) {
                reverse = !reverse
            } else if (listTarget[0].wildCard == WildCardEffect.BURNPILE) {
                addToBurn()
                burned = true
            } else {
                if (additionalInfo[0] != listTarget[0].value) {
                    additionalInfo[0] = listTarget[0].value
                    additionalInfo[1] = 1
                } else {
                    additionalInfo[1] = additionalInfo[1] + listTarget.size
                }

                if (additionalInfo[1] >= 4) {
                    addToBurn()
                }
            }

        } else {
            listTarget.forEach { target ->
                backToHand(target)
            }
        }

        updateDeck(room.value!!.deck)

        if (!burned) {
            changeTurn()
        }
    }

    private fun updateDeck(deck: MutableList<Card>) {
        firestore.collection("rooms")
            .document(room.value!!.id).update("deck", deck)
    }

    private fun changeTurn() {
        if (reverse) {
            if (currentTurn == 1) {
                currentTurn = lastPlayer
            } else {
                currentTurn--
            }
        } else {
            if (currentTurn == lastPlayer) {
                currentTurn = 1
            } else {
                currentTurn++
            }
        }
    }

    private fun backToHand(target: Card) {
        target.position = Position.HAND
        room.value!!.deck.forEach {
            if (it.owner == Owner.DISCARDED) {
                it.owner = target.owner
                it.position = Position.HAND
            }
        }
        additionalInfo[0] = 0
        additionalInfo[1] = 0
    }

    private fun addToBurn() {
        room.value!!.deck.forEach {
            if (it.owner == Owner.DISCARDED) {
                it.owner = Owner.BURNED
                it.position = Position.NONE
            }
        }
        additionalInfo[0] = 0
        additionalInfo[1] = 0
    }

    private fun validateDiscard(target: Card, previous: Card?, ignoreValueWildCards: Boolean): Boolean {
        val isValidWildCard =
            (ignoreValueWildCards && target.wildCard != WildCardEffect.NONE) || target.wildCard == WildCardEffect.RESET
        val isValidValueToDiscard =
            previous == null || previous.wildCard == WildCardEffect.RESET ||
                    if (previous.wildCard == WildCardEffect.FORCEDOWN) {
                        target.value <= previous.value
                    } else {
                        target.value >= previous.value
                    }
        return isValidWildCard || isValidValueToDiscard
    }

    fun getCard() {

        val target = room.value!!.deck.filter {
            it.owner == Owner.ON_PILE
        }
        if (target.isNotEmpty()) {
            target[0].position = Position.HAND
            target[0].owner = when (currentTurn) {
                1 -> Owner.PLAYER1
                2 -> Owner.PLAYER2
                3 -> Owner.PLAYER3
                else -> Owner.PLAYER4
            }
            updateDeck(room.value!!.deck)
        }
    }

    fun changeHand(card: Card) {
        val target = room.value!!.deck.single {
            it == card
        }
        val cardHandClicked = room.value!!.deck.singleOrNull { it.position == Position.HAND_CLICKED }

        if (cardHandClicked != null) {
            cardHandClicked.position = target.position
            target.position = Position.HAND
            updateDeck(room.value!!.deck)
        }
    }

    fun recordHistory(sizes: IntArray, gameMode: String, context: Context) {
        val player1size = sizes[0]
        sizes.sort()
        val history = History(
            sizes.indexOf(player1size) + 1,
            gameMode,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
        val historyViewModel = HistoryViewModel(context)
        historyViewModel.setHistoryList(history)
    }

    fun shufflePlayers() {
        room.value?.members = room.value?.members?.shuffled()?.toMutableList()!!
        updateRoom(room.value!!)
    }

    private fun updateRoom(room: Room) {
        firestore.collection("rooms")
            .document(room.id).set(room)
    }

    fun listenRoom(roomId: String, activity: MainMultiActivity) {
        firestore.collection("rooms")
            .document(roomId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val room = snapshot.toObject(Room::class.java)
                    this.room.value = room
                    if (!init) {
                        init = true
                        activity.loadGame()
                    }
                }
            }

    }

    fun displayPlayer(context: Context): String {
        return "${context.getString(R.string.turn)} ${room.value?.members?.get(currentTurn - 1)?.displayName}"
    }

    fun displayWinner(context: Context, index: Int): String {
        return "${context.getString(R.string.winner)} ${room.value?.members?.get(index)?.displayName}"
    }

    fun updateStatus(playerPosition: Int) {
        room.value!!.members[playerPosition].status = Status.READY
        updateRoom(room.value!!)
    }

    fun closeRoom() {
        firestore.collection("rooms")
            .document(room.value!!.id).delete()
        room.value = null
    }
}