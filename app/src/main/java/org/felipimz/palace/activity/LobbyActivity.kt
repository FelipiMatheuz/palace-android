package org.felipimz.palace.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import org.felipimz.palace.R
import org.felipimz.palace.databinding.ActivityLobbyBinding
import org.felipimz.palace.viewmodel.PreferencesViewModel

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesViewModel: PreferencesViewModel
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        preferencesViewModel = PreferencesViewModel(this)
    }

    public override fun onStart() {
        super.onStart()
        user = auth.currentUser
        if (user == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = task.result?.user
                        syncUserName()
                    } else {
                        Toast.makeText(
                            baseContext, getString(R.string.auth_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    }
                }
        } else {
            syncUserName()
        }
    }

    private fun syncUserName() {
        val profileUpdateName = userProfileChangeRequest {
            displayName = preferencesViewModel.loadNickName()
        }
        user?.updateProfile(profileUpdateName)
        binding.tvLobbyId.text = "ID: ${user?.displayName}"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        auth.currentUser?.delete()
        finish()
    }
}