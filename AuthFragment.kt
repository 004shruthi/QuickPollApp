package com.example.quickpollapp.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickpollapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var auth: FirebaseAuth
    private var isLoginMode = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailInput = view.findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = view.findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val confirmLayout = view.findViewById<View>(R.id.confirmPasswordLayout)
        val authButton = view.findViewById<MaterialButton>(R.id.authButton)
        val toggleText = view.findViewById<TextView>(R.id.toggleModeText)
        val titleText = view.findViewById<TextView>(R.id.titleText)

        // Toggle Login/Register Mode
        toggleText.setOnClickListener {
            isLoginMode = !isLoginMode

            if (isLoginMode) {
                titleText.text = "Login"
                authButton.text = "Login"
                confirmLayout.visibility = View.GONE
                toggleText.text = "Don't have an account? Register"
            } else {
                titleText.text = "Register"
                authButton.text = "Register"
                confirmLayout.visibility = View.VISIBLE
                toggleText.text = "Already have an account? Login"
            }
        }

        // Auth Button Click
        authButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Email Validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Enter a valid email address")
                return@setOnClickListener
            }

            // Password Validation
            val passwordPattern =
                Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()]).{6,}\$")

            if (!passwordPattern.matches(password)) {
                showToast("Password must contain:\n• 1 Capital Letter\n• 1 Number\n• 1 Special Character\n• Minimum 6 Characters")
                return@setOnClickListener
            }

            if (!isLoginMode) {
                // Register Mode
                if (password != confirmPassword) {
                    showToast("Passwords do not match")
                    return@setOnClickListener
                }
                registerUser(email, password)
            } else {
                // Login Mode
                loginUser(email, password)
            }
        }
    }

    // Auto-skip if already logged in & verified
    override fun onStart() {
        super.onStart()

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {

            // Reload user to get updated verification status
            currentUser.reload().addOnCompleteListener {

                if (currentUser.isEmailVerified) {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.sendEmailVerification()
                showToast("Verification email sent. Please check your inbox.")

                // Force logout until email is verified
                auth.signOut()
            }
            .addOnFailureListener {
                showToast(it.message ?: "Registration failed")
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    showToast("Please verify your email before logging in")
                    auth.signOut()
                }
            }
            .addOnFailureListener {
                showToast(it.message ?: "Login failed")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}