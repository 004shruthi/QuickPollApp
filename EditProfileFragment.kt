package com.example.quickpollapp.ui.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickpollapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.AutoCompleteTextView
import java.util.Calendar

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var profileImage: CircleImageView
    private var imageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imageUri = it
                profileImage.setImageURI(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstNameField = view.findViewById<TextInputEditText>(R.id.firstNameField)
        val lastNameField = view.findViewById<TextInputEditText>(R.id.lastNameField)
        val usernameField = view.findViewById<TextInputEditText>(R.id.usernameField)
        val emailField = view.findViewById<TextInputEditText>(R.id.emailField)
        val mobileField = view.findViewById<TextInputEditText>(R.id.mobileField)
        val dobField = view.findViewById<TextInputEditText>(R.id.dobField)
        val ageField = view.findViewById<TextInputEditText>(R.id.ageField)
        val genderField = view.findViewById<AutoCompleteTextView>(R.id.genderField)
        val saveButton = view.findViewById<View>(R.id.saveButton)
        profileImage = view.findViewById(R.id.profileImage)

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference.child("users").child(uid)

        emailField.setText(user.email)
        emailField.isEnabled = false

        // Load Existing Data
        db.get().addOnSuccessListener {
            firstNameField.setText(it.child("firstName").value?.toString() ?: "")
            lastNameField.setText(it.child("lastName").value?.toString() ?: "")
            usernameField.setText(it.child("username").value?.toString() ?: "")
            mobileField.setText(it.child("mobile").value?.toString() ?: "")
            dobField.setText(it.child("dob").value?.toString() ?: "")
            ageField.setText(it.child("age").value?.toString() ?: "")
            genderField.setText(it.child("gender").value?.toString() ?: "")
        }

        // Gender Dropdown
        val genders = listOf("Female", "Male", "Prefer not to say")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genders)
        genderField.setAdapter(adapter)

        // DOB Picker
        dobField.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val formatted = "$day-${month + 1}-$year"
                    dobField.setText(formatted)

                    val calculatedAge = calculateAge(year, month, day)
                    ageField.setText(calculatedAge.toString())
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Image Picker
        profileImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        saveButton.setOnClickListener {

            val profileMap = mapOf(
                "firstName" to firstNameField.text.toString(),
                "lastName" to lastNameField.text.toString(),
                "username" to usernameField.text.toString(),
                "mobile" to mobileField.text.toString(),
                "dob" to dobField.text.toString(),
                "age" to ageField.text.toString(),
                "gender" to genderField.text.toString(),
                "email" to user.email
            )

            db.updateChildren(profileMap)
                .addOnSuccessListener {

                    // Upload image if selected
                    imageUri?.let {
                        val storageRef = FirebaseStorage.getInstance().reference
                            .child("profileImages/$uid.jpg")

                        storageRef.putFile(it)
                            .continueWithTask { task ->
                                storageRef.downloadUrl
                            }
                            .addOnSuccessListener { uri ->
                                db.child("profileImageUrl").setValue(uri.toString())
                            }
                    }

                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()
        dob.set(year, month, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}