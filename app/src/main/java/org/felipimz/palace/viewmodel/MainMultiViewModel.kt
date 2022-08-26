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
    private val _deck = MutableLiveData<MutableList<Card>>()
    private val cardUtil = CardUtil()
    private var _room = MutableLiveData<Room>()
    var currentTurn: Int = 1
    var lastPlayer: Int = 4
    private val additionalInfo: Map<String, Int>
    private var reverse: Boolean = false
    private var firestore = FirebaseFirestore.getInstance()

    init {
        _deck.value = mutableListOf()
        additionalInfo = cardUtil.loadAdditionalInfo()
    }

    internal var getRoom: MutableLiveData<Room>
        get() {
            return _room
        }
        set(value) {
            _room.value
        }

    fun distributeCard() {
        //init deck and shuffle
        firestore.collection("rooms").document(_room.value?.id!!).get()
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
                //distribute the rest
                cardIndex = distributeTable(cardIndex, Owner.PLAYER1)
                cardIndex = distributeTable(cardIndex, Owner.PLAYER2)
                if (doubleDeck) {
                    cardIndex = distributeTable(cardIndex, Owner.PLAYER3)
                    distributeTable(cardIndex, Owner.PLAYER4)
                } else {
                    lastPlayer = 2
                }

                room.deck = deck
                updateRoom(room)
            }
    }

    private fun distributeTable(index: Int, owner: Owner): Int {
        _deck.value!![index].position = Position.TABLE_LEFT_DOWN
        _deck.value!![index].owner = owner
        _deck.value!![index + 1].position = Position.TABLE_LEFT_UP
        _deck.value!![index + 1].owner = owner
        _deck.value!![index + 2].position = Position.TABLE_CENTER_DOWN
        _deck.value!![index + 2].owner = owner
        _deck.value!![index + 3].position = Position.TABLE_CENTER_UP
        _deck.value!![index + 3].owner = owner
        _deck.value!![index + 4].position = Position.TABLE_RIGHT_DOWN
        _deck.value!![index + 4].owner = owner
        _deck.value!![index + 5].position = Position.TABLE_RIGHT_UP
        _deck.value!![index + 5].owner = owner

        return index + 6
    }

    fun addToDiscard(listCard: List<Card>, ignoreValueWildCards: Boolean) {

        var burned = false

        val listTarget = _deck.value!!.filter {
            listCard.contains(it)
        }
        val previousTopDiscardedCard = _deck.value!!.singleOrNull {
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
                if (additionalInfo["discarded_top_value"] != listTarget[0].value) {
                    additionalInfo["discarded_top_value"] to listTarget[0].value
                    additionalInfo["discarded_top_times"] to 1
                } else {
                    additionalInfo["discarded_top_times"] to additionalInfo["discarded_top_times"]?.plus(listTarget.size)
                }

                if (additionalInfo["discarded_top_times"]!! >= 4) {
                    addToBurn()
                }
            }

        } else {
            listTarget.forEach { target ->
                backToHand(target)
            }
        }

        _deck.postValue(_deck.value)

        if (!burned) {
            changeTurn()
        }
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
        _deck.value!!.forEach {
            if (it.owner == Owner.DISCARDED) {
                it.owner = target.owner
                it.position = Position.HAND
            }
        }
        additionalInfo["discarded_top_value"] to 0
        additionalInfo["discarded_top_times"] to 0
    }

    private fun addToBurn() {
        _deck.value!!.forEach {
            if (it.owner == Owner.DISCARDED) {
                it.owner = Owner.BURNED
                it.position = Position.NONE
            }
        }
        additionalInfo["discarded_top_value"] to 0
        additionalInfo["discarded_top_times"] to 0
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

        val target = _deck.value!!.filter {
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
            _deck.postValue(_deck.value)
        }
    }

    fun changeHand(card: Card) {
        val target = _deck.value!!.single {
            it == card
        }
        val cardHandClicked = _deck.value!!.singleOrNull { it.position == Position.HAND_CLICKED }

        if (cardHandClicked != null) {
            cardHandClicked.position = target.position
            target.position = Position.HAND
            _deck.postValue(_deck.value)
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

    fun shufflePlayers(playerId: String): Int {
        _room.value?.members = _room.value?.members?.shuffled()?.toMutableList()!!
        updateRoom(_room.value!!)

        return _room.value!!.members.indexOf(_room.value!!.members.filter { f -> f.id == playerId }[0]) + 1
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
                    _room.value = room
                    activity.loadGame()
                }
            }

    }

    fun displayPlayer(context: Context): String {
        return "${context.getString(R.string.turn)} ${_room.value?.members?.get(currentTurn - 1)}"
    }

    fun displayWinner(context: Context, index: Int): String {
        return "${context.getString(R.string.winner)} ${_room.value?.members?.get(index)}"
    }

    fun updateStatus(playerOnwer: Int) {
        _room.value!!.members[playerOnwer].status = Status.READY
        updateRoom(_room.value!!)
    }

    fun closeRoom() {
        firestore.collection("rooms")
            .document(_room.value!!.id).delete()
        _room.value = null
    }
}