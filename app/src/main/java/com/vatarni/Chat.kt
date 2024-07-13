package com.vatarni

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vatarni.databinding.FragmentChatBinding

class Chat : Fragment() {
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<UserData>
    private lateinit var binding: FragmentChatBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance() // Initialize Firebase database instance
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false) // Inflate the binding here

        userList = ArrayList()
        userAdapter = UserAdapter(this, userList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        // Load user data from Firebase
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user: UserData? = userSnapshot.getValue(UserData::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                // You can log the error or show a toast message
                // e.g., Log.e("Chat", "Database error: ${error.message}")
            }
        })

        return binding.root
    }
}
