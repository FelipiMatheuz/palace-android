package org.felipimz.palace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.felipimz.palace.R
import org.felipimz.palace.activity.MainActivity
import org.felipimz.palace.model.Card
import org.felipimz.palace.model.Owner
import org.felipimz.palace.model.Position

class CardHandAdapter(private var listCard: List<Card>, private val orientation: Boolean, val activity: MainActivity) :
    RecyclerView.Adapter<CardHandAdapter.CardHandHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHandHolder {

        val layoutResource = if (orientation) {
            R.layout.player_card_item_h
        } else {
            R.layout.player_card_item_v
        }

        val view = LayoutInflater
            .from(parent.context)
            .inflate(layoutResource, parent, false)

        return CardHandHolder(view)
    }

    override fun onBindViewHolder(holder: CardHandHolder, position: Int) {
        val card = listCard[position]
        val resId = activity.resources.getIdentifier(card.name, "drawable", activity.packageName)
        holder.ivCardItem.setImageResource(resId)
        holder.cvCard.foreground = if (card.position == Position.HAND_CLICKED) {
            activity.getDrawable(R.drawable.highlight)
        } else {
            null
        }

        if (listCard.isNotEmpty() && listCard[listCard.size - 1].owner == Owner.PLAYER1 && !activity.lockActions) {
            holder.cvCard.setOnClickListener {
                if (card.position == Position.HAND_CLICKED) {
                    activity.viewModel.addToDiscard(card)
                    activity.displayTurn(activity.viewModel.currentTurn)
                } else {
                    listCard.filter { c ->
                        c.position == Position.HAND_CLICKED
                    }.forEach { c ->
                        c.position = Position.HAND
                    }
                    card.position = Position.HAND_CLICKED
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount() = listCard.size

    class CardHandHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivCardItem: ImageView = view.findViewById(R.id.card_item)
        var cvCard: CardView = view.findViewById(R.id.card_cv)
    }
}