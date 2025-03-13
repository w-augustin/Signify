package com.example.signifybasic.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class UserInfoCRUD(context: Context) {

    private val dbHelper: DBHelper = DBHelper(context)

    // Create User
    fun addUser(username: String, password: String, email: String, age: Int): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("email", email)
            put("age", age)
        }

        val result = db.insert("users", null, values)
        db.close()
        return result
    }

    // Read User
    fun getUser(username: String): Cursor? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        return db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
    }

    // Update User Info
    fun updateUser(username: String, newPassword: String, newEmail: String): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("password", newPassword)
            put("email", newEmail)
        }

        val result = db.update("users", values, "username = ?", arrayOf(username))
        db.close()
        return result
    }

    // Delete User
    fun deleteUser(username: String): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val result = db.delete("users", "username = ?", arrayOf(username))
        db.close()
        return result
    }
}

