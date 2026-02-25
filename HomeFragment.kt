package com.example.quickpollapp.ui.home

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickpollapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val createPollCard = view.findViewById<View>(R.id.createPollCard)
        val myPollsCard = view.findViewById<View>(R.id.myPollsCard)
        val analyticsCard = view.findViewById<View>(R.id.analyticsCard)
        val greetingText = view.findViewById<TextView>(R.id.greetingText)

        // Dynamic Greeting (Safe)
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("name")
            .get()
            .addOnSuccessListener {
                val name = it.value?.toString()
                if (!name.isNullOrEmpty()) {
                    greetingText.text = "Welcome, $name"
                } else {
                    greetingText.text = "Welcome, ${user.email?.substringBefore("@")}"
                }
            }

        // Navigation
        createPollCard.setOnClickListener {
            findNavController().navigate(R.id.createPollFragment)
        }

        myPollsCard.setOnClickListener {
            findNavController().navigate(R.id.myPollsFragment)
        }

        analyticsCard.setOnClickListener {
            findNavController().navigate(R.id.analyticsFragment)
        }

        // Card Animation
        animateCard(createPollCard)
        animateCard(myPollsCard)
        animateCard(analyticsCard)
    }

    private fun animateCard(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(120)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }
            false
        }
    }
}