package com.fourthline.sdksample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fourthline.orca.Orca
import com.fourthline.orca.selfie.*
import com.fourthline.sdksample.databinding.ActivityOrcaSelfieSampleBinding

class OrcaSelfieProductSampleActivity : AppCompatActivity() {

    private val TAG: String = OrcaSelfieProductSampleActivity::class.java.simpleName

    private lateinit var binding: ActivityOrcaSelfieSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrcaSelfieSampleBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupActionBar()
        setupButtons()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupButtons() = with(binding) {
        buttonDefaultFlow.setOnClickListener { buildSelfieProduct() }
    }

    /**
     * Starts the Selfie Product.
     */
    private fun buildSelfieProduct() {
        Orca.selfie(this)
            .configure()
            .present { selfieResult ->
                Log.d(TAG, "Finished Selfie Product with result: $selfieResult")

                Toast
                    .makeText(
                        this,
                        "Finished Selfie Product with result: $selfieResult",
                        Toast.LENGTH_LONG
                    )
                    .show()

                selfieResult.fold(
                    onSuccess = {
                        // Fill the KYCInfo object using the result.
                    },
                    onFailure = { error ->
                        when (error) {
                            is SelfieError -> handleSelfieError(error)
                            else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                        }
                    },
                )
            }
    }

    private fun handleSelfieError(error: SelfieError) {
        when (error) {
            is Canceled -> {
                // User canceled the Qes flow
            }
            is Unexpected -> {
                // Something wrong happened, please report to Fourthline and provide the `error.message`
            }
        }
    }
}
