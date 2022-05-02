package org.felipimz.palace.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.felipimz.palace.model.*
import org.felipimz.palace.util.CardUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {

    //global deck
    val deck = MutableLiveData<MutableList<Card>>()
    private val cardUtil = CardUtil()
    var currentTurn: Int = 1
    var lastPlayer: Int = 4
    private val additionalInfo: Map<String, Int>
    private var reverse: Boolean = false

    init {
        deck.value = mutableListOf()
        additionalInfo = cardUtil.loadAdditionalInfo()
    }

    fun distributeCard(deckWithJoker: Boolean, rules: String, doubleDeck: Boolean) {
        //init deck and shuffle
        deck.value = if (rules == "default") {
            cardUtil.getDefaultDeck(deckWithJoker, doubleDeck)
        } else {
            cardUtil.getCustomDeck(deckWithJoker, rules.split(";"), doubleDeck)
        }

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

    fun addToDiscard(listCard: List<Card>, ignoreValueWildCards: Boolean) {

        var burned = false

        val listTarget = deck.value!!.filter {
            listCard.contains(it)
        }
        val previousTopDiscardedCard = deck.value!!.singleOrNull {
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

        deck.postValue(deck.value)

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
        deck.value!!.forEach {
            if (it.owner == Owner.DISCARDED) {
                it.owner = target.owner
                it.position = Position.HAND
            }
        }
        additionalInfo["discarded_top_value"] to 0
        additionalInfo["discarded_top_times"] to 0
    }

    private fun addToBurn() {
        deck.value!!.forEach {
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

    fun getCard(ignoreValueWildCards: Boolean, lockBot: Boolean): Boolean {

        val target = deck.value!!.filter {
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
            deck.postValue(deck.value)
        }
        return if (currentTurn != 1 && !lockBot) {
            robotPlay(currentTurn, ignoreValueWildCards)
            true
        } else {
            false
        }
    }

    private fun robotPlay(player: Int, ignoreValueWildCards: Boolean) {
        val deckOwner = when (player) {
            2 -> Owner.PLAYER2
            3 -> Owner.PLAYER3
            4 -> Owner.PLAYER4
            else -> null
        }
        if (deckOwner != null) {
            val handToDiscard = robotCheckHand(deckOwner, ignoreValueWildCards)
            if (handToDiscard != null) {
                addToDiscard(listOf(handToDiscard), ignoreValueWildCards)
            } else {
                val tableToDiscard = robotCheckTable(deckOwner, ignoreValueWildCards)
                if (tableToDiscard != null) {
                    addToDiscard(listOf(tableToDiscard), ignoreValueWildCards)
                }
            }
        }
    }

    private fun robotCheckTable(deckOwner: Owner, ignoreValueWildCards: Boolean): Card? {
        if (ignoreValueWildCards) {
            val botTableUp = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("UP") && it.wildCard == WildCardEffect.NONE
            }.toMutableList()
            val botWildCardTableUp = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("UP") && it.wildCard != WildCardEffect.NONE
            }.toMutableList()
            if (botTableUp.isNotEmpty()) {
                botTableUp.sortBy { it.value }

                val previousTopDiscardedCard = deck.value!!.singleOrNull {
                    it.position == Position.ON_TOP
                }
                return if (previousTopDiscardedCard == null) {
                    botTableUp[0]
                } else {
                    val cardToDiscard = botTableUp.filter {
                        if (previousTopDiscardedCard.wildCard == WildCardEffect.FORCEDOWN) {
                            it.value <= previousTopDiscardedCard.value
                        } else {
                            it.value >= previousTopDiscardedCard.value
                        }
                    }
                    if (cardToDiscard.isNotEmpty()) {
                        cardToDiscard[0]
                    } else {
                        if (botWildCardTableUp.isNotEmpty()) {
                            botWildCardTableUp[0]
                        } else {
                            botTableUp[0]
                        }
                    }
                }
            } else {
                return if (botWildCardTableUp.isNotEmpty()) {
                    botWildCardTableUp[0]
                } else {
                    val botTableDown = deck.value!!.filter {
                        it.owner == deckOwner && it.position.name.contains("DOWN")
                    }.toMutableList()
                    if (botTableDown.isNotEmpty()) {
                        botTableDown.random()
                    } else {
                        null
                    }
                }
            }
        } else {
            val botTableUp = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("UP")
            }.toMutableList()

            return if (botTableUp.isNotEmpty()) {
                botTableUp.sortBy { it.value }

                val previousTopDiscardedCard = deck.value!!.singleOrNull {
                    it.position == Position.ON_TOP
                }
                if (previousTopDiscardedCard == null) {
                    botTableUp[0]
                } else {
                    val cardToDiscard = botTableUp.filter {
                        if (previousTopDiscardedCard.wildCard == WildCardEffect.FORCEDOWN) {
                            it.value <= previousTopDiscardedCard.value
                        } else {
                            it.value >= previousTopDiscardedCard.value
                        }
                    }
                    if (cardToDiscard.isNotEmpty()) {
                        cardToDiscard[0]
                    } else {
                        botTableUp[0]
                    }
                }
            } else {
                val botTableDown = deck.value!!.filter {
                    it.owner == deckOwner && it.position.name.contains("DOWN")
                }.toMutableList()
                if (botTableDown.isNotEmpty()) {
                    botTableDown.random()
                } else {
                    null
                }
            }
        }
    }

    private fun robotCheckHand(deckOwner: Owner?, ignoreValueWildCards: Boolean): Card? {
        if (ignoreValueWildCards) {
            val botHand = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("HAND") && it.wildCard == WildCardEffect.NONE
            }.toMutableList()
            val botWildCardHand = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("HAND") && it.wildCard != WildCardEffect.NONE
            }.toMutableList()
            if (botHand.isNotEmpty()) {
                botHand.sortBy { it.value }

                val previousTopDiscardedCard = deck.value!!.singleOrNull {
                    it.position == Position.ON_TOP
                }
                return if (previousTopDiscardedCard == null) {
                    botHand[0]
                } else {
                    val cardToDiscard = botHand.filter {
                        if (previousTopDiscardedCard.wildCard == WildCardEffect.FORCEDOWN) {
                            it.value <= previousTopDiscardedCard.value
                        } else {
                            it.value >= previousTopDiscardedCard.value
                        }
                    }
                    if (cardToDiscard.isNotEmpty()) {
                        cardToDiscard[0]
                    } else {
                        if (botWildCardHand.isNotEmpty()) {
                            botWildCardHand[0]
                        } else {
                            botHand[0]
                        }
                    }
                }
            } else {
                return if (botWildCardHand.isNotEmpty()) {
                    botWildCardHand[0]
                } else {
                    null
                }
            }
        } else {
            val botHand = deck.value!!.filter {
                it.owner == deckOwner && it.position.name.contains("HAND")
            }.toMutableList()

            return if (botHand.isNotEmpty()) {
                botHand.sortBy { it.value }

                val previousTopDiscardedCard = deck.value!!.singleOrNull {
                    it.position == Position.ON_TOP
                }
                if (previousTopDiscardedCard == null) {
                    botHand[0]
                } else {
                    val cardToDiscard = botHand.filter {
                        if (previousTopDiscardedCard.wildCard == WildCardEffect.FORCEDOWN) {
                            it.value <= previousTopDiscardedCard.value
                        } else {
                            it.value >= previousTopDiscardedCard.value
                        }
                    }
                    if (cardToDiscard.isNotEmpty()) {
                        cardToDiscard[0]
                    } else {
                        botHand[0]
                    }
                }
            } else {
                null
            }
        }
    }

    fun changeHand(card: Card) {
        val target = deck.value!!.single {
            it == card
        }
        val cardHandClicked = deck.value!!.singleOrNull { it.position == Position.HAND_CLICKED }

        if (cardHandClicked != null) {
            cardHandClicked.position = target.position
            target.position = Position.HAND
            deck.postValue(deck.value)
        }
    }

    fun recordHistory(sizes: IntArray, context: Context) {
        val player1size = sizes[0]
        sizes.sort()
        val history = History(
            sizes.indexOf(player1size) + 1,
            "single",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
        val historyViewModel = HistoryViewModel(context)
        historyViewModel.setHistoryList(history)
    }
}