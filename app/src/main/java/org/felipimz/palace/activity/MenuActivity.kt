package org.felipimz.palace.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
            Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show()
        }

        binding.history.setOnClickListener {
            val historyIntent = Intent(this, HistoryActivity::class.java)
            startActivity(historyIntent)
        }

        binding.settings.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }
}