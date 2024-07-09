package com.vatarni

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vatarni.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)

        if (auth.currentUser != null) {
            val userReference = database.reference.child("users").child(auth.uid!!)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        setupNavigation(savedInstanceState)
                    } else {
                        startActivity(Intent(this@MainActivity, ProfileSetUp::class.java))
                        finishAffinity()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                            Log.e("FirebaseError", databaseError.message)
                }
            }
            )

        } else {
            startActivity(Intent(this@MainActivity, Welcome::class.java))
            finishAffinity()
        }
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Chats -> {
                    Toast.makeText(this, "Chats Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.status -> {
                    Toast.makeText(this, "Status Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.calls -> {
                    Toast.makeText(this, "Calls Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.edit_profile -> {
                    loadFragment(UserProfile())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.Chats
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.group -> {
                Toast.makeText(this, "Group Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.invite -> {
                Toast.makeText(this, "Invite Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.settings -> {
                Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.aboutDeveloper -> {
                Toast.makeText(this, "About Developer Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.privacyPolicy -> {
                Toast.makeText(this, "Privacy Policy Selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.logout -> {
                auth.signOut()
                Toast.makeText(this, "Logout Selected", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, Welcome::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
