package com.example.eveosample.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.core.Amplify
import com.example.eveosample.LoginActivity
import com.example.eveosample.MainActivity
import com.example.eveosample.R
import com.example.eveosample.adapters.OptionsAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class ProfileFragment : Fragment(R.layout.fragment_profile){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView5 = view.findViewById<TextView>(R.id.textView5)
        val listView = view.findViewById<ListView>(R.id.listViewOptions)

        //recieve email from shared pref
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val email = sharedPref.getString("EMAIL_KEY", "") ?: ""
        textView5.text = email

        val optionsList = listOf("Account", "Notifications", "Logout")
        val adapter = OptionsAdapter(requireContext(), optionsList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    Toast.makeText(requireContext(), "Account clicked", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    // Sign out from Amplify (Cognito)
                    Amplify.Auth.signOut { signOutResult ->
                        when (signOutResult) {
                            is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                                Log.i("AuthQuickStart", "Amplify sign-out successful")
                                // Now, sign out from Google
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()  // or add .requestIdToken(...) if needed
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    Log.i("AuthQuickStart", "Google sign-out completed")
                                    // Navigate back to MainActivity (or LoginActivity)
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                }
                            }
                            is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                                // Handle partial sign-out errors if needed
                                signOutResult.hostedUIError?.let {
                                    Log.e("AuthQuickStart", "HostedUI Error", it.exception)
                                }
                                signOutResult.globalSignOutError?.let {
                                    Log.e("AuthQuickStart", "GlobalSignOut Error", it.exception)
                                }
                                signOutResult.revokeTokenError?.let {
                                    Log.e("AuthQuickStart", "RevokeToken Error", it.exception)
                                }
                            }
                            is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                                Log.e("AuthQuickStart", "Amplify sign-out failed", signOutResult.exception)
                            }
                        }
                    }
                }
            }
        }

    }
}