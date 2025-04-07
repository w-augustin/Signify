package com.example.signifybasic.features.tabs.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.signifybasic.database.DBHelper
import com.example.signifybasic.databinding.FragmentNotificationsBinding
import com.example.signifybasic.R
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val dbHelper = DBHelper(requireContext())
        val notifications = dbHelper.getAllNotifications()

        val screenContainer = _binding!!.achievementsScroll.getChildAt(0) as LinearLayout
        screenContainer.removeAllViews()

        val inflater = LayoutInflater.from(context)
        applyTextSizeToAllTextViews(binding.root, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(binding.root, requireContext())
        }


//        val clearButton = Button(requireContext()).apply {
//            text = "Clear All Notifications"
//            setBackgroundResource(R.drawable.button_background)
//            setTextColor(resources.getColor(android.R.color.white, null))
//            setPadding(24, 12, 24, 12)
//            setOnClickListener {
//                dbHelper.clearAllNotifications()
//                screenContainer.removeAllViews()
//                Toast.makeText(requireContext(), "Notifications cleared", Toast.LENGTH_SHORT).show()
//            }
//        }
//        screenContainer.addView(clearButton)

        if (notifications.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No notifications yet."
                setTextColor(resources.getColor(android.R.color.white, null))
                textSize = 16f
                setPadding(32, 32, 32, 32)
            }
            screenContainer.addView(emptyText)
        } else {
            for (item in notifications) {
                val view = inflater.inflate(R.layout.notification_card_item, screenContainer, false)

                val date = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(item.timestamp))
                val time = SimpleDateFormat("hh:mm a", Locale.US).format(Date(item.timestamp))

                view.findViewById<TextView>(R.id.date_text).text = date
                view.findViewById<TextView>(R.id.time_text).text = time
                view.findViewById<TextView>(R.id.message_text).text = item.message

                screenContainer.addView(view)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
