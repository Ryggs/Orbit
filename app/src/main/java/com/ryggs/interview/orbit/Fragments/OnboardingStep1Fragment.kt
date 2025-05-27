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

class OnboardingStep1Fragment : Fragment() {

    companion object {
        private const val TAG = "OnboardingStep1"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_onboarding_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)
        val getstartedButton = view.findViewById<Button>(R.id.get_started_btn)

        welcomeText.text = getString(R.string.welcome_text)

        getstartedButton.setOnClickListener {
            Log.d(TAG, "Continue button clicked")
            (activity as? OnboardingActivity)?.goToNextStep()
        }
    }

}