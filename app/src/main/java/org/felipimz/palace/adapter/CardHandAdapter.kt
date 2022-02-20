package org.felipimz.palace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.felipimz.palace.R
import org.felipimz.palace.model.Card

class CardHandAdapter(val listCard: List<Card>, val orientation: Boolean, val context: Context) :
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

        val resId = context.resources.getIdentifier(card.name, "drawable", context.packageName)
        holder.ivCardItem.setImageResource(resId)

        holder.cvCard.setOnClickListener {
            Toast.makeText(context, card.name, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = listCard.size

    class CardHandHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivCardItem = view.findViewById<ImageView>(R.id.card_item)
        var cvCard = view.findViewById<CardView>(R.id.card_cv)
    }
}