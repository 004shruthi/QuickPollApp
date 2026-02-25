package com.example.quickpollapp.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickpollapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameText = view.findViewById<TextView>(R.id.nameText)
        val emailText = view.findViewById<TextView>(R.id.emailText)
        val editButton = view.findViewById<View>(R.id.editButton)
        val profileImage = view.findViewById<CircleImageView>(R.id.profileImage)

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference.child("users").child(uid)

        emailText.text = user.email

        db.get().addOnSuccessListener {
            val firstName = it.child("firstName").value?.toString() ?: ""
            val lastName = it.child("lastName").value?.toString() ?: ""
            val imageUrl = it.child("profileImageUrl").value?.toString()

            if (firstName.isNotEmpty()) {
                nameText.text = "$firstName $lastName"
            } else {
                nameText.text = user.email?.substringBefore("@")
            }

            if (!imageUrl.isNullOrEmpty()) {
                // If you want full loading, use Glide/Picasso
                // For now keeping default
            }
        }

        editButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        setupRow(view, R.id.favouritesRow, R.drawable.favorite_24dp, "Favourites") {
            Toast.makeText(requireContext(), "Favourites clicked", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.downloadsRow, R.drawable.download_24dp, "Downloads") {
            Toast.makeText(requireContext(), "Downloads clicked", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.languageRow, R.drawable.language_24dp, "Language") {
            Toast.makeText(requireContext(), "Language settings", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.locationRow, R.drawable.location_on_24dp, "Location") {
            Toast.makeText(requireContext(), "Location settings", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.subscriptionRow, R.drawable.subscriptions_24dp, "Subscription") {
            Toast.makeText(requireContext(), "Subscription clicked", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.displayRow, R.drawable.desktop_windows_24dp, "Display") {
            Toast.makeText(requireContext(), "Display settings", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.clearCacheRow, R.drawable.delete_24dp, "Clear Cache") {
            requireContext().cacheDir.deleteRecursively()
            Toast.makeText(requireContext(), "Cache Cleared", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.clearHistoryRow, R.drawable.schedule_24dp, "Clear History") {
            db.child("history").removeValue()
            Toast.makeText(requireContext(), "History Cleared", Toast.LENGTH_SHORT).show()
        }

        setupRow(view, R.id.logoutRow, R.drawable.logout_24dp, "Log Out") {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.authFragment)
        }
    }

    private fun setupRow(
        parent: View,
        rowId: Int,
        iconRes: Int,
        titleText: String,
        onClick: () -> Unit
    ) {
        val row = parent.findViewById<LinearLayout>(rowId)
        val icon = row.findViewById<ImageView>(R.id.icon)
        val title = row.findViewById<TextView>(R.id.title)

        icon.setImageResource(iconRes)
        title.text = titleText
        row.setOnClickListener { onClick() }
    }
}