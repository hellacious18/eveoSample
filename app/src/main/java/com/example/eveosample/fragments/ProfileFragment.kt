package com.example.eveosample.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.core.Amplify
import com.example.eveosample.MainActivity
import com.example.eveosample.R
import com.example.eveosample.adapters.OptionsAdapter

class ProfileFragment : Fragment(R.layout.fragment_profile){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString("EMAIL_KEY") ?: ""
        Log.d("ProfileFragment", "Received email: $email")

        val optionsList = listOf("Account", "Notifications", "Logout")
        val listView = view.findViewById<ListView>(R.id.listViewOptions)
        val adapter = OptionsAdapter(requireContext(), optionsList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    Toast.makeText(requireContext(), "Account clicked", Toast.LENGTH_SHORT).show()
                }

                1 -> {
                    Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT)
                        .show()
                }

                2 -> {
                    Amplify.Auth.signOut { signOutResult ->
                        when(signOutResult) {
                            is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                                // Sign Out completed fully and without errors.
                                Log.i("AuthQuickStart", "Signed out successfully")
//                        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                            is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                                // Sign Out completed with some errors. User is signed out of the device.
                                signOutResult.hostedUIError?.let {
                                    Log.e("AuthQuickStart", "HostedUI Error", it.exception)
                                    // Optional: Re-launch it.url in a Custom tab to clear Cognito web session.

                                }
                                signOutResult.globalSignOutError?.let {
                                    Log.e("AuthQuickStart", "GlobalSignOut Error", it.exception)
                                    // Optional: Use escape hatch to retry revocation of it.accessToken.
                                }
                                signOutResult.revokeTokenError?.let {
                                    Log.e("AuthQuickStart", "RevokeToken Error", it.exception)
                                    // Optional: Use escape hatch to retry revocation of it.refreshToken.
                                }
                            }
                            is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                                // Sign Out failed with an exception, leaving the user signed in.
                                Log.e("AuthQuickStart", "Sign out Failed", signOutResult.exception)
                            }
                        }
                    }
                }
            }
        }
    }
}