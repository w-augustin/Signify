package com.example.signifybasic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifications_activity) // Ensure this matches your actual XML filename

        val mainView =
            findViewById<View>(R.id.notifications) // Ensure this ID exists in `notifications_activity.xml`

        if (mainView != null) {  // Prevents NullPointerException
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v: View, insets: WindowInsetsCompat ->
                v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                )
                insets
            }
        }
    }
}