package com.fourthline.sdksample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fourthline.core.CountryNetworkModel
import com.fourthline.core.DocumentType
import com.fourthline.orca.Orca
import com.fourthline.orca.document.*
import com.fourthline.sdksample.databinding.ActivityOrcaDocumentSampleBinding

class OrcaDocumentProductSampleActivity : AppCompatActivity() {

    private val TAG: String = OrcaDocumentProductSampleActivity::class.java.simpleName

    private lateinit var binding: ActivityOrcaDocumentSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrcaDocumentSampleBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupActionBar()
        setupButtons()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupButtons() = with(binding) {
        buttonTestMe.setOnClickListener { testDocumentProduct() }
        buttonDefaultFlow.setOnClickListener { buildDocumentProduct() }
    }

    private fun testDocumentProduct() {
        Orca.document(this)
            .testMe()
    }

    /**
     * Starts the Document Product.
     */
    private fun buildDocumentProduct() {
        val documentConfiguration = DocumentConfig(
            type = DocumentType.PASSPORT,
            supportedCountries = CountryNetworkModel.create(SUPPORTED_COUNTRIES_AS_STRING),
        )

        Orca.document(this)
            .configure(config = documentConfiguration)
            .present { documentResult ->
                Log.d(TAG, "Finished Document Product with result: $documentResult")

                Toast
                    .makeText(this, "Finished Document Product with result: $documentResult", Toast.LENGTH_LONG)
                    .show()

                documentResult.fold(
                    onSuccess = {
                        // Fill the KYCInfo object using the result.
                    },
                    onFailure = { error ->
                        when (error) {
                            is DocumentError -> handleDocumentError(error)
                            else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                        }
                    },
                )
            }
    }

    private fun handleDocumentError(error: DocumentError) {
        when (error) {
            is Canceled -> {
                // User canceled the Qes flow
            }
            is DocumentExpired -> {
                // Handle document expired. Example: Redirect to document type screen.
            }
            is DocumentTypeInvalid -> {
                // Invalid document type. Can occur if user scanned a document type that is disabled.
            }
            is DocumentTypeNotSupported -> {
                // Handle document type not supported. Example: Redirect to Document type screen.
            }
            is IssuingCountryNotSupported -> {
                // Handle issuing country not supported. Example: Redirect to Issuing country screen.
            }
            is NationalityNotSupported -> {
                // Handle nationality not supported.
            }
            is PersonNotAdult -> {
                // Handle person not adult. Example: Stop flow.
            }
            is Unexpected -> {
                // Something wrong happened, please report to Fourthline and provide the `error.message`
            }
        }
    }
}