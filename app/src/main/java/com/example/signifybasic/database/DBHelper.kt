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
import com.example.signifybasic.features.tabs.dictionary.*
import java.io.ByteArrayOutputStream

data class NotificationItem(val message: String, val timestamp: Long)

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        Log.d("DB_DEBUG", "DBHelper constructed using path: ${context.getDatabasePath(DATABASE_NAME).absolutePath}")
    }

    companion object {
        private const val DATABASE_NAME = "SignifyDB"
        private const val DATABASE_VERSION = 11 // Incremented to account for new tables

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
        val createUsersTable = """
        CREATE TABLE $TABLE_USERS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT UNIQUE,
            $COLUMN_PASSWORD TEXT,
            $COLUMN_EMAIL TEXT UNIQUE,
    
            module_index INTEGER DEFAULT 0,
            step_index INTEGER DEFAULT 0
        )
        """.trimIndent()
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

        // Create LoginHistory table
        db.execSQL("""
                CREATE TABLE IF NOT EXISTS LoginHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    loginDate TEXT,
                    UNIQUE(userID, loginDate)
                )
            """.trimIndent())

        db.execSQL("""
                CREATE TABLE IF NOT EXISTS UserBadges (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    slot INTEGER, -- 0, 1, 2
                    achievementName TEXT,
                    FOREIGN KEY (userID) REFERENCES users(id)
                )
            """.trimIndent())
        
        val settingsTable = "CREATE TABLE user_settings (" +
                "user_id INTEGER PRIMARY KEY, " +
                "sound_effects INTEGER NOT NULL, " +
                "vibration_feedback INTEGER NOT NULL, " +
                "push_notifs INTEGER NOT NULL, " +
                "email_updates INTEGER NOT NULL, " +
                "notif_sound INTEGER NOT NULL, " +
                "daily_reminder_time TIME NOT NULL, " +
                "text_size INTEGER NOT NULL, " +
                "contrast INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);"

        db.execSQL(settingsTable)

        db.execSQL(
        """
            CREATE TABLE IF NOT EXISTS LoginHistory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                loginDate TEXT,
                UNIQUE(userID, loginDate)
            )
        """.trimIndent())

        db.execSQL(
        """CREATE TABLE KnownWords (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            userID INTEGER NOT NULL,
            word TEXT NOT NULL,
            UNIQUE(userID, word)
        )
        """.trimIndent())


        // Create Additional Tables
        db.execSQL("CREATE TABLE $TABLE_ACCOUNT (userID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, token INTEGER)")
        db.execSQL("CREATE TABLE $TABLE_MODULE (moduleID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT NOT NULL, media TEXT)")
        db.execSQL("CREATE TABLE $TABLE_VOCAB_LIST (word TEXT PRIMARY KEY, moduleID INTEGER, FOREIGN KEY (moduleID) REFERENCES $TABLE_MODULE(moduleID))")
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
        if (oldVersion < 10) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS LoginHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER,
                    loginDate TEXT,
                    UNIQUE(userID, loginDate)
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

    fun addKnownWord(userId: Int, word: String) {
        val db = writableDatabase
        val stmt = db.compileStatement("""
        INSERT OR IGNORE INTO KnownWords (userID, word)
        VALUES (?, ?)
    """.trimIndent())
        stmt.bindLong(1, userId.toLong())
        stmt.bindString(2, word.uppercase())
        stmt.execute()
        stmt.close()
        db.close()
    }

    fun getKnownWordCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM KnownWords WHERE userID = ?", arrayOf(userId.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        db.close()
        return count
    }


    fun getUserProgress(username: String): Pair<Int, Int> {
        val db = this.readableDatabase
        val query = "SELECT module_index, step_index FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        var result = 0 to 0
        if (cursor.moveToFirst()) {
            val mod = cursor.getInt(cursor.getColumnIndexOrThrow("module_index"))
            val step = cursor.getInt(cursor.getColumnIndexOrThrow("step_index"))
            result = mod to step
        }
        cursor.close()
        db.close()
        return result
    }

    fun updateUserProgress(username: String, moduleIndex: Int, stepIndex: Int) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT module_index, step_index FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
            arrayOf(username)
        )
        if (cursor.moveToFirst()) {
            // get user's furthest progress
            val currentModule = cursor.getInt(cursor.getColumnIndexOrThrow("module_index"))
            val currentStep = cursor.getInt(cursor.getColumnIndexOrThrow("step_index"))

            // only update if this is a 'higher' module than user's 'highest'
            val shouldUpdate = when {
                moduleIndex > currentModule -> true // trying to move to a higher module
                moduleIndex == currentModule && stepIndex > currentStep -> true // same module, but higher activity
                else -> false
            }

            if (shouldUpdate) {
                val values = ContentValues().apply {
                    put("module_index", moduleIndex)
                    put("step_index", stepIndex)
                }
                db.update(
                    TABLE_USERS,
                    values,
                    "$COLUMN_USERNAME = ?",
                    arrayOf(username)
                )
                Log.d("PROGRESS", "Progress updated for $username → module=$moduleIndex, step=$stepIndex")
            } else {
                Log.d("PROGRESS", "Skipped update — attempted to regress progress for $username")
            }
        }

        cursor.close()
        db.close()
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

//    fun getUserTotalExp(userID: Int): Int {
//        val db = this.readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT SUM(score) FROM $TABLE_USER_PROGRESS WHERE userID = ?",
//            arrayOf(userID.toString())
//        )
//        val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
//        cursor.close()
//        return total
//    }

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
    fun addUser(username: String, password: String, email: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_EMAIL, email)
            put("module_index", 0)
            put("step_index", 0)
        }

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
                val currentMod = cursor.getString(cursor.getColumnIndexOrThrow("currentModuleTitle"))
                users.add("ID: $id | Username: $username | Email: $email | Progress: $progress " +
                "| CurrentModule : $currentMod")
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

    /* get all discussion posts
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
    }*/

    //        val settingsTable = "CREATE TABLE user_settings (" +
    //                "user_id INTEGER PRIMARY KEY, " +
    //                "sound_effects INTEGER NOT NULL, " +
    //                "vibration_feedback INTEGER NOT NULL, " +
    //                "push_notifs INTEGER NOT NULL, " +
    //                "email_updates INTEGER NOT NULL, " +
    //                "notif_sound INTEGER NOT NULL, " +
    //                "daily_reminder_time TIME NOT NULL, " +
    //                "text_size INTEGER NOT NULL, " +
    //                "contrast INTEGER NOT NULL, " +
    //                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);"

    fun insertUserSettings(userID: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("user_id", userID)
            put("sound_effects", 1)
            put("vibration_feedback", 1)
            put("push_notifs", 1)
            put("email_updates", 1)
            put("notif_sound", 1)
            put("daily_reminder_time", "08:00:00")
            put("text_size", 1)
            put("contrast", 0)
        }

        db.insert("user_settings", null, values)
        db.close()
    }

    fun getSoundEffects(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT sound_effects FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val result = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        db.close()
        return result
    }

    fun setSoundEffects(userId: Int, enabled: Boolean) {
        val db = writableDatabase
        val value = if (enabled) 1 else 0
        db.execSQL("UPDATE user_settings SET sound_effects = ? WHERE user_id = ?", arrayOf(value, userId))
        db.close()
    }

    fun getTextSize(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT text_size FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val size = if (cursor.moveToFirst()) cursor.getInt(0) else 1 // default fallback
        cursor.close()
        db.close()
        return size
    }

    fun setTextSize(userId: Int, size: Int) {
        val db = writableDatabase
        db.execSQL("UPDATE user_settings SET text_size = ? WHERE user_id = ?", arrayOf(size, userId))
        db.close()
    }

    fun getVibrationFeedback(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT vibration_feedback FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val result = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        db.close()
        return result
    }

    fun setVibrationFeedback(userId: Int, enabled: Boolean) {
        val db = writableDatabase
        val value = if (enabled) 1 else 0
        db.execSQL("UPDATE user_settings SET vibration_feedback = ? WHERE user_id = ?", arrayOf(value, userId))
        db.close()
    }

    fun getPushNotifs(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT push_notifs FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val result = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        db.close()
        return result
    }

    fun setPushNotifs(userId: Int, enabled: Boolean) {
        val db = writableDatabase
        val value = if (enabled) 1 else 0
        db.execSQL("UPDATE user_settings SET push_notifs = ? WHERE user_id = ?", arrayOf(value, userId))
        db.close()
    }

    fun getEmailUpdates(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT email_updates FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val result = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        db.close()
        return result
    }

    fun setEmailUpdates(userId: Int, enabled: Boolean) {
        val db = writableDatabase
        val value = if (enabled) 1 else 0
        db.execSQL("UPDATE user_settings SET email_updates = ? WHERE user_id = ?", arrayOf(value, userId))
        db.close()
    }

    fun getContrast(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT contrast FROM user_settings WHERE user_id = ?", arrayOf(userId.toString()))
        val result = if (cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        db.close()
        return result
    }

    fun setContrast(userId: Int, enabled: Boolean) {
        val db = writableDatabase
        val value = if (enabled) 1 else 0
        db.execSQL("UPDATE user_settings SET contrast = ? WHERE user_id = ?", arrayOf(value, userId))
        db.close()
    }

    fun getNotifSound(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT notif_sound FROM user_settings WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        val result = if (cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        db.close()
        return result
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

    fun recordLoginDate(userID: Int) {
        val db = this.writableDatabase
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val values = ContentValues().apply {
            put("userID", userID)
            put("loginDate", today)
        }

        db.insertWithOnConflict("LoginHistory", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
    }

    fun getLoginStreak(userID: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("""
        SELECT loginDate FROM LoginHistory
        WHERE userID = ?
        ORDER BY loginDate DESC
    """.trimIndent(), arrayOf(userID.toString()))

        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        var streak = 0
        var lastExpectedDate = java.util.Calendar.getInstance()

        if (cursor.moveToFirst()) {
            do {
                val loginDateStr = cursor.getString(cursor.getColumnIndexOrThrow("loginDate"))
                val loginDate = formatter.parse(loginDateStr)
                val expectedDate = lastExpectedDate.time

                if (loginDate?.let { formatter.format(it) } == formatter.format(expectedDate)) {
                    streak++
                    lastExpectedDate.add(java.util.Calendar.DATE, -1)
                } else {
                    break
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return streak
    }
}
