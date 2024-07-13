package com.vatarni

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vatarni.databinding.ActivityOtpverificationBinding

class OTPVerification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String
    private lateinit var binding: ActivityOtpverificationBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupOtpFields()
        binding.otpMsg.text = "Successfully OTP sent to +91 $phoneNumber"
        binding.otp1.requestFocus()

        binding.verifyOtp.setOnClickListener {
            val otp = getEnteredOtp()
            if (otp.isNotEmpty() && otp.length == 6) {
                verifyCode(otp)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpFields() {
        val otpFields = arrayOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
            binding.otp5,
            binding.otp6
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < otpFields.size - 1) {
                            otpFields[i].clearFocus()
                            otpFields[i + 1].requestFocus()
                        } else {
                            binding.verifyOtp.performClick()
                        }
                    } else if (s?.length == 0 && before == 1) {
                        if (i > 0) {
                            otpFields[i].clearFocus()
                            otpFields[i - 1].requestFocus()
                            otpFields[i - 1].text?.clear()
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (otpFields[i].text.isNullOrEmpty() && i > 0) {
                        otpFields[i].clearFocus()
                        otpFields[i - 1].requestFocus()
                        otpFields[i - 1].text?.clear()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }


    private fun getEnteredOtp(): String {
        val otp1 = binding.otp1.text.toString().trim()
        val otp2 = binding.otp2.text.toString().trim()
        val otp3 = binding.otp3.text.toString().trim()
        val otp4 = binding.otp4.text.toString().trim()
        val otp5 = binding.otp5.text.toString().trim()
        val otp6 = binding.otp6.text.toString().trim()
        return otp1 + otp2 + otp3 + otp4 + otp5 + otp6
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("isVerified", true)
                        apply()
                    }
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                    val userReference = database.reference.child("users").child(auth.uid!!)
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                startActivity(Intent(this@OTPVerification, MainActivity::class.java))
                                finishAffinity()
                            } else {
                                startActivity(Intent(this@OTPVerification, ProfileSetUp::class.java))
                                finishAffinity()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
//                            Log.e("FirebaseError", databaseError.message)
                        }
                    }
                    )
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
