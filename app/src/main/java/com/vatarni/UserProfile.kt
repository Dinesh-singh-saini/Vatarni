package com.vatarni

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.vatarni.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UserProfile : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var selectedImage: Uri? = null
    private var progressDialog: ProgressDialog? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.userPicture.setImageURI(it)
            selectedImage = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchProfileData()
        makeTextViewEditable(binding.userName)
        makeTextViewEditable(binding.userMail)
        makeTextViewEditable(binding.userBio)

        binding.editProfile.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.save.setOnClickListener {
            saveProfile()
        }
    }

    private fun makeTextViewEditable(textView: TextView) {
        textView.setOnClickListener {
            val originalId = textView.id
            val editText = EditText(requireContext()).apply {
                id = originalId
                layoutParams = textView.layoutParams
                setText(textView.text)
                setSelection(textView.text.length)
                inputType = EditorInfo.TYPE_CLASS_TEXT
            }

            // Replace TextView with EditText
            val parent = textView.parent as ViewGroup
            val index = parent.indexOfChild(textView)
            parent.removeView(textView)
            parent.addView(editText, index)

            // Focus on the EditText and show the keyboard
            editText.requestFocus()
            editText.postDelayed({
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val newTextView = TextView(requireContext()).apply {
                        id = originalId
                        layoutParams = editText.layoutParams
                        text = editText.text
                    }

                    // Replace EditText with TextView
                    parent.removeView(editText)
                    parent.addView(newTextView, index)

                    // Update data in Firebase
                    updateUserField(newTextView.id, editText.text.toString())

                    // Hide the keyboard
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)

                    // Reapply the click listener to the new TextView
                    makeTextViewEditable(newTextView)

                    true
                } else {
                    false
                }
            }
        }
    }

    private fun updateUserField(id: Int, value: String) {
        val user = auth.currentUser
        val field = when (id) {
            R.id.userName -> "name"
            R.id.userMail -> "email"
            R.id.userNumber -> "phoneNumber"
            R.id.userBio -> "bio"
            else -> return
        }
        user?.let {
            val userRef = database.reference.child("users").child(it.uid)
            userRef.child(field).setValue(value)
                .addOnSuccessListener {
                    showToast("Profile updated successfully")
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to update profile: ${exception.message}")
                }
        }
    }

    private fun fetchProfileData() {
        val user = auth.currentUser
        user?.let {
            val userRef = database.reference.child("users").child(it.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(UserData::class.java)
                        userData?.let { data -> updateUI(data) }
                    } else {
                        showToast("No profile data found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to fetch profile data: ${error.message}")
                }
            })
        }
    }

    private fun updateUI(userData: UserData) {
        _binding?.let { binding ->
            binding.userName.text = userData.getName()
            binding.userMail.text = userData.getEmail() ?: getString(R.string.enter_mail)
            binding.userBio.text = userData.getBio() ?: getString(R.string.bio)
            binding.userNumber.text = userData.getPhoneNumber()
            loadProfilePicture(userData.getProfilePictureUrl())
        }
    }

    private fun loadProfilePicture(imageUrl: String?) {
        val imageView = binding.userPicture
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.user)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.user)
        }
    }

    private fun saveProfile() {
        val name = binding.userName.text.toString()
        val bio = binding.userBio.text.toString()
        val mail = binding.userMail.text.toString()

        // Validate name field
        if (name.isEmpty()) {
            binding.userName.error = "Enter your name"
            return
        }

        val user = auth.currentUser
        user?.let {
            // Show a progress dialog
            showProgressDialog()

            // Build a confirmation dialog
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to update your profile?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    lifecycleScope.launch {
                        try {
                            if (selectedImage != null) {
                                val resizedImage = resizeImageForProfile(requireContext(), selectedImage!!)
                                uploadProfilePicture(name, bio, mail, resizedImage)
                            } else {
                                val userData = UserData(name, it.phoneNumber, mail, null, bio)
                                updateUserProfile(userData)
                            }
                        } catch (e: Exception) {
                            showToast("Failed to update profile: ${e.message}")
                        } finally {
                            dismissProgressDialog()
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    dismissProgressDialog()
                }
                .create()
                .show()
        }
    }

    private fun resizeImageForProfile(context: Context, imageUri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val profileWidth = 500
        val profileHeight = 500

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, profileWidth, profileHeight, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        return outputStream.toByteArray()
    }

    private suspend fun uploadProfilePicture(name: String, bio: String, mail: String, imageBytes: ByteArray) {
        val user = auth.currentUser
        user?.let {
            val ref = storage.reference.child("profile").child(it.uid)
            ref.putBytes(imageBytes).await()
            val uri = ref.downloadUrl.await()
            val profilePictureUrl = uri.toString()
            val userData = UserData(name, it.phoneNumber, mail, profilePictureUrl, bio)
            updateUserProfile(userData)
        }
    }

    private suspend fun updateUserProfile(userData: UserData) {
        val userRef = database.reference.child("users").child(auth.uid!!)
        userRef.setValue(userData).await()
        showToast("Profile updated successfully")
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Updating profile...")
            setCancelable(false)
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dismissProgressDialog()
    }
}
