package com.example.signifybasic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity3 : AppCompatActivity() {

    private lateinit var takePictureButton: Button
    private lateinit var loadImageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        takePictureButton = findViewById(R.id.take_picture_button)
        loadImageButton = findViewById(R.id.load_image_button)

        // Handle taking picture
        takePictureButton.setOnClickListener {
            val intent = Intent(this, TakePictureActivity::class.java)
            startActivity(intent)
        }

        // Handle loading image by user ID
        loadImageButton.setOnClickListener {
            val intent = Intent(this, LoadImageActivity::class.java)
            startActivity(intent)
        }
    }

}
