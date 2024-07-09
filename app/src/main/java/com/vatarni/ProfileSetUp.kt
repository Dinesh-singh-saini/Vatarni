package com.vatarni

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.vatarni.databinding.ActivityProfileSetUpBinding
import java.io.ByteArrayOutputStream

class ProfileSetUp : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri? = null
    private lateinit var dialog: ProgressDialog
    private var resizedImageByteArray: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileSetUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading profile...")
        dialog.setCancelable(false)

        binding.editProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
    }

    private fun resizeImageForProfile(context: Context, imageUri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val profileWidth = 500
            val profileHeight = 500

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, profileWidth, profileHeight, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && requestCode == 100 && resultCode == RESULT_OK) {
            selectedImage = data.data
            selectedImage?.let {
                resizedImageByteArray = resizeImageForProfile(this, it)
                binding.userProfilePicture.setImageURI(it)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    fun finish(view: View) {
        val name: String = binding.fullName.text.toString()
        if (name.isEmpty()) {
            binding.fullName.error = "Enter your name"
            return
        }
        dialog.show()

        if (resizedImageByteArray != null) {
            val ref = storage.reference.child("profile").child(auth.uid!!)
            ref.putBytes(resizedImageByteArray!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val profilePictureUrl = uri.toString()
                        val bio = "Hey I'm using Vatarni"

                        val userData = UserData(
                            name, auth.currentUser?.phoneNumber, auth.currentUser?.email,
                            profilePictureUrl, bio
                        )

                        database.reference.child("users").child(auth.uid!!).setValue(userData)
                            .addOnSuccessListener {
                                dialog.dismiss()
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                dialog.dismiss()
                                Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener { exception ->
                        dialog.dismiss()
                        Toast.makeText(this, "Failed to get profile picture URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    dialog.dismiss()
                    Toast.makeText(this, "Failed to upload profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val bio = "Hey I'm using Vatarni"
            val userData = UserData(
                name, auth.currentUser?.phoneNumber, auth.currentUser?.email, null, bio
            )

            database.reference.child("users").child(auth.uid!!).setValue(userData)
                .addOnSuccessListener {
                    dialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    dialog.dismiss()
                    Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
