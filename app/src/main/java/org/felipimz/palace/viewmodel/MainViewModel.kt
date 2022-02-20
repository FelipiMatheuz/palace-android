package org.felipimz.palace.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Faced
import org.felipimz.palace.model.Pilar
import org.felipimz.palace.model.Position
import org.felipimz.palace.util.CardUtil

class MainViewModel : ViewModel() {

    //players deck
    val p1Hand = MutableLiveData<MutableList<Card>>()
    val p2Hand = MutableLiveData<MutableList<Card>>()
    val p3Hand = MutableLiveData<MutableList<Card>>()
    val p4Hand = MutableLiveData<MutableList<Card>>()

    //players pilars
    val p1Pilar = MutableLiveData<MutableList<Pilar>>()
    val p2Pilar = MutableLiveData<MutableList<Pilar>>()
    val p3Pilar = MutableLiveData<MutableList<Pilar>>()
    val p4Pilar = MutableLiveData<MutableList<Pilar>>()

    //global deck
    val pile = MutableLiveData<MutableList<Card>>()

    init {
        //init lists
        p1Hand.value = mutableListOf()
        p2Hand.value = mutableListOf()
        p3Hand.value = mutableListOf()
        p4Hand.value = mutableListOf()
        p1Pilar.value = mutableListOf()
        p2Pilar.value = mutableListOf()
        p3Pilar.value = mutableListOf()
        p4Pilar.value = mutableListOf()
    }

    fun distributeCard(deckWithJoker: Boolean) {
        //init deck and shuffle
        val cardUtil = CardUtil()
        pile.value = if (deckWithJoker) {
            cardUtil.getDeckWithJoker()
        } else {
            cardUtil.getDeckDefault()
        }

        pile.value?.shuffled()

        //get in hands
        for (i in 0..2) {
            p1Hand.value?.add(pile.value!!.last())
            pile.value?.removeLast()
            p2Hand.value?.add(pile.value!!.last())
            pile.value?.removeLast()
            p3Hand.value?.add(pile.value!!.last())
            pile.value?.removeLast()
            p4Hand.value?.add(pile.value!!.last())
            pile.value?.removeLast()
        }

        //distribute the rest
        for (p in Position.values()) {
            for (f in Faced.values()) {
                p1Pilar.value?.add(Pilar(p, pile.value!!.last(), f))
                pile.value?.removeLast()
                p2Pilar.value?.add(Pilar(p, pile.value!!.last(), f))
                pile.value?.removeLast()
                p3Pilar.value?.add(Pilar(p, pile.value!!.last(), f))
                pile.value?.removeLast()
                p4Pilar.value?.add(Pilar(p, pile.value!!.last(), f))
                pile.value?.removeLast()
            }
        }
    }
}