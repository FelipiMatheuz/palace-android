package org.felipimz.palace.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.felipimz.palace.R
import org.felipimz.palace.adapter.HistoryAdapter
import org.felipimz.palace.databinding.ActivityHistoryBinding
import org.felipimz.palace.repository.HistoryRepository
import org.felipimz.palace.repository.PreferencesRepository

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryRepository
    private lateinit var preferencesViewModel: PreferencesRepository
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = HistoryRepository(this)
        preferencesViewModel = PreferencesRepository(this)

        binding.tvHistoryNickname.text = preferencesViewModel.loadNickName()
        binding.tvMatchs.text = "${viewModel.getHistoryList().size} ${resources.getString(R.string.matches)}"
        binding.tvWins.text = "${
            viewModel.getHistoryList().filter {
                it.playerPosition == 1
            }.size
        } ${resources.getString(R.string.wins)}"

        historyAdapter = HistoryAdapter(viewModel.getHistoryList(), this)

        binding.rvHistory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvHistory.adapter = historyAdapter
    }
}