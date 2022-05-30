package org.felipimz.palace.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.felipimz.palace.model.Member
import org.felipimz.palace.model.Room

class LobbyViewModel : ViewModel() {

    private var _rooms = MutableLiveData<ArrayList<Room>>()
    private var _room = MutableLiveData<Room>()
    private var firestore = FirebaseFirestore.getInstance()
    var enabledCreateRoomButtom = MutableLiveData(true)

    init {
        listenRooms()
    }

    internal var getRooms: MutableLiveData<ArrayList<Room>>
        get() {
            return _rooms
        }
        set(value) {
            _rooms.value
        }

    internal var getRoom: MutableLiveData<Room>
        get() {
            return _room
        }
        set(value) {
            _room.value
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
                _rooms.value = rooms
            }
    }

    fun newRoom(room: Room): Boolean {
        var createdRoom = false
        firestore.collection("rooms")
            .add(room)
            .addOnSuccessListener {
                room.id = it.id
                updateRoom(room)
                createdRoom = true
            }
            .addOnFailureListener {
                createdRoom = false
            }
        return createdRoom
    }

    fun updateRoom(room: Room) {
        firestore.collection("rooms")
            .document(room.id).set(room)
    }

    fun getRoomDetails(id: String) {
        getRoom(id)
    }

    private fun getRoom(id: String) {
        val roomDB = firestore.collection("rooms").document(id).get()
        roomDB.addOnSuccessListener {
            val room = it.toObject(Room::class.java)
            _room.value = room
        }
    }

    fun checkStatusUser(itself: Member) {
        firestore.collection("rooms").whereArrayContains("members", itself).get().addOnSuccessListener {
            enabledCreateRoomButtom.value = it.isEmpty
        }
    }
}