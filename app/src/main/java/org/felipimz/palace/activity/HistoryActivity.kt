package org.felipimz.palace.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.felipimz.palace.R
import org.felipimz.palace.adapter.HistoryAdapter
import org.felipimz.palace.databinding.ActivityHistoryBinding
import org.felipimz.palace.viewmodel.HistoryViewModel
import org.felipimz.palace.viewmodel.PreferencesViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var preferencesViewModel: PreferencesViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = HistoryViewModel(this)
        preferencesViewModel = PreferencesViewModel(this)

        binding.tvHistoryNickname.text = preferencesViewModel.loadNickName()
        binding.tvMatchs.text = "${viewModel.getHistoryList().size} ${resources.getString(R.string.matches)}"
        binding.tvWins.text = "${
            viewModel.getHistoryList().filter {
                it.playerPosition == 1
            }.size
        } ${resources.getString(R.string.wins)}"

        historyAdapter = HistoryAdapter(
            viewModel.getHistoryList().sortedByDescending {
                LocalDateTime.parse(
                    it.matchDate,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                )
            }, this
        )

        binding.rvHistory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvHistory.adapter = historyAdapter
    }
}