package com.ryggs.interview.orbit.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ryggs.interview.orbit.OnboardingActivity
import com.ryggs.interview.orbit.R

class OnboardingStep3Fragment : Fragment() {
    companion object {
        private const val TAG = "OnboardingStep3"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_onboarding_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        val thankYouText = view.findViewById<TextView>(R.id.thank_you_text)
        val continueButton = view.findViewById<Button>(R.id.finish_btn)

        thankYouText.text = getString(R.string.thanks_text)

        continueButton.setOnClickListener {
            Log.d(TAG, "Continue button clicked")
            (activity as? OnboardingActivity)?.goToNextStep()
        }
    }
}