package org.felipimz.palace.activity

import android.R.layout.simple_spinner_item
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.felipimz.palace.R
import org.felipimz.palace.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButtons()
    }

    private fun initButtons() {
        val helpAdapter = ArrayAdapter(
            this,
            simple_spinner_item,
            resources.getStringArray(R.array.help_topics_array).toList()
        )
        binding.spHelpHeader.adapter = helpAdapter
        binding.spHelpHeader.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                changeContent(position)
                checkNavButtons(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //Nothing
            }
        }

        binding.tvPrevious.setOnClickListener {
            binding.spHelpHeader.setSelection(binding.spHelpHeader.selectedItemPosition - 1)
        }
        binding.tvNext.setOnClickListener {
            binding.spHelpHeader.setSelection(binding.spHelpHeader.selectedItemPosition + 1)
        }
    }

    private fun checkNavButtons(page: Int) {
        if (page == 0) {
            binding.tvPrevious.visibility = View.INVISIBLE
            binding.tvPrevious.isEnabled = false
        } else {
            binding.tvPrevious.visibility = View.VISIBLE
            binding.tvPrevious.isEnabled = true
        }

        if (page == binding.spHelpHeader.adapter.count - 1) {
            binding.tvNext.visibility = View.INVISIBLE
            binding.tvNext.isEnabled = false
        } else {
            binding.tvNext.visibility = View.VISIBLE
            binding.tvNext.isEnabled = true
        }
    }

    private fun changeContent(page: Int) {
        val content = when (page) {
            0 -> R.string.game_objective
            1 -> R.string.game_setup
            2 -> R.string.starting_the_game
            3 -> R.string.ending_the_game
            4 -> R.string.card_ranking
            5 -> R.string.wildcards
            else -> R.string.game_customization
        }
        binding.tvHelpContent.text = getText(content)
    }
}