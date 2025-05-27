package com.ryggs.interview.orbit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
        const val EXTRA_FROM_ONBOARDING = "from_onboarding"
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_WAITING_FOR_DEFAULT_HOME = "waiting_for_default_home"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent categories: ${intent.categories}")
        Log.d(TAG, "Intent extras: ${intent.extras?.keySet()}")

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (shouldRedirectToOnboarding()) {
            Log.d(TAG, "Redirecting to onboarding immediately, not showing HomeActivity UI")
            redirectToOnboarding()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        setupHomeUI()
    }

    private fun shouldRedirectToOnboarding(): Boolean {
        val fromOnboarding = intent.getBooleanExtra(EXTRA_FROM_ONBOARDING, false)
        val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        val wasWaitingForDefaultHome = prefs.getBoolean(KEY_WAITING_FOR_DEFAULT_HOME, false)

        return fromOnboarding || (wasWaitingForDefaultHome && !onboardingCompleted)
    }

    private fun redirectToOnboarding() {
        val wasWaitingForDefaultHome = prefs.getBoolean(KEY_WAITING_FOR_DEFAULT_HOME, false)

        if (wasWaitingForDefaultHome) {
            clearWaitingForDefaultHome()
            val onboardingIntent = Intent(this, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(OnboardingActivity.EXTRA_FROM_HOME_SELECTION, true)
                putExtra("go_to_step_3", true)
            }
            startActivity(onboardingIntent)
        } else {
            val onboardingIntent = Intent(this, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(OnboardingActivity.EXTRA_FROM_HOME_SELECTION, true)
            }
            startActivity(onboardingIntent)
        }
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called")
        Log.d(TAG, "New intent action: ${intent.action}")
        Log.d(TAG, "New intent categories: ${intent.categories}")
        setIntent(intent)
        handleIntentAndSetup(intent)
    }

    private fun handleIntentAndSetup(intent: Intent) {
        val fromOnboarding = intent.getBooleanExtra(EXTRA_FROM_ONBOARDING, false)
        val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        val wasWaitingForDefaultHome = prefs.getBoolean(KEY_WAITING_FOR_DEFAULT_HOME, false)

        Log.d(TAG, "handleIntentAndSetup: fromOnboarding = $fromOnboarding, onboardingCompleted = $onboardingCompleted, wasWaitingForDefaultHome = $wasWaitingForDefaultHome")

        when {
            fromOnboarding -> {
                Log.d(TAG, "Launched from onboarding (normal flow), returning to onboarding activity")
                returnToOnboarding()
            }
            wasWaitingForDefaultHome && !onboardingCompleted -> {
                Log.d(TAG, "Was waiting for default home selection, returning to onboarding step 3")
                clearWaitingForDefaultHome()
                returnToOnboarding()
            }
            !onboardingCompleted -> {
                Log.d(TAG, "Set as default outside onboarding flow, showing home UI without launching onboarding")
                setupHomeUI()
            }
            else -> {
                Log.d(TAG, "Normal home launch, onboarding already completed")
                setupHomeUI()
            }
        }
    }

    private fun returnToOnboarding() {
        val onboardingIntent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(OnboardingActivity.EXTRA_FROM_HOME_SELECTION, true)
        }
        startActivity(onboardingIntent)
        finish()
    }

    private fun clearWaitingForDefaultHome() {
        Log.d(TAG, "Clearing waiting for default home flag")
        prefs.edit().putBoolean(KEY_WAITING_FOR_DEFAULT_HOME, false).apply()
    }

    private fun setupHomeUI() {
        Log.d(TAG, "Setting up HomeUI")
        supportActionBar?.title = "Home Launcher"
    }
}