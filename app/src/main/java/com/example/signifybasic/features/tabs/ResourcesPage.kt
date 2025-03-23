package com.example.signifybasic.features.tabs

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.signifybasic.R

class ResourcesPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)

        val seeMoreButton = findViewById<Button>(R.id.see_more_button)
        val link1Button = findViewById<Button>(R.id.link1_button)
        val link2Button = findViewById<Button>(R.id.link2_button)
        val link3Button = findViewById<Button>(R.id.link3_button)

//        seeMoreButton.setOnClickListener { view: View? ->
//            // Redirect to a full video page (modify with actual link)
//            val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://www.example.com/videos")
//            )
//            startActivity(intent)
//        }
//
//        link1Button.setOnClickListener { view: View? ->
//            val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://www.example.com/link1")
//            )
//            startActivity(intent)
//        }
//
//        link2Button.setOnClickListener { view: View? ->
//            val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://www.example.com/link2")
//            )
//            startActivity(intent)
//        }
//
//        link3Button.setOnClickListener { view: View? ->
//            val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://www.example.com/link3")
//            )
//            startActivity(intent)
//        }
    }
}