package com.vatarni

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.vatarni.databinding.ActivityVerificationBinding
import java.util.concurrent.TimeUnit

class Verification : AppCompatActivity() {
    private lateinit var verificationId: String
    private lateinit var binding: ActivityVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this)
        dialog.setMessage("Sending OTP...")
        dialog.setCancelable(false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.inputNumber.requestFocus()

        binding.sendOtp.setOnClickListener {
            val phoneNumber = binding.inputNumber.text.toString().trim()
            if (phoneNumber.length == 10) {
                dialog.show()
                sendVerificationCode(phoneNumber)

            } else {
                binding.inputNumber.error = "Please enter a valid 10-digit number"
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                override fun onVerificationFailed(e: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(this@Verification, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@Verification.verificationId = verificationId
                    dialog.dismiss()
                    Toast.makeText(this@Verification, "OTP sent", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Verification, OTPVerification::class.java)
                    intent.putExtra("verificationId", verificationId).putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}
