package com.example.signifybasic.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class SignLanguageCRUD(context: Context) {

    private val dbHelper: DBHelper = DBHelper(context)

    // Create or Add Sign
    fun addSign(signName: String, signData: String): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("sign_name", signName)  // Store the name of the sign
            put("sign_data", signData)  // Store the sign data (could be video, image, etc.)
        }

        val result = db.insert("signs", null, values)
        db.close()
        return result
    }

    // Read Sign (Get sign by name)
    fun getSign(signName: String): Cursor? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        return db.rawQuery("SELECT * FROM signs WHERE sign_name = ?", arrayOf(signName))
    }

    // Update Sign (If you want to modify a sign, like correcting its name or data)
    fun updateSign(signName: String, newSignData: String): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("sign_data", newSignData)  // Update the sign's data
        }

        val result = db.update("signs", values, "sign_name = ?", arrayOf(signName))
        db.close()
        return result
    }

    // Delete Sign (If a sign is no longer needed)
    fun deleteSign(signName: String): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val result = db.delete("signs", "sign_name = ?", arrayOf(signName))
        db.close()
        return result
    }

    // Validate Sign (Check if a given sign matches the stored one)
    fun validateSign(signName: String, signData: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT sign_data FROM signs WHERE sign_name = ?", arrayOf(signName))

        val isValid = if (cursor.moveToFirst()) {
            val storedSignData = cursor.getString(cursor.getColumnIndexOrThrow("sign_data"))
            storedSignData == signData
        } else {
            false // Sign not found
        }

        cursor.close()
        db.close()
        return isValid
    }
}
