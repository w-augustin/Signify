package com.example.signifybasic.features.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.signifybasic.features.activitycenter.ActivityCenter;
import com.example.signifybasic.R;
import com.example.signifybasic.features.tabs.resources.ResourcesPage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_center);

        CardView cardSettings = findViewById(R.id.card_settings);
        CardView cardDiscussion = findViewById(R.id.card_discussion);
//        CardView cardModules = findViewById(R.id.card_modules);
        CardView cardActivityCenter = findViewById(R.id.card_activity_center);
        CardView cardResources = findViewById(R.id.card_resources);

        cardSettings.setOnClickListener(view -> startActivity(new Intent(HomePage.this, SettingsPage.class)));
        cardDiscussion.setOnClickListener(view -> startActivity(new Intent(HomePage.this, DiscussionPage.class)));
//        cardModules.setOnClickListener(view -> startActivity(new Intent(WelcomeCenter.this, ModulesActivity.class)));
        cardActivityCenter.setOnClickListener(view -> startActivity(new Intent(HomePage.this, ActivityCenter.class)));
        cardResources.setOnClickListener(view -> startActivity(new Intent(HomePage.this, ResourcesPage.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(HomePage.this, HomePage.class);
                } else if (item.getItemId() == R.id.nav_achievements) {
                    intent = new Intent(HomePage.this, AchievementsPage.class);
                } else if (item.getItemId() == R.id.nav_notifications) {
                    intent = new Intent(HomePage.this, NotificationsPage.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(HomePage.this, ProfilePage.class);
                }

                if (intent != null) {
                    startActivity(intent);
                }

                return true;
            }
        });
    }
}
