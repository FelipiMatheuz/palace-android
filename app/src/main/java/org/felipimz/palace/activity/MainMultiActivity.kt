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
import org.felipimz.palace.adapter.CardHandMultiAdapter
import org.felipimz.palace.databinding.ActivityMainBinding
import org.felipimz.palace.model.*
import org.felipimz.palace.viewmodel.MainMultiViewModel
import org.felipimz.palace.viewmodel.PreferencesViewModel

class MainMultiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainMultiViewModel
    lateinit var preferencesViewModel: PreferencesViewModel

    private lateinit var cardHandAdapter1: CardHandMultiAdapter
    private lateinit var cardHandAdapter2: CardHandMultiAdapter
    private lateinit var cardHandAdapter3: CardHandMultiAdapter
    private lateinit var cardHandAdapter4: CardHandMultiAdapter

    var lockActions: Boolean = false
    var isSetupCards: Boolean = true
    private var allSet: Boolean = false
    private var playerOnwer = -1
    private var owners = listOf<Owner>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesViewModel(this)

        viewModel = ViewModelProvider.NewInstanceFactory().create(MainMultiViewModel::class.java)
        viewModel.listenRoom(intent.extras!!.getString("room_id")!!, this)
        loadGame()
    }

    fun loadGame() {
        playerOnwer = viewModel.shufflePlayers(intent.extras!!.getString("player_id")!!)
        loadOwners()
        if (playerOnwer == 0) {
            viewModel.distributeCard()
        }
        loadGameSetup()
        viewModel.room.observe(this) { value: Room? ->
            if (value == null) {
                return@observe
            }
            if (!allSet) {
                val countReadyPlayers = value.members.filter { it.status == Status.READY }.size
                if (countReadyPlayers == value.members.size) {
                    allSet = true
                    displayTurn()
                }
            }
            if (value.deck.isNotEmpty()) {
                loadPiles(value.deck)
                loadHands(value.deck)
                loadTable(value.deck)
                checkWinner(value.deck)
            }
        }
    }

    private fun loadOwners() {
        owners = getOnwers(playerOnwer)
    }

    private fun checkWinner(value: MutableList<Card>) {
        val countPlayer1 = value.filter { card -> card.owner == Owner.PLAYER1 }.size
        val countPlayer2 = value.filter { card -> card.owner == Owner.PLAYER2 }.size
        val countPlayer3 = if (intent.extras!!.getBoolean("double")) {
            value.filter { card -> card.owner == Owner.PLAYER3 }.size
        } else {
            99
        }
        val countPlayer4 = if (intent.extras!!.getBoolean("double")) {
            value.filter { card -> card.owner == Owner.PLAYER4 }.size
        } else {
            99
        }

        if (countPlayer1 == 0 || countPlayer2 == 0 || countPlayer3 == 0 || countPlayer4 == 0) {
            displayWinner(intArrayOf(countPlayer1, countPlayer2, countPlayer3, countPlayer4))
            lockActions = true
        }
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
                viewModel.updateStatus(playerOnwer)
            }
        }
    }

    fun displayTurn() {
        viewModel.viewModelScope.launch {
            binding.messageTable.text = viewModel.displayPlayer(this@MainMultiActivity)
            lockActions = viewModel.currentTurn != playerOnwer
            delay(1500)
            binding.messageTable.text = ""
            viewModel.getCard()
        }
    }

    private fun displayWinner(sizes: IntArray) {
        viewModel.viewModelScope.launch {
            binding.messageTable.text = viewModel.displayWinner(this@MainMultiActivity, sizes.indexOf(0))
            delay(1500)
            viewModel.recordHistory(sizes, "multi", this@MainMultiActivity)
            viewModel.closeRoom()
            super.onBackPressed()
            finish()
        }
    }

    private fun getOnwers(index: Int): List<Owner> {
        return when (index) {
            0 -> listOf(Owner.PLAYER1, Owner.PLAYER2, Owner.PLAYER3, Owner.PLAYER4)
            1 -> listOf(Owner.PLAYER2, Owner.PLAYER3, Owner.PLAYER4, Owner.PLAYER1)
            2 -> listOf(Owner.PLAYER3, Owner.PLAYER4, Owner.PLAYER1, Owner.PLAYER2)
            else -> listOf()
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
        cardHandAdapter1 = CardHandMultiAdapter(
            value.filter { it.owner == owners[0] && it.position.name.contains("HAND") }.sortedBy { it.value },
            true,
            this
        )
        cardHandAdapter2 = CardHandMultiAdapter(
            value.filter { it.owner == owners[1] && it.position.name.contains("HAND") }.sortedBy { it.value },
            true,
            this
        )

        binding.poolPlayer1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.poolPlayer2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.poolPlayer1.adapter = cardHandAdapter1
        binding.poolPlayer2.adapter = cardHandAdapter2

        if (intent.extras!!.getBoolean("double")) {
            cardHandAdapter3 = CardHandMultiAdapter(
                value.filter { it.owner == owners[2] && it.position.name.contains("HAND") }.sortedBy { it.value },
                false,
                this
            )
            cardHandAdapter4 = CardHandMultiAdapter(
                value.filter { it.owner == owners[3] && it.position.name.contains("HAND") }.sortedBy { it.value },
                false,
                this
            )

            binding.poolPlayer3.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.poolPlayer4.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            binding.poolPlayer3.adapter = cardHandAdapter3
            binding.poolPlayer4.adapter = cardHandAdapter4
        } else {
            binding.poolPlayer3.visibility = View.GONE
            binding.poolPlayer4.visibility = View.GONE
        }
    }

    private fun loadTable(value: MutableList<Card>) {
        setupTable(1, value.filter { it.owner == owners[0] })
        setupTable(2, value.filter { it.owner == owners[1] })
        if (intent.extras!!.getBoolean("double")) {
            setupTable(3, value.filter { it.owner == owners[2] })
            setupTable(4, value.filter { it.owner == owners[3] })
        } else {
            binding.cardView1Player3.visibility = View.GONE
            binding.cardView2Player3.visibility = View.GONE
            binding.cardView3Player3.visibility = View.GONE
            binding.cardView1Player4.visibility = View.GONE
            binding.cardView2Player4.visibility = View.GONE
            binding.cardView3Player4.visibility = View.GONE
        }
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

    private fun changeImage(card: List<Card>, imageView: ImageView, adapter: CardHandMultiAdapter) {
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
                            viewModel.addToDiscard(listOf(cardUp), intent.extras!!.getBoolean("special"))
                            displayTurn()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                imageView.setImageResource(preferencesViewModel.loadDeck())
                if (adapter.itemCount == 0 && !lockActions) {
                    imageView.setOnClickListener {
                        val cardDown = card.single { v -> v.position.name.contains("DOWN") }
                        viewModel.addToDiscard(listOf(cardDown), intent.extras!!.getBoolean("special"))
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