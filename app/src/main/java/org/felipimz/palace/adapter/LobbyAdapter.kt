package org.felipimz.palace.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.felipimz.palace.R
import org.felipimz.palace.activity.LobbyActivity
import org.felipimz.palace.model.Room

class LobbyAdapter(val activity: LobbyActivity) : RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder>() {

    private var rooms = emptyList<Room>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<Room>) {
        rooms = list
        notifyDataSetChanged()
    }

    class LobbyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvRoomName: TextView = view.findViewById(R.id.tv_room_name)
        val tvRoomMembers: TextView = view.findViewById(R.id.tv_room_members)
        val tvRoomStatus: TextView = view.findViewById(R.id.tv_room_status)
        val ivRoomArrow: ImageView = view.findViewById(R.id.iv_room_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobbyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.room_item, parent, false)

        return LobbyViewHolder(view)
    }

    override fun onBindViewHolder(holder: LobbyViewHolder, position: Int) {

        val room = rooms[position]

        holder.tvRoomName.text = room.name
        holder.tvRoomMembers.text = "${room.members.size}/4"
        holder.tvRoomStatus.text = room.status.toString()

        holder.ivRoomArrow.setOnClickListener {
            activity.listenRoomDetails(room.id)
        }
    }

    override fun getItemCount(): Int {
        return rooms.size
    }
}