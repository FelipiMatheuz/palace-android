package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.felipimz.palace.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}