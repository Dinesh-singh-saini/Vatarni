package com.vatarni

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vatarni.databinding.FragmentStatusBinding
import java.util.Date

class StatusFr : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var binding: FragmentStatusBinding
    private val userStatusList = ArrayList<UserStatus>()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadStatus(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.upload.setOnClickListener {
            getContent.launch("image/*")
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        statusAdapter = StatusAdapter(requireContext(), userStatusList)
        recyclerView.adapter = statusAdapter
    }

    private fun uploadStatus(uri: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val date: Date = Date()
        val reference: StorageReference = storage.reference.child("status").child(date.time.toString())

        reference.putFile(uri)
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { downloadUri ->
                    val userStatus = UserStatus("User Name", downloadUri.toString(), date.time, emptyArray())
                    userStatusList.add(userStatus)
                    statusAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
            }
    }
}
