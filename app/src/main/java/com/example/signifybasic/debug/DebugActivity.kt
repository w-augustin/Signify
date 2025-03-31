package com.example.signifybasic.debug

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper

class DebugActivity : AppCompatActivity() {

    private lateinit var printUsersButton: Button
    private lateinit var deleteUserButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var debugTextView: TextView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug)

        dbHelper = DBHelper(this)
        printUsersButton = findViewById(R.id.print_users_button)
        deleteUserButton = findViewById(R.id.delete_user_button)
        usernameInput = findViewById(R.id.username_input)
        debugTextView = findViewById(R.id.debugTextView)

        // Print all users when the "Print Users" button is pressed
        printUsersButton.setOnClickListener {
            val usersList = dbHelper.getAllUsers()
            if (usersList.isNotEmpty()) {
                debugTextView.text = usersList.joinToString("\n")
            } else {
                debugTextView.text = "No users found"
            }
        }

        // Delete user when the "Delete User" button is pressed
        deleteUserButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.deleteUser(username)) {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}