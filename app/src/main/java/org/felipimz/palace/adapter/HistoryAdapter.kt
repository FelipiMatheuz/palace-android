package org.felipimz.palace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.felipimz.palace.R
import org.felipimz.palace.model.History
import org.felipimz.palace.util.getColorRes
import org.felipimz.palace.util.getGameMode
import org.felipimz.palace.util.getMatchDate
import org.felipimz.palace.util.getPlayerPosition

class HistoryAdapter(private val historyList: List<History>, val context: Context) :
    RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.history_item, parent, false)

        return HistoryHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val history = historyList[position]
        holder.tvPosition.text = getPlayerPosition(context, history.playerPosition)
        holder.tvGameMode.text = getGameMode(context, history.gameMode)
        holder.tvDate.text = getMatchDate(history.matchDate)
        holder.ivCrownPlace.setColorFilter(
            ContextCompat.getColor(context, getColorRes(history.playerPosition)),
            android.graphics.PorterDuff.Mode.ADD
        )
        //update adapter
        notifyItemChanged(position)
    }

    override fun getItemCount() = historyList.size

    class HistoryHolder(view: View) : RecyclerView.ViewHolder(view) {

        var ivCrownPlace: ImageView = view.findViewById(R.id.iv_crown_place)
        var tvPosition: TextView = view.findViewById(R.id.tv_position)
        var tvGameMode: TextView = view.findViewById(R.id.tv_gamemode)
        var tvDate: TextView = view.findViewById(R.id.tv_date)

    }
}