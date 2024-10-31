package com.example.tunesync.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.tunesync.R
import com.example.tunesync.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import java.util.concurrent.Executor

class LoginFragment : Fragment() {

    // Login fragment to handle authentication

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        // Check and request notification permission
        checkNotificationPermission()

        // Setup biometric login
        setupBiometricPrompt()

        // Fingerprint Login Button
        binding.fingerprintLoginButton.setOnClickListener {
            startBiometricAuthentication()
        }

        // Email/password login logic
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    loginUser(email, password)
                } else {
                    // Show Snack bar suggestion for biometric authentication
                    Snackbar.make(binding.root, "No internet connection. Use fingerprint authentication?", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Use Fingerprint") {
                            startBiometricAuthentication()
                        }
                        .show()
                }
            } else {
                Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerTextView.setOnClickListener {
            // Navigate to RegisterFragment
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Allow users to switch between Afrikaans and English
        binding.switchLanguageButton.setOnClickListener {
            val currentLocale = resources.configuration.locales[0].language
            val newLocale = if (currentLocale == "af") "en" else "af"
            setLocale(newLocale)
        }

        return view
    }

    // Check if the user device has access to the internet
    // Adapted from - source: https://medium.com/@manuchekhrdev/simple-ways-to-monitor-internet-connectivity-in-android-a3bef75bd3d9
    // Author - Manuchekhr Tursunov

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    // Biometric login using fingerprint authentication
    // Adapted from - source: https://developer.android.com/identity/sign-in/biometric-auth
    // Author - Developer.Android
    private fun startBiometricAuthentication() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Start authentication
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(context, "No biometric features available on this device", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(context, "Biometric features are currently unavailable", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(context, "No biometric credentials are currently enrolled", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(context, "A security update is required to use biometrics", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(context, "Biometric features are unsupported on this device", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Toast.makeText(context, "Biometric status is unknown", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Ask user for permission to send notifications
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Handle permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Switch the language
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart activity to apply changes
        requireActivity().recreate()
    }

    // Setup biometric authentication prompt
    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show()

                    // Handle successful authentication
                    findNavController().navigate(
                        R.id.action_loginFragment_to_homeFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .build()
                    )
                }


                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_login_title))
            .setSubtitle(getString(R.string.biometric_login_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_login_negative))
            .build()
    }

    // Authenticate the user then log them in using email and password
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        R.id.action_loginFragment_to_homeFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .build()
                    )
                } else {
                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
