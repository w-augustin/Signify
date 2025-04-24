package com.example.signifybasic.features.tabs.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.features.auth.MainActivity
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import com.google.android.material.textfield.TextInputLayout

class SettingsAccountFragment : Fragment(R.layout.account_preferences) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // basic xml setup of page
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        val dbHelper = DBHelper(requireContext())

        //Retrieve username to assign text
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", null)
        val password = sharedPref.getString("userPassword", null)
        val email = sharedPref.getString("userEmail", null)

        val user_textView = view.findViewById<TextView>(R.id.tv_user)
        user_textView.text = username

        val pass_textView = view.findViewById<TextView>(R.id.tv_password)
        val maskedPassword = password?.let { "•".repeat(it.length) }
        pass_textView.text = maskedPassword

        val email_textView = view.findViewById<TextView>(R.id.tv_email)
        email_textView.text = email

        val usernameDisplayRow = view.findViewById<LinearLayout>(R.id.username_display_row)
        val usernameEditRow = view.findViewById<LinearLayout>(R.id.username_edit_row)
        val inputUsernameLayout = view.findViewById<TextInputLayout>(R.id.input_layout_username)
        val inputUsername = view.findViewById<EditText>(R.id.edit_username)
        val btnSubmitUsername = view.findViewById<Button>(R.id.btn_submit_username)

        val passwordDisplayRow = view.findViewById<LinearLayout>(R.id.password_display_row)
        val passwordEditRow = view.findViewById<LinearLayout>(R.id.password_edit_row)
        val inputPasswordLayout = view.findViewById<TextInputLayout>(R.id.input_layout_password)
        val inputPasswordEdit = view.findViewById<EditText>(R.id.edit_password)
        val btnSubmitPassword = view.findViewById<Button>(R.id.btn_submit_password)

        val emailDisplayRow = view.findViewById<LinearLayout>(R.id.email_display_row)
        val emailEditRow = view.findViewById<LinearLayout>(R.id.email_edit_row)
        val inputEmailLayout = view.findViewById<TextInputLayout>(R.id.input_layout_email)
        val inputEmail = view.findViewById<EditText>(R.id.edit_email)
        val btnSubmitEmail = view.findViewById<Button>(R.id.btn_submit_email)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // change username
        view.findViewById<MaterialButton>(R.id.btn_change_user).setOnClickListener {
            usernameDisplayRow.visibility = View.GONE
            usernameEditRow.visibility = View.VISIBLE
        }

        // dialog for changing username
        btnSubmitUsername.setOnClickListener {
            btnSubmitUsername.setOnClickListener {
                val newUsername = inputUsername.text.toString().trim()

                if (newUsername.isEmpty()) {
                    inputUsernameLayout.error = "Username cannot be empty"
                } else {
                    inputUsernameLayout.error = null
                    val oldUsername = sharedPref.getString("loggedInUser", null)

                    if (oldUsername != null) {
                        val updated = dbHelper.updateUsername(oldUsername, newUsername)

                        if (updated) {
                            sharedPref.edit().putString("loggedInUser", newUsername).apply()
                            user_textView.text = newUsername
                            Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                            inputUsername.setText("")
                            usernameEditRow.visibility = View.GONE
                            usernameDisplayRow.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }

        // change email
        view.findViewById<MaterialButton>(R.id.btn_change_email).setOnClickListener {
            emailDisplayRow.visibility = View.GONE
            emailEditRow.visibility = View.VISIBLE
        }

        // dialog for changing email
        btnSubmitEmail.setOnClickListener {
            val newEmail = inputEmail.text.toString().trim()

            if (newEmail.isEmpty()) {
                inputEmailLayout.error = "Email cannot be empty"
            } else {
                inputEmailLayout.error = null
                val oldEmail = sharedPref.getString("userEmail", null)

                if (oldEmail != null) {
                    val updated = dbHelper.updateEmail(oldEmail, newEmail)

                    if (updated) {
                        sharedPref.edit().putString("userEmail", newEmail).apply()
                        email_textView.text = newEmail
                        Toast.makeText(requireContext(), "Email updated", Toast.LENGTH_SHORT).show()
                        inputEmail.setText("")
                        emailEditRow.visibility = View.GONE
                        emailDisplayRow.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // change password
        view.findViewById<MaterialButton>(R.id.btn_change_password).setOnClickListener {
            passwordDisplayRow.visibility = View.GONE
            passwordEditRow.visibility = View.VISIBLE
        }

        // dialog for changing password
        btnSubmitPassword.setOnClickListener {
            val newPassword = inputPasswordEdit.text.toString().trim()

            if (newPassword.isEmpty()) {
                inputPasswordLayout.error = "Password cannot be empty"
            } else {
                inputPasswordLayout.error = null
                val oldPassword = sharedPref.getString("userPassword", null)

                if (oldPassword != null) {
                    val updated = dbHelper.updatePassword(oldPassword, newPassword)

                    if (updated) {
                        sharedPref.edit().putString("userPassword", newPassword).apply()
                        pass_textView.text = newPassword
                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                        inputPasswordEdit.setText("")
                        passwordEditRow.visibility = View.GONE
                        passwordDisplayRow.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // log user out of session
        view.findViewById<MaterialButton>(R.id.btn_log_out).setOnClickListener {
            if (username != null) {
                    // clear session
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    // redirect to mainactivity (login screen)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "No user found in session", Toast.LENGTH_SHORT).show()
            }
        }

        // delete account
        view.findViewById<MaterialButton>(R.id.btn_delete_account).setOnClickListener {
            if (username != null) {
                val deleted = dbHelper.deleteUser(username) // remove user from db
                if (deleted) {
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()

                    // clear session
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    // redirect to mainactivity (login screen)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No user found in session", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
