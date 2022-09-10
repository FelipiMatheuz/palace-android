package org.felipimz.palace.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.felipimz.palace.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.singleplayer.setOnClickListener {
            val singleplayerIntent = Intent(this, MainActivity::class.java)
            startActivity(singleplayerIntent)
        }

        binding.multiplayer.setOnClickListener {
            val multiplayerIntent = Intent(this, LobbyActivity::class.java)
            startActivity(multiplayerIntent)
        }

        binding.history.setOnClickListener {
            val historyIntent = Intent(this, HistoryActivity::class.java)
            startActivity(historyIntent)
        }

        binding.settings.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        binding.ivHelp.setOnClickListener {
            val helpIntent = Intent(this, HelpActivity::class.java)
            startActivity(helpIntent)
        }
    }
}