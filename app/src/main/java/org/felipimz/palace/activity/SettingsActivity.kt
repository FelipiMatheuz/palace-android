package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import org.felipimz.palace.R
import org.felipimz.palace.databinding.ActivitySettingsBinding
import org.felipimz.palace.model.Preferences
import org.felipimz.palace.viewmodel.PreferencesViewModel

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesViewModel: PreferencesViewModel
    private lateinit var cardAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesViewModel = PreferencesViewModel(this)
        binding.etNickname.setText(preferencesViewModel.loadNickName())
        initToogleButton()
        initDeck()
        initRadioRules()
        initButtons()
    }

    private fun initButtons() {
        binding.btnApply.setOnClickListener {
            val rules = if (binding.rbDefault.isChecked) {
                "default"
            } else {
                if (binding.spBurnPile.selectedItem != binding.spReset.selectedItem && binding.spBurnPile.selectedItem != binding.spForceDown.selectedItem
                    && binding.spBurnPile.selectedItem != binding.spReverse.selectedItem && binding.spReset.selectedItem != binding.spForceDown.selectedItem
                    && binding.spReset.selectedItem != binding.spReverse.selectedItem && binding.spForceDown.selectedItem != binding.spReverse.selectedItem
                ) {
                    "${binding.spBurnPile.selectedItem};${binding.spReset.selectedItem};${binding.spForceDown.selectedItem};${binding.spReverse.selectedItem}"
                } else {
                    "NA"
                }
            }
            if (rules != "NA") {
                preferencesViewModel.preferences = Preferences(
                    binding.etNickname.toString(),
                    binding.tbUseJoker.isChecked,
                    rules,
                    binding.spDeck.selectedItem.toString()
                )
                val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("nickname", binding.etNickname.text.toString())
                editor.putBoolean("deckWithJoker", binding.tbUseJoker.isChecked)
                editor.putString("card", binding.spDeck.selectedItem.toString())
                editor.putString("rules", rules)
                editor.apply()
                onBackPressed()
            } else {
                Toast.makeText(this, resources.getString(R.string.msg_multiple_effect_same_card), Toast.LENGTH_SHORT)
            }
        }

        binding.btnCancel.setOnClickListener {
            onBackPressed()
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
        val deckArray = listOf("blue", "red")
        val deckAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deckArray)
        binding.spDeck.adapter = deckAdapter
        binding.spDeck.setSelection(deckAdapter.getPosition(preferencesViewModel.loadCard()))
    }

    private fun initToogleButton() {
        binding.tbUseJoker.isChecked = preferencesViewModel.loadDeckWithJoker()
        checkToogleButton(binding.tbUseJoker, binding.tbUseJoker.isChecked)
        binding.tbUseJoker.setOnCheckedChangeListener { button, isChecked ->
            checkToogleButton(button, isChecked)
        }
    }

    private fun checkToogleButton(button: CompoundButton, isChecked: Boolean) {
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