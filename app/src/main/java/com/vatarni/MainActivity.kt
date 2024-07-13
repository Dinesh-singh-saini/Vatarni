package com.vatarni

import android.content.Intent
import android.net.Uri
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
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            )

        } else {
            startActivity(Intent(this@MainActivity, Welcome::class.java))
            finishAffinity()
        }
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        loadFragment(Chat())

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Chats -> {
                    loadFragment(Chat())
                    binding.title.text = "Chats"
                    true
                }
                R.id.status -> {
                loadFragment(StatusFr())
                    binding.title.text = "Status"
                    true
                }
                R.id.calls -> {
                    binding.title.text = "Calls"
                    Toast.makeText(this, "Calls Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.edit_profile -> {
                    loadFragment(UserProfile())
                    binding.title.text = "Profile"
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
                openWebPage("https://d-s.netlify.app/")
                true
            }
            R.id.privacyPolicy -> {
                openWebPage("https://d-s.netlify.app/privacy_policy/")
                true
            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this@MainActivity, Welcome::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }
}

