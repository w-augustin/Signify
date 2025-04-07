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
import com.google.android.material.textfield.TextInputLayout

class SettingsAccountFragment : Fragment(R.layout.account_preferences) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        // Make content draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        // Apply padding to avoid overlap with status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
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

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<MaterialButton>(R.id.btn_change_user).setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_field, null)
            val inputEdit = dialogView.findViewById<EditText>(R.id.input_edit)
            val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_layout)
            val submitBtn = dialogView.findViewById<Button>(R.id.btn_submit)

            inputLayout.hint = "New username"

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            submitBtn.setOnClickListener {
                val newUsername = inputEdit.text.toString().trim()

                if (newUsername.isEmpty()) {
                    inputLayout.error = "Username cannot be empty"
                } else {
                    val oldUsername = sharedPref.getString("loggedInUser", null)

                    if (oldUsername != null) {
                        val updated = dbHelper.updateUsername(oldUsername, newUsername)

                        if (updated) {
                            sharedPref.edit().putString("loggedInUser", newUsername).apply()
                            user_textView.text = newUsername
                            Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            dialog.show()
        }



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

        view.findViewById<MaterialButton>(R.id.btn_delete_account).setOnClickListener {
            if (username != null) {
                val deleted = dbHelper.deleteUser(username)
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
