package com.android.signify

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.signify.database.DBHelper
import android.Manifest
import android.widget.Toast

class TakePictureActivity : AppCompatActivity() {

    private lateinit var takePictureButton: Button
    private lateinit var saveImageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var userIdInput: EditText
    private lateinit var home: Button

    // Database Helper
    private lateinit var dbHelper: DBHelper

    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100 //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_picture)

        takePictureButton = findViewById(R.id.take_picture_button)
        imageView = findViewById(R.id.image_view)

        saveImageButton = findViewById(R.id.save_image_button)
        userIdInput = findViewById(R.id.user_id_input)
        home = findViewById(R.id.home)

        dbHelper = DBHelper(this)

        // Register for activity result to capture the image
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val photo: Bitmap? = data?.extras?.getParcelable("data")
                photo?.let {
                    imageView.setImageBitmap(it)
                }
            }
        }

        takePictureButton.setOnClickListener { _: View? ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }
        }

        // Save image button action
        saveImageButton.setOnClickListener {
            val userId = userIdInput.text.toString()
            val photo = (imageView.drawable as? BitmapDrawable)?.bitmap
            if (userId.isNotEmpty() && photo != null) {
                dbHelper.saveImage(photo, userId)
                Toast.makeText(this, "Image of userId: $userId saved", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Image of userId: $userId not saved", Toast.LENGTH_SHORT).show()
            }
        }

        home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Launch the camera intent using the ActivityResultLauncher
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }
}
