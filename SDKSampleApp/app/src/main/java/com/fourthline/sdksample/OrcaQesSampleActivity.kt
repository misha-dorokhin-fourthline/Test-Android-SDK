package com.fourthline.sdksample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fourthline.networking.NetworkEnvironment
import com.fourthline.orca.Orca
import com.fourthline.orca.qes.*
import com.fourthline.sdksample.databinding.ActivityOrcaQesSampleBinding

class OrcaQesSampleActivity : AppCompatActivity() {

    private val TAG: String = OrcaQesSampleActivity::class.java.simpleName

    private lateinit var binding: ActivityOrcaQesSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrcaQesSampleBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupActionBar()
        setupButtons()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupButtons() = with(binding) {
        buttonTestMe.setOnClickListener { testQes() }
        buttonDefaultFlow.setOnClickListener { buildDefaultFlow() }
    }

    private fun testQes() {
        Orca.qes(this)
            .testMe()
    }

    /**
     * Starts the Default Qes flow.
     */
    private fun buildDefaultFlow() {
        val qesConfiguration = QesConfig(
            mobilePhoneNumber = "+4513123123",
            accessCodeSource = AccessCodeSource.Value("XXXXXX"),
            networkEnvironment = NetworkEnvironment.Mock,
        )

        Orca.qes(this)
            .configure(config = qesConfiguration)
            .present { qesResult ->
                Log.d(TAG, "Finished Qes Flow with result: $qesResult")

                Toast
                    .makeText(this, "Finished Qes Flow with result: $qesResult", Toast.LENGTH_LONG)
                    .show()

                qesResult.fold(
                    onSuccess = {
                        // User has successfully signed the documents
                    },
                    onFailure = { error ->
                        when (error) {
                            is QesError -> handleQesError(error)
                            else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                        }
                    },
                )
            }
    }

    private fun handleQesError(error: QesError) {
        when (error) {
            is Canceled -> {
                // User canceled the Qes flow
            }
            is InvalidAccessCode -> {
                // Handle by generating a new access code.
            }
            is KycRequired -> {
                // Handle by directing the user to the KYC flow.
            }
            is Rejected -> {
                // User was rejected during one of the QES flow steps.
            }
            is TooManyAuthorizationAttempts -> {
                // User tried to authorize documents too many times
            }
            is TooManyResendOtpAttempts -> {
                // User tried to request OTP code too many times
            }
            is Unexpected -> {
                // Something wrong happened, please report to Fourthline and provide the `error.message`
            }
        }
    }
}