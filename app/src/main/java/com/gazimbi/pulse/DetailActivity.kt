package com.gazimbi.pulse

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gazimbi.pulse.databinding.ActivityDetailBinding
import com.gazimbi.pulse.viewmodels.SignalStrengthVM

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val signalStrengthVM: SignalStrengthVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val parameterName = intent.getStringExtra("PARAMETER_NAME")
        binding.title.text = parameterName

        // Safely handle parameter-specific logic
        when (parameterName) {
            "Signal Strength" -> {
                signalStrengthVM.measureSignalStrength(this)
                signalStrengthVM.signalStrength.observe(this) { result ->
                    binding.result.text = "Signal Strength: $result dBm"
                }
            }
            else -> {
                // Placeholder for other parameters
                binding.result.text = "Measurement for $parameterName is not implemented yet."
            }
        }
    }
}