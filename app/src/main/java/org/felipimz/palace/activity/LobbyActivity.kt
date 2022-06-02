package org.felipimz.palace.activity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.felipimz.palace.R
import org.felipimz.palace.adapter.LobbyAdapter
import org.felipimz.palace.databinding.ActivityLobbyBinding
import org.felipimz.palace.model.Member
import org.felipimz.palace.model.Room
import org.felipimz.palace.viewmodel.LobbyViewModel
import org.felipimz.palace.viewmodel.PreferencesViewModel

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var adapter: LobbyAdapter
    private lateinit var viewModel: LobbyViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesViewModel: PreferencesViewModel
    private var user: FirebaseUser? = null
    private lateinit var userMember: Member

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        preferencesViewModel = PreferencesViewModel(this)

        adapter = LobbyAdapter(this)
        binding.rvRooms.adapter = adapter
        binding.rvRooms.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.lobbyDetailsGroup.visibility = View.GONE
        binding.lobbySettingsGroup.visibility = View.GONE

        userMember = Member(auth.uid!!, preferencesViewModel.loadNickName())
        viewModel = ViewModelProvider.NewInstanceFactory().create(LobbyViewModel::class.java)
        observeLeftScreen()
        observeRightScreen()

        binding.btnNewRoom.setOnClickListener {
            viewDialogNewRoom()
        }
    }

    public override fun onStart() {
        super.onStart()
        syncUser()
    }

    override fun onStop() {
        viewModel.leaveCurrentRoom(userMember)
        super.onStop()
    }

    override fun onBackPressed() {
        viewModel.leaveCurrentRoom(userMember)
        super.onBackPressed()
    }

    private fun observeLeftScreen() {
        viewModel.getRooms.observe(this) {
            adapter.updateList(it)
            syncRooms()
            viewModel.checkStatusUser(userMember)
        }

        viewModel.enabledCreateRoomButtom.observe(this) {
            binding.btnNewRoom.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun observeRightScreen() {
        viewModel.getRoom.observe(this) { room ->

            if (room == null || room.id.isEmpty()) {
                binding.lobbyDetailsGroup.visibility = View.GONE
                binding.lobbySettingsGroup.visibility = View.GONE
                return@observe
            }

            binding.tvLobbyRoomName.text = room.name
            val roomOwner = room.members[0]
            binding.tvLobbyMember1.text = roomOwner.displayName
            binding.tvLobbyMember2.text = if (room.members.size > 1) {
                room.members[1].displayName
            } else {
                ""
            }
            binding.tvLobbyMember3.text = if (room.members.size > 2) {
                room.members[2].displayName
            } else {
                ""
            }
            binding.tvLobbyMember4.text = if (room.members.size > 3) {
                room.members[3].displayName
            } else {
                ""
            }
            binding.cbUseJoker.isChecked = room.deckWithJokerMultiplayer
            binding.cbWildcardSpecial.isChecked = room.wildcardAsSpecialMultiplayer

            binding.lobbyDetailsGroup.visibility = View.VISIBLE
            binding.lobbySettingsGroup.visibility = View.VISIBLE

            if (user?.uid == roomOwner.id) {
                binding.tvLobbySettings.text = getString(R.string.lobby_room_settings)
                binding.cbUseJoker.isEnabled = true
                binding.cbWildcardSpecial.isEnabled = true
                binding.cbUseJoker.setOnCheckedChangeListener { _, isChecked ->
                    room.deckWithJokerMultiplayer = isChecked
                    viewModel.updateRoom(room)
                }
                binding.cbWildcardSpecial.setOnCheckedChangeListener { _, isChecked ->
                    room.wildcardAsSpecialMultiplayer = isChecked
                    viewModel.updateRoom(room)
                }
                binding.btnStart.visibility = View.VISIBLE

                binding.btnJoinLeave.text = getString(R.string.leave)
                binding.btnJoinLeave.setOnClickListener {
                    viewModel.leaveRoom(room, userMember)
                }
            } else {
                binding.tvLobbySettings.text = getString(R.string.lobby_room_details)
                binding.cbUseJoker.isEnabled = false
                binding.cbWildcardSpecial.isEnabled = false
                binding.cbUseJoker.setOnCheckedChangeListener { _, _ ->
                    //Nothing
                }
                binding.cbWildcardSpecial.setOnCheckedChangeListener { _, _ ->
                    //Nothing
                }
                binding.btnStart.visibility = View.GONE

                val isMember = room.members.contains(userMember)

                if (isMember) {
                    binding.btnJoinLeave.text = getString(R.string.leave)
                    binding.btnJoinLeave.setOnClickListener {
                        viewModel.leaveRoom(room, userMember)
                    }
                } else {
                    binding.btnJoinLeave.text = getString(R.string.join_room)
                    binding.btnJoinLeave.setOnClickListener {
                        if (room.password.trim().isNotEmpty()) {
                            viewDialogViewRoom(room)
                        } else {
                            viewModel.joinRoom(room, userMember)
                        }
                    }
                }
            }
        }
    }

    private fun viewDialogNewRoom() {
        val dialog = Dialog(this)
        dialog.setTitle(getString(R.string.new_room))
        dialog.setContentView(R.layout.dialog_new_room)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val etNewRoomName = dialog.findViewById<EditText>(R.id.et_new_room_name)
        val etNewRoomPassword = dialog.findViewById<EditText>(R.id.et_new_room_password)
        val btnCreateNewRoom = dialog.findViewById<Button>(R.id.btn_create_room)
        btnCreateNewRoom.setOnClickListener {
            viewModel.newRoom(
                Room(
                    name = etNewRoomName.text.toString(),
                    password = etNewRoomPassword.text.toString(),
                    members = mutableListOf(userMember)
                ), this
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun syncUser() {
        user = auth.currentUser
        if (user == null) {
            auth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = task.result?.user
                    getUserID()
                } else {
                    Toast.makeText(
                        baseContext, getString(R.string.auth_failed), Toast.LENGTH_SHORT
                    ).show()
                    onBackPressed()
                }
            }
        } else {
            getUserID()
        }
    }

    private fun getUserID() {
        binding.tvLobbyId.text = "ID: ${userMember.displayName}#${userMember.id.substring(0, 5)}"
    }

    private fun syncRooms() {
        binding.tvLobbyRooms.text = "${getString(R.string.rooms)}: ${binding.rvRooms.adapter?.itemCount}"
    }

    fun getRoomDetails(id: String) {
        viewModel.getRoomDetails(id)
    }

    private fun viewDialogViewRoom(room: Room) {
        val dialog = Dialog(this)
        dialog.setTitle(getString(R.string.join_room))
        dialog.setContentView(R.layout.dialog_password)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val etViewRoomPassword = dialog.findViewById<EditText>(R.id.et_view_room_password)
        val btnViewRoom = dialog.findViewById<Button>(R.id.btn_view_room)
        btnViewRoom.setOnClickListener {
            if (etViewRoomPassword.text.toString() == room.password) {
                viewModel.joinRoom(room, userMember)
            } else {
                Toast.makeText(this, getString(R.string.password_fail), Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}