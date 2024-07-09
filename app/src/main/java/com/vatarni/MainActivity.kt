package com.vatarni

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vatarni.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()

        val sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isVerified = sharedPref.getBoolean("isVerified", false)

        if (auth.currentUser != null && isVerified) {
            setContentView(binding.root)
        } else {
            Log.d("MainActivity", "User not verified or not signed in, redirecting to Verification activity")
            startActivity(Intent(this@MainActivity, Welcome::class.java))
            finishAffinity()
        }
        binding.textView.text = "Welcome ${auth.currentUser?.phoneNumber}"
    }
}