package com.example.signifybasic.deprecated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.R

class LoadImageActivity : AppCompatActivity() {

    private lateinit var loadImageButton: Button
    private lateinit var home: Button
    private lateinit var userIdInput: EditText
    private lateinit var imageView: ImageView

    // Database Helper
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_image)

        loadImageButton = findViewById(R.id.load_image_button)
        userIdInput = findViewById(R.id.user_id_input)
        imageView = findViewById(R.id.image_view)
        home = findViewById(R.id.home)

        dbHelper = DBHelper(this)

//        // Load image button action
//        loadImageButton.setOnClickListener {
//            val userId = userIdInput.text.toString()
//            if (userId.isNotEmpty()) {
//                val retrievedImage: Bitmap? = dbHelper.getImage(userId)
//                if (retrievedImage != null) {
//                    imageView.setImageBitmap(retrievedImage)
//                }
//                else {
//                    imageView.setImageBitmap(null)
//                    Toast.makeText(this, "Image of userId: $userId not found", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        home.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }
}
