package com.example.signifybasic.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SignifyDB"
        private const val DATABASE_VERSION = 3  // Incremented to account for new tables

        // User Images Table
        private const val TABLE_USER_IMAGES = "userImages"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE = "userImage"

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_PROGRESS = "progress"

        // Additional Tables
        private const val TABLE_ACCOUNT = "Account"
        private const val TABLE_MODULE = "Module"
        private const val TABLE_VOCAB_LIST = "VocabList"
        private const val TABLE_USER_PROGRESS = "UserProgress"
        private const val TABLE_ASSESSMENT = "Assessment"
        private const val TABLE_QUESTION = "Question"
        private const val TABLE_USER_ANSWER = "UserAnswer"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create User Images Table
        val createUserImagesTable = "CREATE TABLE $TABLE_USER_IMAGES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID TEXT, " +
                "$COLUMN_IMAGE BLOB)"
        db.execSQL(createUserImagesTable)

        // Create Users Table
        val createUsersTable = "CREATE TABLE $TABLE_USERS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT UNIQUE, " +
                "$COLUMN_PASSWORD TEXT, " +
                "$COLUMN_EMAIL TEXT UNIQUE, " +
                "$COLUMN_AGE INTEGER, " +
                "$COLUMN_PROGRESS INTEGER DEFAULT 0)"
        db.execSQL(createUsersTable)

        // Create Additional Tables
        db.execSQL("CREATE TABLE $TABLE_ACCOUNT (userID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, token INTEGER)")
        db.execSQL("CREATE TABLE $TABLE_MODULE (moduleID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT NOT NULL, media TEXT)")
        db.execSQL("CREATE TABLE $TABLE_VOCAB_LIST (word TEXT PRIMARY KEY, moduleID INTEGER, FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_USER_PROGRESS (progressID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, moduleID INTEGER, completed BOOLEAN DEFAULT 0, score INTEGER, FOREIGN KEY (userID) REFERENCES $TABLE_ACCOUNT(userID), FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_ASSESSMENT (assessmentID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, moduleID INTEGER, FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_QUESTION (questionID INTEGER PRIMARY KEY AUTOINCREMENT, assessmentID INTEGER, question TEXT NOT NULL, correctAnswer TEXT NOT NULL, FOREIGN KEY (assessmentID) REFERENCES $TABLE_ASSESSMENT(assessmentID))")
        db.execSQL("CREATE TABLE $TABLE_USER_ANSWER (userAnswerID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, questionID INTEGER, userAnswer TEXT NOT NULL, isCorrect BOOLEAN DEFAULT 0, FOREIGN KEY (userID) REFERENCES $TABLE_ACCOUNT(userID), FOREIGN KEY (questionID) REFERENCES $TABLE_QUESTION(questionID))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val addColumnQuery = "ALTER TABLE $TABLE_USER_IMAGES ADD COLUMN $COLUMN_USER_ID TEXT"
            db.execSQL(addColumnQuery)
        }
        if (oldVersion < 3) {
            onCreate(db) // Recreate tables if upgrading
        }
    }

    // Save Image (Bitmap) into SQLite
    fun saveImage(bitmap: Bitmap, id: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USER_IMAGES WHERE $COLUMN_USER_ID = ?", arrayOf(id))
        val result: Boolean

        if (cursor.moveToFirst()) {
            values.put(COLUMN_IMAGE, byteArray)
            val updateResult = db.update(TABLE_USER_IMAGES, values, "$COLUMN_USER_ID = ?", arrayOf(id))
            result = updateResult > 0
        } else {
            val query = "INSERT INTO $TABLE_USER_IMAGES ($COLUMN_USER_ID, $COLUMN_IMAGE) VALUES (?, ?)"
            val statement = db.compileStatement(query)
            statement.bindString(1, id)
            statement.bindBlob(2, byteArray)
            result = statement.executeInsert() != -1L
        }

        cursor.close()
        db.close()
        return result
    }

    // Retrieve saved image
    fun getImage(id: String): Bitmap? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT $COLUMN_IMAGE FROM $TABLE_USER_IMAGES WHERE $COLUMN_USER_ID = ?", arrayOf(id))
        return if (cursor.moveToFirst()) {
            val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
            cursor.close()
            db.close()
            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    // Add a new user
    fun addUser(username: String, password: String, email: String, age: Int, progress: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_AGE, age)
        values.put(COLUMN_PROGRESS, progress)

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // Verify login credentials
    fun verifyUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?", arrayOf(username, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // Retrieve all users
    fun getAllUsers(): List<String> {
        val users = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE))
                val progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS))
                users.add("ID: $id | Username: $username | Email: $email | Age: $age | Progress: $progress")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }

    // Add user progress
    fun addUserProgress(userID: Int, moduleID: Int, completed: Boolean, score: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("userID", userID)
        values.put("moduleID", moduleID)
        values.put("completed", if (completed) 1 else 0)
        values.put("score", score)
        db.insert(TABLE_USER_PROGRESS, null, values)
        db.close()
    }
}
