package com.example.turkishidentifiescontrol

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.turkishidentifiescontrol.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel = HomeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonVerify.setOnClickListener {
            homeViewModel.verifyIdentity(
                binding.editTextFirstName.text.toString(),
                binding.editTextSurname.text.toString(),
                binding.editTextIdentityNumber.text.toString(),
                binding.editTextBirthYear.text.toString()
            )
        }

        homeViewModel.verificationResult.observe(this) { result ->
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }
    }
}