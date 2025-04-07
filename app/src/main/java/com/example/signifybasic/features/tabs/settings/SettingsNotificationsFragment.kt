package com.example.signifybasic.features.tabs.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.signifybasic.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Calendar
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.example.signifybasic.features.tabs.notifications.ReminderReceiver
import com.example.signifybasic.features.utility.applyHighContrastToAllViews
import com.example.signifybasic.features.utility.applyTextSizeToAllTextViews
import com.example.signifybasic.features.utility.isHighContrastEnabled


class SettingsNotificationsFragment : Fragment(R.layout.notifications_preferences) {

    private lateinit var sharedPref: android.content.SharedPreferences

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Notifications won't work without permission", Toast.LENGTH_LONG).show()
        }
    }



    private fun formatTime(hour: Int, minute: Int): String {
        val isPM = hour >= 12
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        val formattedMinute = String.format("%02d", minute)
        val amPm = if (isPM) "PM" else "AM"
        return "$hour12:$formattedMinute $amPm"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        applyTextSizeToAllTextViews(view, requireContext())
        if (isHighContrastEnabled(requireContext())) {
            applyHighContrastToAllViews(view, requireContext())
        }

        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Initialize SharedPreferences
        sharedPref = requireContext().getSharedPreferences("NotificationSettings", Context.MODE_PRIVATE)

        // Grab views
        val switchPush = view.findViewById<MaterialSwitch>(R.id.switch_push_notifications)
        val switchEmail = view.findViewById<MaterialSwitch>(R.id.switch_email_updates)
        val tvReminderTime = view.findViewById<TextView>(R.id.tv_reminder_time)
        val btnChangeReminder = view.findViewById<MaterialButton>(R.id.btn_change_reminder)

        // Load saved states
        switchPush.isChecked = sharedPref.getBoolean("push_enabled", true)
        switchEmail.isChecked = sharedPref.getBoolean("email_enabled", false)
        tvReminderTime.text = sharedPref.getString("reminder_time", "08:00 AM")

        // Save toggles
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("push_enabled", isChecked).apply()
            Toast.makeText(requireContext(), if (isChecked) "Push notifications enabled" else "Push notifications disabled", Toast.LENGTH_SHORT).show()
        }

        switchEmail.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("email_enabled", isChecked).apply()
            Toast.makeText(requireContext(), if (isChecked) "Email updates enabled" else "Email updates disabled", Toast.LENGTH_SHORT).show()
        }

        val tvNotificationSound = view.findViewById<TextView>(R.id.tv_notification_sound)
        val btnChangeSound = view.findViewById<MaterialButton>(R.id.btn_change_sound)

        // Load saved sound name
        tvNotificationSound.text = sharedPref.getString("notification_sound", "Chime")

        btnChangeSound.setOnClickListener {
            val soundOptions = arrayOf("Chime", "Ding", "Pop")
            val currentSound = sharedPref.getString("notification_sound", "Chime")
            val selectedIndex = soundOptions.indexOf(currentSound)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Notification Sound")
                .setSingleChoiceItems(soundOptions, selectedIndex) { dialog, which ->
                    val selectedSound = soundOptions[which]
                    tvNotificationSound.text = selectedSound
                    sharedPref.edit().putString("notification_sound", selectedSound).apply()
                    Toast.makeText(requireContext(), "Sound set to $selectedSound", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(requireContext(), "Please allow exact alarms for daily reminders to work properly", Toast.LENGTH_LONG).show()
            }
        }


        // Handle time change
        btnChangeReminder.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                ContextThemeWrapper(requireContext(), R.style.TimePickerDialogTheme),
                { _: TimePicker, hour: Int, minute: Int ->
                    val formattedTime = formatTime(hour, minute)
                    tvReminderTime.text = formattedTime
                    sharedPref.edit().putString("reminder_time", formattedTime).apply()
                    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(requireContext(), ReminderReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        requireContext(),
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
                    }

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )


                    Toast.makeText(requireContext(), "Reminder set for $formattedTime", Toast.LENGTH_SHORT).show()
                },
                currentHour,
                currentMinute,
                false
            )
            timePicker.show()

        }
    }
}