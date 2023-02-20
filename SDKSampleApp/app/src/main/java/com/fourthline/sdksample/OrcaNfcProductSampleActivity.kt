package com.fourthline.sdksample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fourthline.orca.Orca
import com.fourthline.orca.nfc.*
import com.fourthline.sdksample.databinding.ActivityOrcaNfcSampleBinding
import java.util.*

class OrcaNfcProductSampleActivity : AppCompatActivity() {

    private val TAG: String = OrcaNfcProductSampleActivity::class.java.simpleName

    private lateinit var binding: ActivityOrcaNfcSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrcaNfcSampleBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupActionBar()
        setupButtons()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupButtons() = with(binding) {
        buttonDefaultFlow.setOnClickListener { buildNfcProduct() }
    }

    /**
     * Starts the Nfc Product.
     */
    private fun buildNfcProduct() {
        val scanDDL = false

        val nfcConfig: NfcConfig = if (scanDDL) {
            NfcConfig(keyToUnlockChip = IdlNfcUnlockKey("Document MRZ here..."))
        } else {
            NfcConfig(
                keyToUnlockChip = MrtdNfcUnlockKey(
                    documentNumber = "Document number here...",
                    birthDate = Date(),
                    expiryDate = Date(),
                ),
                documentType = MrtdNfcDocumentType.PASSPORT,
            )
        }

        Orca.nfc(this)
            .configure(config = nfcConfig)
            .present { nfcResult ->
                Log.d(TAG, "Finished Nfc Product with result: $nfcResult")

                Toast
                    .makeText(
                        this,
                        "Finished Nfc Product with result: $nfcResult",
                        Toast.LENGTH_LONG
                    )
                    .show()

                nfcResult.fold(
                    onSuccess = {
                        // Fill the KYCInfo object using the result.
                    },
                    onFailure = { error ->
                        when (error) {
                            is NfcError -> handleNfcError(error)
                            else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                        }
                    },
                )
            }
    }

    private fun handleNfcError(error: NfcError) {
        when (error) {
            is Canceled -> {
                // User canceled the Qes flow
            }
            is NfcNotSupported -> {
                // The device does not support NFC - there is no cheap.
            }
            is WrongUnlockKey -> {
                // The data provided to scanner is not matching the document presented by user.
            }
            is Unexpected -> {
                // Something wrong happened, please report to Fourthline and provide the `error.message`
            }
        }
    }
}