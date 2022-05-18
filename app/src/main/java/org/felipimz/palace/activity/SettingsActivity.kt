package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.felipimz.palace.R
import org.felipimz.palace.databinding.ActivitySettingsBinding
import org.felipimz.palace.model.Preferences
import org.felipimz.palace.viewmodel.PreferencesViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesViewModel: PreferencesViewModel
    private lateinit var cardAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesViewModel(this)
        binding.etNickname.setText(preferencesViewModel.loadNickName())
        initToggleButton()
        initDeck()
        initRadioRules()
        initButtons()
    }

    private fun initButtons() {
        binding.btnApply.setOnClickListener {
            val rules = if (binding.rbDefault.isChecked) {
                "default"
            } else {
                val cardRulesList: List<String> = listOf(
                    binding.spBurnPile.selectedItem.toString(),
                    binding.spReset.selectedItem.toString(),
                    binding.spForceDown.selectedItem.toString(),
                    binding.spReverse.selectedItem.toString()
                )
                if (cardRulesList.size == cardRulesList.distinct().size) {
                    cardRulesList.toString().substring(1, cardRulesList.toString().length - 1)
                        .replace(",", ";").replace(" ", "")
                } else {
                    "NA"
                }
            }
            if (rules != "NA") {
                preferencesViewModel.preferences = Preferences(
                    binding.etNickname.toString(),
                    binding.tbUseJoker.isChecked,
                    binding.cbWildcardSpecial.isChecked,
                    binding.cb4Player.isChecked,
                    rules,
                    binding.spDeck.selectedItemPosition
                )
                val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("nickname", binding.etNickname.text.toString())
                editor.putBoolean("deckWithJoker", binding.tbUseJoker.isChecked)
                editor.putBoolean("doubleDeck", binding.cb4Player.isChecked)
                editor.putBoolean("wildcardAsSpecial", binding.cbWildcardSpecial.isChecked)
                editor.putInt("card", binding.spDeck.selectedItemPosition)
                editor.putString("rules", rules)
                editor.apply()
                onBackPressed()
            } else {
                Toast.makeText(this, resources.getString(R.string.msg_multiple_effect_same_card), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnReset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.confirm_reset)
                .setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    deleteSharedPreferences("preferences")
                    onBackPressed()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    private fun initRadioRules() {
        val rules = preferencesViewModel.loadRules()
        if (rules == "default") {
            binding.rbDefault.isChecked = true
        } else {
            binding.rbCustom.isChecked = true
            val rulesItens = rules.split(";")
            binding.spBurnPile.setSelection(cardAdapter.getPosition(rulesItens[0]))
            binding.spReset.setSelection(cardAdapter.getPosition(rulesItens[1]))
            binding.spForceDown.setSelection(cardAdapter.getPosition(rulesItens[2]))
            binding.spReverse.setSelection(cardAdapter.getPosition(rulesItens[3]))
        }
        toogleCustomRules(binding.rbDefault.isChecked)
        binding.rbDefault.setOnCheckedChangeListener { _, isChecked ->
            toogleCustomRules(isChecked)
        }
    }

    private fun toogleCustomRules(checked: Boolean) {
        if (checked) {
            binding.spBurnPile.isEnabled = false
            binding.spReset.isEnabled = false
            binding.spForceDown.isEnabled = false
            binding.spReverse.isEnabled = false
        } else {
            binding.spBurnPile.isEnabled = true
            binding.spReset.isEnabled = true
            binding.spForceDown.isEnabled = true
            binding.spReverse.isEnabled = true
        }
    }

    private fun initDeck() {
        val decksArray = resources.getStringArray(R.array.decks_array).toList()
        val deckAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, decksArray)
        binding.spDeck.adapter = deckAdapter
        binding.spDeck.setSelection(preferencesViewModel.loadCard())
    }

    private fun initToggleButton() {
        binding.cbWildcardSpecial.isChecked = preferencesViewModel.loadWildCardAsSpecial()
        binding.cb4Player.isChecked = preferencesViewModel.loadDoubleDeck()
        binding.tbUseJoker.isChecked = preferencesViewModel.loadDeckWithJoker()
        checkToggleButton(binding.tbUseJoker, binding.tbUseJoker.isChecked)
        binding.tbUseJoker.setOnCheckedChangeListener { button, isChecked ->
            checkToggleButton(button, isChecked)
        }
    }

    private fun checkToggleButton(button: CompoundButton, isChecked: Boolean) {
        val cardsArray = resources.getStringArray(R.array.cards_array).toList()
        val color = if (isChecked) {
            resources.getColor(R.color.yellow_700, null)
        } else {
            resources.getColor(R.color.black, null)
        }
        button.setTextColor(color)
        cardAdapter = if (isChecked) {
            ArrayAdapter(this, android.R.layout.simple_spinner_item, cardsArray)
        } else {
            ArrayAdapter(this, android.R.layout.simple_spinner_item, cardsArray.filter { f -> !f.equals("Joker") })
        }
        binding.spBurnPile.adapter = cardAdapter
        binding.spReset.adapter = cardAdapter
        binding.spForceDown.adapter = cardAdapter
        binding.spReverse.adapter = cardAdapter
    }
}