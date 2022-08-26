package org.felipimz.palace.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import org.felipimz.palace.R
import org.felipimz.palace.model.Member
import org.felipimz.palace.model.Room
import org.felipimz.palace.model.Status

class LobbyViewModel : ViewModel() {

    internal var rooms = MutableLiveData<ArrayList<Room>>()
    internal var room = MutableLiveData<Room?>()
    private var userRoom: Room?
    private var roomListener: ListenerRegistration? = null
    private var firestore = FirebaseFirestore.getInstance()
    var enabledCreateRoomButtom = MutableLiveData(true)

    init {
        userRoom = null
        listenRooms()
    }

    private fun listenRooms() {
        firestore.collection("rooms")
            .orderBy("name", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val rooms = ArrayList<Room>()

                if (snapshot != null) {
                    val documents = snapshot.documents
                    documents.forEach {
                        val room = it.toObject(Room::class.java)
                        room!!.id = it.id
                        rooms.add(room)
                    }
                }
                this.rooms.value = rooms
            }
    }

    private fun listenRoom(roomId: String?) {
        if (roomId == null) {
            room.value = null
        } else {
            roomListener?.remove()
            roomListener = firestore.collection("rooms")
                .document(roomId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val room = snapshot.toObject(Room::class.java)
                        this.room.value = room
                    }
                }
        }
    }

    fun newRoom(room: Room, context: Context) {
        firestore.collection("rooms")
            .add(room)
            .addOnSuccessListener {
                room.id = it.id
                updateRoom(room)
                getRoomDetails(room.id)
                Toast.makeText(context, context.getString(R.string.room_created), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, context.getString(R.string.room_failed), Toast.LENGTH_SHORT).show()
            }

    }

    fun updateRoom(room: Room) {
        firestore.collection("rooms")
            .document(room.id).set(room)
        if (room.id == userRoom?.id) {
            userRoom = room
        }
    }

    private fun removeRoom(roomId: String) {
        room.value = null
        firestore.collection("rooms")
            .document(roomId).delete()
    }

    fun getRoomDetails(id: String) {
        getRoom(id)
    }

    private fun getRoom(id: String) {
        val roomDB = firestore.collection("rooms").document(id).get()
        roomDB.addOnSuccessListener {
            val room = it.toObject(Room::class.java)
            this.room.value = room
            listenRoom(this.room.value?.id)
        }
    }

    fun checkStatusUser(itself: Member) {
        firestore.collection("rooms").whereArrayContains("members", itself).get().addOnSuccessListener {
            enabledCreateRoomButtom.value = it.isEmpty
            userRoom = if (!it.isEmpty) {
                val room = it.documents[0]
                room.toObject(Room::class.java)
            } else {
                null
            }
        }
    }

    fun joinRoom(room: Room, member: Member) {
        leaveCurrentRoom(member)
        room.members.add(member)
        room.status = updateRoomStatus(room)
        updateRoom(room)
    }

    private fun updateRoomStatus(room: Room) = when (room.members.size) {
        2 -> Status.READY
        4 -> Status.FULL
        else -> Status.OPEN
    }

    fun leaveRoom(room: Room, member: Member) {
        room.members.remove(member)
        if (room.members.size == 0) {
            removeRoom(room.id)
        } else {
            room.status = updateRoomStatus(room)
            updateRoom(room)
        }
    }

    fun leaveCurrentRoom(member: Member) {
        if (userRoom != null) {
            leaveRoom(userRoom!!, member)
        }
    }

    fun isGameStarted(): Boolean {
        return userRoom?.status == Status.GAME
    }

    fun getGameId(): String{
        return userRoom!!.id
    }
}