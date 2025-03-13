package com.example.signifybasic;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WelcomeCenter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_center);

        CardView cardSettings = findViewById(R.id.card_settings);
        CardView cardDiscussion = findViewById(R.id.card_discussion);
//        CardView cardModules = findViewById(R.id.card_modules);
        CardView cardActivityCenter = findViewById(R.id.card_activity_center);
        CardView cardResources = findViewById(R.id.card_resources);

        cardSettings.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, SettingsActivity.class)));
        cardDiscussion.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, DiscussionActivity.class)));
//        cardModules.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, ModulesActivity.class)));
        cardActivityCenter.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, ActivityCenterActivity.class)));
        cardResources.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, ResourcesActivity.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(WelcomeCenter.this, HomeActivity.class);
                } else if (item.getItemId() == R.id.nav_achievements) {
                    intent = new Intent(WelcomeCenter.this, AchievementsActivity.class);
                } else if (item.getItemId() == R.id.nav_notifications) {
                    intent = new Intent(WelcomeCenter.this, NotificationsActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(WelcomeCenter.this, ProfileActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                }

                return true;
            }
        });
    }
}
