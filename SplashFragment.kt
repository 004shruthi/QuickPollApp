package com.example.quickpollapp.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickpollapp.R
import kotlinx.coroutines.*

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logo = view.findViewById<View>(R.id.logoView)
        val title = view.findViewById<View>(R.id.appName)

        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha = 0f

        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(1000)
            .start()

        title.alpha = 0f
        title.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(600)
            .start()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2200)
            findNavController().navigate(R.id.action_splashFragment_to_authFragment)
        }
    }
}