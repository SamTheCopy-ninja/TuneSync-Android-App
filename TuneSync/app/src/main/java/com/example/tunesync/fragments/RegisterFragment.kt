package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tunesync.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {
    // Fragment for registration

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Firebase auth for profile creation
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        // Register Button using View Binding
        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Create the user profile in Firebase
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                    // Navigate back to login
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}