package com.example.signifybasic.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.signifybasic.features.tabs.achievements.AchievementMeta
import com.example.signifybasic.features.tabs.discussion.DiscussionPost
import java.io.ByteArrayOutputStream

data class NotificationItem(val message: String, val timestamp: Long)

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        Log.d("DB_DEBUG", "DBHelper constructed using path: ${context.getDatabasePath(DATABASE_NAME).absolutePath}")
    }

    companion object {
        private const val DATABASE_NAME = "SignifyDB"
        private const val DATABASE_VERSION = 7  // Incremented to account for new tables

        // User Images Table
        private const val TABLE_USER_IMAGES = "userImages"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE = "userImage"

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
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
        Log.d("DB_DEBUG", "onCreate() was called!")
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

                "$COLUMN_PROGRESS INTEGER DEFAULT 0," +
                "knownWords INTEGER DEFAULT 0," +
                "currentModuleTitle TEXT DEFAULT 'Module 1')"
        db.execSQL(createUsersTable)

        val createDiscussionTable = """
            CREATE TABLE discussions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            userID INTEGER,
            content TEXT NOT NULL,
            timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY(userID) REFERENCES $TABLE_USERS(id)
        )
        """.trimIndent()
        db.execSQL(createDiscussionTable)


        val achievementTable = """
            CREATE TABLE IF NOT EXISTS Achievements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                achievementName TEXT NOT NULL,
                dateEarned TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (userID) REFERENCES users(id)
            )
        """.trimIndent()
        db.execSQL(achievementTable)

        // Create Additional Tables
        db.execSQL("CREATE TABLE $TABLE_ACCOUNT (userID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, token INTEGER)")
        db.execSQL("CREATE TABLE $TABLE_MODULE (moduleID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT NOT NULL, media TEXT)")
        db.execSQL("CREATE TABLE $TABLE_VOCAB_LIST (word TEXT PRIMARY KEY, moduleID INTEGER, FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_USER_PROGRESS (progressID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, moduleID INTEGER, completed BOOLEAN DEFAULT 0, score INTEGER, FOREIGN KEY (userID) REFERENCES $TABLE_ACCOUNT(userID), FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_ASSESSMENT (assessmentID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, moduleID INTEGER, FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
        db.execSQL("CREATE TABLE $TABLE_QUESTION (questionID INTEGER PRIMARY KEY AUTOINCREMENT, assessmentID INTEGER, question TEXT NOT NULL, correctAnswer TEXT NOT NULL, FOREIGN KEY (assessmentID) REFERENCES $TABLE_ASSESSMENT(assessmentID))")
        db.execSQL("CREATE TABLE $TABLE_USER_ANSWER (userAnswerID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, questionID INTEGER, userAnswer TEXT NOT NULL, isCorrect BOOLEAN DEFAULT 0, FOREIGN KEY (userID) REFERENCES $TABLE_ACCOUNT(userID), FOREIGN KEY (questionID) REFERENCES $TABLE_QUESTION(questionID))")
        db.execSQL("""CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT NOT NULL, timestamp INTEGER )""".trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val addColumnQuery = "ALTER TABLE $TABLE_USER_IMAGES ADD COLUMN $COLUMN_USER_ID TEXT"
            db.execSQL(addColumnQuery)
        }
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS discussions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    content TEXT NOT NULL,
                    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(userID) REFERENCES $TABLE_USERS(id)
                )
            """.trimIndent())
        }
        if (oldVersion < 5) {
            db.execSQL("""
        CREATE TABLE IF NOT EXISTS notifications (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            message TEXT NOT NULL,
            timestamp INTEGER
        )
    """.trimIndent())
        }
        if (oldVersion < 6) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS Achievements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    achievementName TEXT NOT NULL,
                    dateEarned TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (userID) REFERENCES users(id)
                )
            """.trimIndent())
        }
        if (oldVersion < 7) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS UserBadges (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    slot INTEGER, -- 0, 1, 2
                    achievementName TEXT,
                    FOREIGN KEY (userID) REFERENCES users(id)
                )
            """.trimIndent())
        }

    }

    fun addAchievement(userID: Int, achievementName: String, context: Context): Boolean {
        val inputName = achievementName.trim().lowercase()
        val validNames = loadValidAchievementNames(context).map { it.trim().lowercase() }

        Log.d("ACHIEVEMENT", "Comparing: \"$inputName\" to $validNames")

        if (!validNames.contains(inputName)) {
            Log.d("ACHIEVEMENT", "INVALID: \"$achievementName\"")
            return false
        }

        val db = this.writableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM Achievements WHERE userID = ? AND LOWER(TRIM(achievementName)) = ?",
            arrayOf(userID.toString(), inputName)
        )

        val exists = cursor.count > 0
        cursor.close()

        if (exists) {
            Log.d("ACHIEVEMENT", "Already exists: \"$achievementName\"")
            return false
        }

        val values = ContentValues()
        values.put("userID", userID)
        values.put("achievementName", achievementName.trim()) // original formatting for UI

        Log.d("ACHIEVEMENT", "Inserting achievement: \"$achievementName\"")
        val result = db.insert("Achievements", null, values)
        Log.d("ACHIEVEMENT", "Insert result: $result")
        db.close()

        return result != -1L
    }


    fun getAchievements(userID: Int): List<String> {
        val achievements = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT achievementName FROM Achievements WHERE userID = ?", arrayOf(userID.toString()))

        if (cursor.moveToFirst()) {
            do {
                achievements.add(cursor.getString(cursor.getColumnIndexOrThrow("achievementName")))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return achievements
    }

    fun clearAchievementsForUser(userID: Int): Boolean {
        val db = this.writableDatabase
        val rowsDeleted = db.delete("Achievements", "userID = ?", arrayOf(userID.toString()))
        db.close()
        return rowsDeleted > 0
    }

    private fun loadValidAchievementNames(context: Context): List<String> {
        val inputStream = context.assets.open("valid_achievements.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = org.json.JSONArray(jsonString)
        val list = mutableListOf<String>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            list.add(name)
        }

        return list
    }


    fun getUserIdByUsername(username: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", arrayOf(username))
        val userId = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        db.close()
        return userId
    }

    fun getUserProgress(username: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_PROGRESS FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"

        val cursor = db.rawQuery(query, arrayOf(username))
        var progress = -1

        if (cursor.moveToFirst()) {
            progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS))
        }

        cursor.close()
        db.close()
        return progress
    }



    fun getUserTotalExp(userID: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(score) FROM $TABLE_USER_PROGRESS WHERE userID = ?",
            arrayOf(userID.toString())
        )
        val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    fun getKnownWordCount(userID: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT knownWords FROM $TABLE_USERS WHERE id = ?",
            arrayOf(userID.toString())
        )

        val knownWords = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("knownWords"))
        } else {
            0
        }

        cursor.close()
        db.close()
        return knownWords
    }

    fun setKnownWords(userID: Int, value: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("knownWords", value)
        db.update(TABLE_USERS, values, "id = ?", arrayOf(userID.toString()))
        db.close()
    }

    fun getCurrentModuleTitle(userID: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT currentModuleTitle FROM $TABLE_USERS WHERE id = ?",
            arrayOf(userID.toString())
        )

        val title = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("currentModuleTitle"))
        } else {
            "Module 1" // fallback
        }

        cursor.close()
        db.close()
        return title
    }

    fun setCurrentModuleTitle(userID: Int, title: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("currentModuleTitle", title)
        db.update(TABLE_USERS, values, "id = ?", arrayOf(userID.toString()))
        db.close()
    }


    fun getUserProgressSummary(userID: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("""
        SELECT COUNT(*) AS totalModules,
               SUM(completed) AS modulesCompleted,
               SUM(score) AS totalScore
        FROM $TABLE_USER_PROGRESS
        WHERE userID = ?
    """.trimIndent(), arrayOf(userID.toString()))

        var summary = "Progress not available"
        if (cursor.moveToFirst()) {
            val totalModules = cursor.getInt(cursor.getColumnIndexOrThrow("totalModules"))
            val completed = cursor.getInt(cursor.getColumnIndexOrThrow("modulesCompleted"))
            val totalScore = cursor.getInt(cursor.getColumnIndexOrThrow("totalScore"))
            summary = "Modules: $completed/$totalModules | Total Score: $totalScore"
        }

        cursor.close()
        db.close()
        return summary
    }


    fun insertNotification(message: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }
        val result = db.insert("notifications", null, values)
        db.close()
        return result != -1L
    }

    fun getAllNotifications(): List<NotificationItem> {
        val list = mutableListOf<NotificationItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM notifications ORDER BY timestamp DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val message = cursor.getString(cursor.getColumnIndexOrThrow("message"))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                list.add(NotificationItem(message, timestamp))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
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
    fun addUser(username: String, password: String, email: String, progress: Int = 0): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PROGRESS, progress)

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // validate username and password
    fun isValidUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // Check if username exists in db
    fun usernameExists(username: String):Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun userExists(username: String, email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun deleteUser(username: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(TABLE_USERS, "$COLUMN_USERNAME = ?", arrayOf(username))
        db.close()
        return rowsDeleted > 0 // Returns true if a row was deleted
    }

    fun updateUsername(oldUsername: String, newUsername: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("username", newUsername)
        val rows = db.update("users", values, "username = ?", arrayOf(oldUsername))
        db.close()
        return rows > 0
    }

    fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("password", newPassword)
        val rows = db.update("users", values, "password = ?", arrayOf(oldPassword))
        db.close()
        return rows > 0
    }

    fun updateEmail(oldEmail: String, newEmail: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("email", newEmail)
        val rows = db.update("users", values, "email = ?", arrayOf(oldEmail))
        db.close()
        return rows > 0
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

                val progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS))
                val knownwords = cursor.getInt(cursor.getColumnIndexOrThrow("knownWords"))
                val currentMod = cursor.getString(cursor.getColumnIndexOrThrow("currentModuleTitle"))
                users.add("ID: $id | Username: $username | Email: $email | Progress: $progress " +
                "Known Words: $knownwords | CurrentModule : $currentMod")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }

    fun getEmailByUsername(username: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT email FROM users WHERE username = ?", arrayOf(username))
        var email: String? = null
        if (cursor.moveToFirst()) {
            email = cursor.getString(0)
        }
        cursor.close()
        return email
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

    // add a new discussion post
    fun addDiscussionPost(userID: Int, content: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("userID", userID)
        values.put("content", content)
        val result = db.insert("discussions", null, values)
        db.close()
        return result != -1L
    }

    // get all discussion posts
    fun getAllDiscussionPosts(): List<DiscussionPost> {
        val posts = mutableListOf<DiscussionPost>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM discussions ORDER BY timestamp DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"))
                posts.add(DiscussionPost(content, timestamp, userId))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return posts
    }


    fun changeUserProgress(username: String,  score: Int) {
        val db = this.writableDatabase

        // First, find the user's ID
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
            arrayOf(username)
        )

        if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

            // Update the progress column for this user
            val values = ContentValues()
            values.put(COLUMN_PROGRESS, score)

            db.update(
                TABLE_USERS,
                values,
                "id = ?",
                arrayOf(userId.toString())
            )
        }

        cursor.close()
        db.close()
    }


    fun addKnownWord(userID: Int, word: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("userID", userID)
        values.put("word", word)
        val result = db.insert("KnownWords", null, values)
        db.close()
        return result != -1L
    }

    fun setUserBadge(userID: Int, slot: Int, achievementName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("userID", userID)
            put("slot", slot)
            put("achievementName", achievementName)
        }
        db.delete("UserBadges", "userID=? AND slot=?", arrayOf(userID.toString(), slot.toString()))
        db.insert("UserBadges", null, values)
        db.close()
    }

    fun getUserBadges(userID: Int): Map<Int, String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT slot, achievementName FROM UserBadges WHERE userID=?", arrayOf(userID.toString()))
        val map = mutableMapOf<Int, String>()
        while (cursor.moveToNext()) {
            val slot = cursor.getInt(cursor.getColumnIndexOrThrow("slot"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("achievementName"))
            map[slot] = name
        }
        cursor.close()
        db.close()
        return map
    }

    fun clearUserBadge(userID: Int, slot: Int) {
        val db = writableDatabase
        db.delete("UserBadges", "userID=? AND slot=?", arrayOf(userID.toString(), slot.toString()))
        db.close()
    }

//    fun getKnownWords(userID: Int): List<String> {
//        val db = this.readableDatabase
//        val words = mutableListOf<String>()
//        val cursor = db.rawQuery("SELECT word FROM KnownWords WHERE userID = ?", arrayOf(userID.toString()))
//
//        if (cursor.moveToFirst()) {
//            do {
//                words.add(cursor.getString(cursor.getColumnIndexOrThrow("word")))
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        db.close()
//        return words
//    }
}
