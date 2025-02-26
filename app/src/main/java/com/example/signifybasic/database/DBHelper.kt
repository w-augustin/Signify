package com.example.signifybasic.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SignifyDB"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "userImages"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE = "userImage"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID TEXT, " +
                "$COLUMN_IMAGE BLOB)"
        db.execSQL(createTableQuery)
    }

    // TODO: edit to include newly added column
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add the new column if it doesn't exist
            val addColumnQuery = "ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_USER_ID TEXT"
            db.execSQL(addColumnQuery)
        }
    }

    // Save Image (Bitmap) into SQLite
    fun saveImage(bitmap: Bitmap, id: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        // convert bitMap to byteArray
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Check if the image already exists for the given user_id
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USER_ID = ?", arrayOf(id))

        val result : Boolean

        if (cursor.moveToFirst()) {
            // If the image exists, update it
            values.put(COLUMN_IMAGE, byteArray)
            // Update the image where the user_id matches
            val updateResult = db.update(TABLE_NAME, values, "$COLUMN_USER_ID = ?", arrayOf(id))

            result = updateResult > 0
        } else {
            // store byteArray to db
            val query = "INSERT INTO $TABLE_NAME ($COLUMN_USER_ID, $COLUMN_IMAGE) VALUES (?, ?)"
            val statement = db.compileStatement(query)
            statement.bindString(1, id)
            statement.bindBlob(2, byteArray)

            val insertResult = statement.executeInsert()
            result = insertResult != -1L
        }

        cursor.close()
        db.close()

        return result
    }

    // Retrieve saved image
    fun getImage(id: String): Bitmap? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT $COLUMN_IMAGE FROM $TABLE_NAME WHERE $COLUMN_USER_ID = ?", arrayOf(id))

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
}
