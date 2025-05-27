package com.ryggs.interview.orbit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.ryggs.interview.orbit.Fragments.OnboardingStep1Fragment
import com.ryggs.interview.orbit.Fragments.OnboardingStep2Fragment
import com.ryggs.interview.orbit.Fragments.OnboardingStep3Fragment

class OnboardingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OnboardingActivity"
        const val EXTRA_FROM_HOME_SELECTION = "from_home_selection"
        const val EXTRA_GO_TO_STEP_3 = "go_to_step_3"
        private const val STATE_CURRENT_STEP = "current_step"
        private const val STATE_WAITING_FOR_DEFAULT = "waiting_for_default"
        private const val REQUEST_SET_DEFAULT_HOME = 1001
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_WAITING_FOR_DEFAULT_HOME = "waiting_for_default_home"
    }

    private var currentStep = 1
    private var isWaitingForDefaultHome = false
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        Log.d(TAG, "onCreate called")

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        savedInstanceState?.let {
            currentStep = it.getInt(STATE_CURRENT_STEP, 1)
            isWaitingForDefaultHome = it.getBoolean(STATE_WAITING_FOR_DEFAULT, false)
            Log.d(TAG, "onCreate: restored currentStep = $currentStep, isWaitingForDefaultHome = $isWaitingForDefaultHome")
        }

        if (intent.getBooleanExtra(EXTRA_FROM_HOME_SELECTION, false)) {
            Log.d(TAG, "Returning from home selection")

            if (intent.getBooleanExtra(EXTRA_GO_TO_STEP_3, false)) {
                Log.d(TAG, "Going directly to step 3 after home selection")
                currentStep = 3
                clearWaitingForDefaultHome()
                showStep(currentStep)
                return
            }

            if (isSetAsDefaultHome()) {
                Log.d(TAG, "App was set as default, going to step 3")
                currentStep = 3
                clearWaitingForDefaultHome()
                showStep(currentStep)
            } else {
                Log.d(TAG, "App was not set as default, staying on step 2")
                currentStep = 2
                showStep(currentStep)
            }
            return
        }

        if (isSetAsDefaultHome()) {
            Log.d(TAG, "Already set as default home")
            val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

            if (!onboardingCompleted) {
                Log.d(TAG, "User set as default outside flow, manually returned - going to step 3")
                currentStep = 3
                showStep(currentStep)
            } else {
                Log.d(TAG, "Onboarding already completed, finishing")
                finish()
                return
            }
        } else {
            showStep(currentStep)
        }
    }

    private fun markOnboardingCompleted() {
        Log.d(TAG, "Marking onboarding as completed")
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    private fun setWaitingForDefaultHome() {
        Log.d(TAG, "Setting waiting for default home flag")
        prefs.edit().putBoolean(KEY_WAITING_FOR_DEFAULT_HOME, true).apply()
    }

    private fun clearWaitingForDefaultHome() {
        Log.d(TAG, "Clearing waiting for default home flag")
        prefs.edit().putBoolean(KEY_WAITING_FOR_DEFAULT_HOME, false).apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: currentStep = $currentStep, isWaitingForDefaultHome = $isWaitingForDefaultHome")
        outState.putInt(STATE_CURRENT_STEP, currentStep)
        outState.putBoolean(STATE_WAITING_FOR_DEFAULT, isWaitingForDefaultHome)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - currentStep: $currentStep, isWaitingForDefaultHome: $isWaitingForDefaultHome")

        if (!intent.getBooleanExtra(EXTRA_FROM_HOME_SELECTION, false)) {
            if (isWaitingForDefaultHome) {
                isWaitingForDefaultHome = false
                clearWaitingForDefaultHome()

                Log.d(TAG, "Was waiting for default home selection, proceeding to step 3")
                currentStep = 3
                showStep(currentStep)
            }
        }
    }

    private fun showStep(step: Int) {
        Log.d(TAG, "showStep: $step")
        val fragment: Fragment = when (step) {
            1 -> OnboardingStep1Fragment()
            2 -> OnboardingStep2Fragment()
            3 -> OnboardingStep3Fragment()
            else -> {
                Log.w(TAG, "Unknown step: $step, defaulting to step 1")
                OnboardingStep1Fragment()
            }
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }

    fun goToNextStep() {
        Log.d(TAG, "goToNextStep called from step: $currentStep")

        when (currentStep) {
            1 -> {
                currentStep = 2
                showStep(currentStep)
            }
            2 -> {
                Log.d(TAG, "Step 2 - launching default home selector")
                launchDefaultHomeSelector()
            }
            3 -> {
                Log.d(TAG, "Step 3 - finishing onboarding and launching home")
                markOnboardingCompleted()
                launchHomeActivity()
            }
        }
    }

    private fun launchHomeActivity() {
        Log.d(TAG, "Launching HomeActivity")
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        }
        startActivity(homeIntent)
        finish()
    }

    private fun launchDefaultHomeSelector() {
        Log.d(TAG, "launchDefaultHomeSelector called")
        if (isSetAsDefaultHome()) {
            Log.d(TAG, "Already set as default home, going directly to step 3")
            currentStep = 3
            showStep(currentStep)
            return
        }

        isWaitingForDefaultHome = true
        setWaitingForDefaultHome()

        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            val resolveInfos = packageManager.queryIntentActivities(homeIntent, 0)
            Log.d(TAG, "Found ${resolveInfos.size} home apps")
            val defaultResolver = packageManager.resolveActivity(homeIntent, 0)
            Log.d(TAG, "Current default home: ${defaultResolver?.activityInfo?.packageName}")
            if (resolveInfos.size > 1) {
                try {
                    val chooserIntent = Intent.createChooser(homeIntent, "Choose Home App").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(chooserIntent)
                    Log.d(TAG, "Chooser started for home selection")
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Chooser failed", e)
                }
            }
            Log.d(TAG, "Trying settings approach")
            val settingsIntent = Intent("android.settings.HOME_SETTINGS")
            if (settingsIntent.resolveActivity(packageManager) != null) {
                startActivity(settingsIntent)
                Log.d(TAG, "Home settings opened")
            } else {
                try {
                    val altSettingsIntent = Intent().apply {
                        action = "android.settings.APPLICATION_SETTINGS"
                    }
                    startActivity(altSettingsIntent)
                    Log.d(TAG, "Application settings opened")
                } catch (e: Exception) {
                    Log.e(TAG, "All methods failed", e)
                    isWaitingForDefaultHome = false
                    clearWaitingForDefaultHome()
                    Log.d(TAG, "Staying on step 2 for user to try again")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in launchDefaultHomeSelector", e)
            isWaitingForDefaultHome = false
            clearWaitingForDefaultHome()
        }
    }

    private fun isSetAsDefaultHome(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo: ResolveInfo? = packageManager.resolveActivity(intent, 0)
        val isDefault = resolveInfo?.activityInfo?.packageName == packageName
        Log.d(TAG, "isSetAsDefaultHome: $isDefault (current default: ${resolveInfo?.activityInfo?.packageName})")
        return isDefault
    }
}