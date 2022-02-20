package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.felipimz.palace.adapter.CardHandAdapter
import org.felipimz.palace.databinding.ActivityMainBinding
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Faced
import org.felipimz.palace.model.Pilar
import org.felipimz.palace.model.Position
import org.felipimz.palace.viewmodel.MainViewModel
import org.felipimz.palace.viewmodel.PreferencesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var preferencesViewModel: PreferencesViewModel

    private lateinit var cardHandAdapter1: CardHandAdapter
    private lateinit var cardHandAdapter2: CardHandAdapter
    private lateinit var cardHandAdapter3: CardHandAdapter
    private lateinit var cardHandAdapter4: CardHandAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesViewModel(this)

        viewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        viewModel.distributeCard(preferencesViewModel.loadDeckWithJoker())

        // Observer
        viewModel.pile.observe(this) { value: MutableList<Card> ->
            binding.cardPile.tooltipText = value.size.toString()
            if (value.size <= 0) {
                binding.cardPile.setImageResource(0)
            } else {
                binding.cardPile.setImageResource(preferencesViewModel.loadDeck())
            }
        }
        loadHands()
        loadPilars()
    }

    private fun loadHands() {
        cardHandAdapter1 = CardHandAdapter(viewModel.p1Hand.value!!.toList(), true, this)
        cardHandAdapter2 = CardHandAdapter(viewModel.p2Hand.value!!.toList(), true, this)
        cardHandAdapter3 = CardHandAdapter(viewModel.p3Hand.value!!.toList(), false, this)
        cardHandAdapter4 = CardHandAdapter(viewModel.p4Hand.value!!.toList(), false, this)

        binding.poolPlayer1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.poolPlayer2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.poolPlayer3.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.poolPlayer4.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.poolPlayer1.adapter = cardHandAdapter1
        binding.poolPlayer2.adapter = cardHandAdapter2
        binding.poolPlayer3.adapter = cardHandAdapter3
        binding.poolPlayer4.adapter = cardHandAdapter4
    }

    private fun loadPilars() {
        viewModel.p1Pilar.observe(this) { value: MutableList<Pilar> ->
            setupPilar(1, value)
        }
        viewModel.p2Pilar.observe(this) { value: MutableList<Pilar> ->
            setupPilar(2, value)
        }
        viewModel.p3Pilar.observe(this) { value: MutableList<Pilar> ->
            setupPilar(3, value)
        }
        viewModel.p4Pilar.observe(this) { value: MutableList<Pilar> ->
            setupPilar(4, value)
        }
    }

    private fun setupPilar(player: Int, value: MutableList<Pilar>) {
        val leftPilar = value.filter { v -> v.position == Position.LEFT }
        val centerPilar = value.filter { v -> v.position == Position.CENTER }
        val rightPilar = value.filter { v -> v.position == Position.RIGHT }

        when (player) {
            1 -> {
                changeImage(leftPilar, binding.card1Player1)
                changeImage(centerPilar, binding.card2Player1)
                changeImage(rightPilar, binding.card3Player1)
            }
            2 -> {
                changeImage(leftPilar, binding.card1Player2)
                changeImage(centerPilar, binding.card2Player2)
                changeImage(rightPilar, binding.card3Player2)
            }
            3 -> {
                changeImage(leftPilar, binding.card1Player3)
                changeImage(centerPilar, binding.card2Player3)
                changeImage(rightPilar, binding.card3Player3)
            }
            4 -> {
                changeImage(leftPilar, binding.card1Player4)
                changeImage(centerPilar, binding.card2Player4)
                changeImage(rightPilar, binding.card3Player4)
            }
        }
    }

    private fun changeImage(pilar: List<Pilar>, imageView: ImageView) {
        if (pilar.isEmpty()) {
            imageView.setImageResource(0)
        } else {
            try {
                val cardFacedUp = pilar.single { v -> v.faced == Faced.UP }
                imageView.setImageResource(
                    resources.getIdentifier(
                        cardFacedUp.card.name,
                        "drawable",
                        packageName
                    )
                )
                imageView.setOnClickListener {
                    Toast.makeText(this, cardFacedUp.card.name, Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                imageView.setImageResource(preferencesViewModel.loadDeck())
            }
        }
    }
}