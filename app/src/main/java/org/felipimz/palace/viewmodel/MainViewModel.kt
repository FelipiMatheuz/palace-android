package org.felipimz.palace.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Owner
import org.felipimz.palace.model.Position
import org.felipimz.palace.model.WildCardEffect
import org.felipimz.palace.util.CardUtil

class MainViewModel : ViewModel() {

    //global deck
    val deck = MutableLiveData<MutableList<Card>>()
    private val cardUtil = CardUtil()
    var currentTurn: Int = 1
    private val additionalInfo: Map<String, Int>
    private var reverse: Boolean = false

    init {
        deck.value = mutableListOf()
        additionalInfo = cardUtil.loadAdditionalInfo()
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
                listTarget.forEach{ target ->
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
                        burned = true
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

    fun addToDiscard(card: Card, ignoreValueWildCards: Boolean) {
        val target = deck.value!!.single {
            it == card
        }
        val previousTopDiscardedCard = deck.value!!.singleOrNull {
            it.position == Position.ON_TOP
        }

        var burned = false

        if (validateDiscard(target, previousTopDiscardedCard, ignoreValueWildCards)) {
            previousTopDiscardedCard?.position = Position.NONE
            target.position = Position.ON_TOP
            target.owner = Owner.DISCARDED

            if (target.wildCard == WildCardEffect.REVERSE) {
                reverse = !reverse
            } else if (target.wildCard == WildCardEffect.BURNPILE) {
                addToBurn()
                burned = true
            } else {
                if (additionalInfo["discarded_top_value"] != target.value) {
                    additionalInfo["discarded_top_value"] to target.value
                    additionalInfo["discarded_top_times"] to 1
                } else {
                    additionalInfo["discarded_top_times"] to additionalInfo["discarded_top_times"]?.plus(1)
                }

                if (additionalInfo["discarded_top_times"]!! >= 4) {
                    addToBurn()
                    burned = true
                }
            }

        } else {
            backToHand(target)
        }
        deck.postValue(deck.value)

        if (!burned) {
            changeTurn()
        }
    }

    private fun changeTurn() {
        if (reverse) {
            if (currentTurn == 1) {
                currentTurn = 4
            } else {
                currentTurn--
            }
        } else {
            if (currentTurn == 4) {
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
        val isWildCard = target.wildCard != WildCardEffect.NONE
        val isGreaterOrEqualValue =
            previous == null || previous.wildCard == WildCardEffect.RESET || target.value >= previous.value

        return (ignoreValueWildCards && isWildCard) || isGreaterOrEqualValue
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
        return if (currentTurn != 1) {
            if (!lockBot)
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
                addToDiscard(handToDiscard, ignoreValueWildCards)
            } else {
                val tableToDiscard = robotCheckTable(deckOwner, ignoreValueWildCards)
                if (tableToDiscard != null) {
                    addToDiscard(tableToDiscard, ignoreValueWildCards)
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
}