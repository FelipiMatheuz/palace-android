package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.felipimz.palace.databinding.ActivityMainBinding
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Faced
import org.felipimz.palace.model.Pilar
import org.felipimz.palace.model.Position
import org.felipimz.palace.util.CardUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //players deck
    private lateinit var p1_hand: MutableList<Card>
    private lateinit var p2_hand: MutableList<Card>
    private lateinit var p3_hand: MutableList<Card>
    private lateinit var p4_hand: MutableList<Card>

    //players pilars
    private lateinit var p1_pilar: MutableList<Pilar>
    private lateinit var p2_pilar: MutableList<Pilar>
    private lateinit var p3_pilar: MutableList<Pilar>
    private lateinit var p4_pilar: MutableList<Pilar>

    //global deck
    private lateinit var pile: MutableList<Card>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDeck()
        distributeCard()
    }

    private fun initDeck() {
        //init lists
        p1_hand = mutableListOf()
        p2_hand = mutableListOf()
        p3_hand = mutableListOf()
        p4_hand = mutableListOf()
        p1_pilar = mutableListOf()
        p2_pilar = mutableListOf()
        p3_pilar = mutableListOf()
        p4_pilar = mutableListOf()

        //init deck and shuffle
        val cardUtil = CardUtil()
        pile = cardUtil.getDeckDefault()
        pile.shuffled()
    }

    private fun distributeCard() {

        //get in hands
        for (i in 0..2) {
            p1_hand.add(pile.last())
            pile.removeLast()
            p2_hand.add(pile.last())
            pile.removeLast()
            p3_hand.add(pile.last())
            pile.removeLast()
            p4_hand.add(pile.last())
            pile.removeLast()
        }

        //distribute the rest
        for (p in Position.values()) {
            for (f in Faced.values()) {
                p1_pilar.add(Pilar(p, pile.last(), f))
                pile.removeLast()
                p2_pilar.add(Pilar(p, pile.last(), f))
                pile.removeLast()
                p3_pilar.add(Pilar(p, pile.last(), f))
                pile.removeLast()
                p4_pilar.add(Pilar(p, pile.last(), f))
                pile.removeLast()
            }
        }
    }
}