package org.felipimz.palace.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        preferencesViewModel = PreferencesViewModel(this)

        adapter = LobbyAdapter(this)
        binding.rvRooms.adapter = adapter
        binding.rvRooms.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel = ViewModelProvider.NewInstanceFactory().create(LobbyViewModel::class.java)
        viewModel.getRooms.observe(this) {
            adapter.updateList(it)
            syncRooms()
            viewModel.checkStatusUser(Member(auth.uid!!, preferencesViewModel.loadNickName()))
        }

        viewModel.enabledCreateRoomButtom.observe(this) {
            binding.btnNewRoom.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.lobbyDetailsGroup.visibility = View.GONE
        binding.lobbySettingsGroup.visibility = View.GONE

        binding.btnNewRoom.setOnClickListener {
            viewDialogNewRoom()
        }
    }

    private fun viewDialogNewRoom() {
        val dialog = Dialog(this)
        dialog.setTitle(getString(R.string.new_room))
        dialog.setContentView(R.layout.dialog_new_room)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val etNewRoomName = dialog.findViewById<EditText>(R.id.et_new_room_name)
        val etNewRoomPassword = dialog.findViewById<EditText>(R.id.et_new_room_password)
        val btnCreateNewRoom = dialog.findViewById<Button>(R.id.btn_create_room)
        btnCreateNewRoom.setOnClickListener {
            val dialogResult = if (viewModel.newRoom(
                    Room(
                        name = etNewRoomName.text.toString(),
                        password = etNewRoomPassword.text.toString(),
                        members = mutableListOf(Member(auth.uid!!, preferencesViewModel.loadNickName()))
                    )
                )
            ) {
                getString(R.string.room_created)
            } else {
                getString(R.string.room_failed)
            }
            Toast.makeText(this, dialogResult, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    public override fun onStart() {
        super.onStart()
        syncUser()
        syncRooms()
    }

    private fun syncUser() {
        user = auth.currentUser
        if (user == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = task.result?.user
                        getUserID()
                    } else {
                        Toast.makeText(
                            baseContext, getString(R.string.auth_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    }
                }
        } else {
            getUserID()
        }
    }

    private fun getUserID() {
        binding.tvLobbyId.text = "ID: ${preferencesViewModel.loadNickName()}#${user?.uid.toString().substring(0, 5)}"
    }

    private fun syncRooms() {
        binding.tvLobbyRooms.text = "${getString(R.string.rooms)}: ${binding.rvRooms.adapter?.itemCount}"
    }

    fun listenRoomDetails(id: String) {
        viewModel.getRoomDetails(id)
        viewModel.getRoom.observe(this) {
            binding.tvLobbyRoomName.text = it.name
            val roomOwner = it.members[0]
            binding.tvLobbyMember1.text = roomOwner.displayName
            binding.tvLobbyMember2.text = if (it.members.size > 1) {
                it.members[1].displayName
            } else {
                ""
            }
            binding.tvLobbyMember3.text = if (it.members.size > 2) {
                it.members[2].displayName
            } else {
                ""
            }
            binding.tvLobbyMember4.text = if (it.members.size > 3) {
                it.members[3].displayName
            } else {
                ""
            }
            binding.cbUseJoker.isChecked = it.deckWithJokerMultiplayer
            binding.cbWildcardSpecial.isChecked = it.wildcardAsSpecialMultiplayer

            binding.lobbyDetailsGroup.visibility = View.VISIBLE
            binding.lobbySettingsGroup.visibility = View.VISIBLE

            if (user?.uid == it.members[0].id) {
                binding.tvLobbySettings.text = getString(R.string.lobby_room_settings)
                binding.cbUseJoker.isEnabled = true
                binding.cbWildcardSpecial.isEnabled = true
                binding.cbUseJoker.setOnCheckedChangeListener { _, isChecked ->
                    it.deckWithJokerMultiplayer = isChecked
                    viewModel.updateRoom(it)
                }
                binding.cbWildcardSpecial.setOnCheckedChangeListener { _, isChecked ->
                    it.wildcardAsSpecialMultiplayer = isChecked
                    viewModel.updateRoom(it)
                }
                binding.btnStart.visibility = View.VISIBLE
            } else {
                binding.tvLobbySettings.text = getString(R.string.lobby_room_details)
                binding.cbUseJoker.isEnabled = false
                binding.cbWildcardSpecial.isEnabled = false
                binding.btnStart.visibility = View.GONE
            }
        }
    }
}