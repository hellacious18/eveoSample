package com.example.eveosample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.example.eveosample.fragments.BeautyFragment
import com.example.eveosample.fragments.FashionFragment
import com.example.eveosample.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //passing email to the profile fragment
        val email = intent.getStringExtra("EMAIL_KEY") ?: ""
        val fragment = ProfileFragment()
        val bundle = Bundle()
        bundle.putString("EMAIL_KEY", email)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        if(savedInstanceState == null)
        {
            loadFragment(BeautyFragment())
        }

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_beauty -> loadFragment(BeautyFragment())
                R.id.nav_fashion -> loadFragment(FashionFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }
}