package org.felipimz.palace.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.felipimz.palace.R
import org.felipimz.palace.adapter.CardHandAdapter
import org.felipimz.palace.databinding.ActivityMainBinding
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.History
import org.felipimz.palace.model.Owner
import org.felipimz.palace.model.Position
import org.felipimz.palace.repository.HistoryRepository
import org.felipimz.palace.repository.PreferencesRepository
import org.felipimz.palace.viewmodel.MainViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    lateinit var preferencesViewModel: PreferencesRepository

    private lateinit var cardHandAdapter1: CardHandAdapter
    private lateinit var cardHandAdapter2: CardHandAdapter
    private lateinit var cardHandAdapter3: CardHandAdapter
    private lateinit var cardHandAdapter4: CardHandAdapter

    var lockActions: Boolean = false
    var isSetupCards: Boolean = true
    private var lockBot: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesRepository(this)

        viewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        viewModel.distributeCard(
            preferencesViewModel.loadDeckWithJoker(),
            preferencesViewModel.loadRules(),
            preferencesViewModel.loadDoubleDeck()
        )

        loadGameSetup()
        viewModel.deck.observe(this) { value: MutableList<Card> ->
            loadPiles(value)
            loadHands(value)
            loadTable(value)
            checkWinner(value)
        }

    }

    private fun checkWinner(value: MutableList<Card>) {
        val countPlayer1 = value.filter { card -> card.owner == Owner.PLAYER1 }.size
        val countPlayer2 = value.filter { card -> card.owner == Owner.PLAYER2 }.size
        val countPlayer3 = value.filter { card -> card.owner == Owner.PLAYER3 }.size
        val countPlayer4 = value.filter { card -> card.owner == Owner.PLAYER4 }.size

        if (countPlayer1 == 0 || countPlayer2 == 0 || countPlayer3 == 0 || countPlayer4 == 0) {
            displayWinner(intArrayOf(countPlayer1, countPlayer2, countPlayer3, countPlayer4))
            lockActions = true
            lockBot = true
        }
    }

    private fun recordHistory(sizes: IntArray) {
        val player1size = sizes[0]
        sizes.sort()
        val history = History(
            sizes.indexOf(player1size) + 1,
            "single",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
        val repository = HistoryRepository(this)
        repository.setHistoryList(history)
    }

    private fun loadGameSetup() {
        viewModel.viewModelScope.launch {
            binding.messageTable.text = getString(R.string.game_start)
            delay(1500)
            binding.messageTable.text = getString(R.string.choose_your_cards)
            delay(1500)
            binding.messageTable.text = ""
            binding.confirmSetup.visibility = View.VISIBLE
            binding.confirmSetup.setOnClickListener {
                it.visibility = View.GONE
                isSetupCards = false
                displayTurn()
            }
        }
    }

    fun displayTurn() {
        viewModel.viewModelScope.launch {
            binding.messageTable.text = when (viewModel.currentTurn) {
                1 -> "${getString(R.string.turn)} ${preferencesViewModel.loadNickName()}"
                else -> "${getString(R.string.bot_turn)}${viewModel.currentTurn}"
            }
            lockActions = viewModel.currentTurn != 1
            delay(1500)
            binding.messageTable.text = ""
            val display = viewModel.getCard(preferencesViewModel.loadWildCardAsSpecial(), lockBot)

            if (display) {
                displayTurn()
            }
        }
    }

    private fun displayWinner(sizes: IntArray) {
        viewModel.viewModelScope.launch {
            val player = sizes.indexOf(0) + 1
            binding.messageTable.text = when (player) {
                1 -> "${getString(R.string.winner)} ${preferencesViewModel.loadNickName()}"
                else -> "${getString(R.string.bot_winner)}${player}"
            }
            delay(1500)
            recordHistory(sizes)
            super.onBackPressed()
            finish()
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

        val discarded = value.filter {
            it.owner == Owner.DISCARDED
        }
        binding.cardDiscarded.tooltipText = discarded.size.toString()
        if (discarded.isEmpty()) {
            binding.cardDiscarded.setImageResource(0)
        } else {
            val resId = resources.getIdentifier(
                discarded.single { it.position == Position.ON_TOP }.name,
                "drawable",
                packageName
            )
            binding.cardDiscarded.setImageResource(resId)
        }
    }

    private fun loadHands(value: MutableList<Card>) {
        cardHandAdapter1 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER1 && it.position.name.contains("HAND") }.sortedBy { it.value },
            true,
            this
        )
        cardHandAdapter2 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER2 && it.position.name.contains("HAND") }.sortedBy { it.value },
            true,
            this
        )
        cardHandAdapter3 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER3 && it.position.name.contains("HAND") }.sortedBy { it.value },
            false,
            this
        )
        cardHandAdapter4 = CardHandAdapter(
            value.filter { it.owner == Owner.PLAYER4 && it.position.name.contains("HAND") }.sortedBy { it.value },
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
        setupTable(1, value.filter { it.owner == Owner.PLAYER1 })
        setupTable(2, value.filter { it.owner == Owner.PLAYER2 })
        setupTable(3, value.filter { it.owner == Owner.PLAYER3 })
        setupTable(4, value.filter { it.owner == Owner.PLAYER4 })
    }

    private fun setupTable(player: Int, value: List<Card>) {
        val leftPilar =
            value.filter { v -> v.position.name.contains("TABLE_LEFT") }
        val centerPilar =
            value.filter { v -> v.position.name.contains("TABLE_CENTER") }
        val rightPilar =
            value.filter { v -> v.position.name.contains("TABLE_RIGHT") }

        when (player) {
            1 -> {
                changeImage(leftPilar, binding.card1Player1, cardHandAdapter1)
                changeImage(centerPilar, binding.card2Player1, cardHandAdapter1)
                changeImage(rightPilar, binding.card3Player1, cardHandAdapter1)
            }
            2 -> {
                changeImage(leftPilar, binding.card1Player2, cardHandAdapter2)
                changeImage(centerPilar, binding.card2Player2, cardHandAdapter2)
                changeImage(rightPilar, binding.card3Player2, cardHandAdapter2)
            }
            3 -> {
                changeImage(leftPilar, binding.card1Player3, cardHandAdapter3)
                changeImage(centerPilar, binding.card2Player3, cardHandAdapter3)
                changeImage(rightPilar, binding.card3Player3, cardHandAdapter3)
            }
            4 -> {
                changeImage(leftPilar, binding.card1Player4, cardHandAdapter4)
                changeImage(centerPilar, binding.card2Player4, cardHandAdapter4)
                changeImage(rightPilar, binding.card3Player4, cardHandAdapter4)
            }
        }
    }

    private fun changeImage(card: List<Card>, imageView: ImageView, adapter: CardHandAdapter) {
        if (card.isEmpty()) {
            imageView.setImageResource(0)
            imageView.isEnabled = false
        } else {
            try {
                val cardUp = card.single { v -> v.position.name.contains("UP") }
                imageView.setImageResource(
                    resources.getIdentifier(
                        cardUp.name,
                        "drawable",
                        packageName
                    )
                )
                if (!lockActions) {
                    if (isSetupCards) {
                        imageView.setOnClickListener {
                            viewModel.changeHand(cardUp)
                        }
                    } else if (adapter.itemCount == 0) {
                        imageView.setOnClickListener {
                            viewModel.addToDiscard(listOf(cardUp), preferencesViewModel.loadWildCardAsSpecial())
                            displayTurn()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                imageView.setImageResource(preferencesViewModel.loadDeck())
                if (adapter.itemCount == 0 && !lockActions) {
                    imageView.setOnClickListener {
                        val cardDown = card.single { v -> v.position.name.contains("DOWN") }
                        viewModel.addToDiscard(listOf(cardDown), preferencesViewModel.loadWildCardAsSpecial())
                        displayTurn()
                    }
                }
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