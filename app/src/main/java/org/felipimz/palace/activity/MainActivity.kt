package org.felipimz.palace.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.felipimz.palace.R
import org.felipimz.palace.adapter.CardHandAdapter
import org.felipimz.palace.databinding.ActivityMainBinding
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Owner
import org.felipimz.palace.model.Position
import org.felipimz.palace.repository.PreferencesRepository
import org.felipimz.palace.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private lateinit var preferencesViewModel: PreferencesRepository

    private lateinit var cardHandAdapter1: CardHandAdapter
    private lateinit var cardHandAdapter2: CardHandAdapter
    private lateinit var cardHandAdapter3: CardHandAdapter
    private lateinit var cardHandAdapter4: CardHandAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesRepository(this)

        viewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        viewModel.distributeCard(preferencesViewModel.loadDeckWithJoker())

        viewModel.deck.observe(this) { value: MutableList<Card> ->
            loadPiles(value)
            loadHands(value)
            loadTable(value)
        }
    }

    private fun loadPiles(value: MutableList<Card>) {
        val pile = value.filter {
            it.owner == Owner.ON_PILE
        }
        binding.cardPile.tooltipText = pile.size.toString()
        if (pile.isEmpty()) {
            binding.cardPile.setImageResource(0)
        } else {
            binding.cardPile.setImageResource(preferencesViewModel.loadDeck())
        }

        binding.cardPile.setOnClickListener {
            if (pile.isNotEmpty()) {
                viewModel.getCard(1)
            }
        }

        val discarded = value.filter {
            it.owner == Owner.DISCARDED
        }
        binding.cardBurned.tooltipText = discarded.size.toString()
        if (discarded.isEmpty()) {
            binding.cardBurned.setImageResource(0)
        } else {
            val resId = resources.getIdentifier(
                discarded.single { it.position == Position.ON_TOP }.name,
                "drawable",
                packageName
            )
            binding.cardBurned.setImageResource(resId)
        }
    }

    private fun loadHands(value: MutableList<Card>) {
        cardHandAdapter1 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER1 && it.position.name.contains("HAND") },
            true,
            this
        )
        cardHandAdapter2 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER2 && it.position.name.contains("HAND") },
            true,
            this
        )
        cardHandAdapter3 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER3 && it.position.name.contains("HAND") },
            false,
            this
        )
        cardHandAdapter4 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER4 && it.position.name.contains("HAND") },
            false,
            this
        )

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

    private fun loadTable(value: MutableList<Card>) {
        setupPilar(1, value.filter { it.owner == Owner.PLAYER1 })
        setupPilar(2, value.filter { it.owner == Owner.PLAYER2 })
        setupPilar(3, value.filter { it.owner == Owner.PLAYER3 })
        setupPilar(4, value.filter { it.owner == Owner.PLAYER4 })
    }

    private fun setupPilar(player: Int, value: List<Card>) {
        val leftPilar =
            value.filter { v -> v.position.name.contains("TABLE_LEFT") }
        val centerPilar =
            value.filter { v -> v.position.name.contains("TABLE_CENTER") }
        val rightPilar =
            value.filter { v -> v.position.name.contains("TABLE_RIGHT") }

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

    private fun changeImage(card: List<Card>, imageView: ImageView) {
        if (card.isEmpty()) {
            imageView.setImageResource(0)
        } else {
            try {
                val cardFacedUp = card.single { v -> v.position.name.contains("UP") }
                imageView.setImageResource(
                    resources.getIdentifier(
                        cardFacedUp.name,
                        "drawable",
                        packageName
                    )
                )
                imageView.setOnClickListener {
                    Toast.makeText(this, cardFacedUp.name, Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                imageView.setImageResource(preferencesViewModel.loadDeck())
            }
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.confirm_exit)
            .setPositiveButton(
                R.string.yes
            ) { _, _ ->
                super.onBackPressed()
                finish()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}