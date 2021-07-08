package com.codepath.pbluc.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.codepath.pbluc.instagram.fragments.ComposeFragment;
import com.codepath.pbluc.instagram.fragments.FeedFragment;
import com.codepath.pbluc.instagram.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  final FragmentManager fragmentManager = getSupportFragmentManager();
  private BottomNavigationView bottomNavigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (ParseUser.getCurrentUser() == null) {
      Intent i = new Intent(this, LoginActivity.class);
      startActivity(i);
      finish();
    }

    bottomNavigationView = findViewById(R.id.bottom_navigation);

    Bundle extras = getIntent().getExtras();
    if(extras != null && extras.containsKey("openProfileFragment")) {
      boolean openProfileFragment = extras.getBoolean("openProfileFragment");
      if(openProfileFragment) {
        bottomNavigationView.setSelectedItemId(R.id.action_profile);
        extras = null;
        fragmentManager.beginTransaction().replace(R.id.flContainer, new ProfileFragment()).commit();
      }
    } else {
      // Set default selection
      bottomNavigationView.setSelectedItemId(R.id.action_home);
      fragmentManager.beginTransaction().replace(R.id.flContainer, new FeedFragment()).commit();
    }

    bottomNavigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
              case R.id.action_home:
                //Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                fragment = new FeedFragment();
                break;
              case R.id.action_compose:
                //Toast.makeText(MainActivity.this, "Compose!", Toast.LENGTH_SHORT).show();
                fragment = new ComposeFragment();
                break;
              case R.id.action_profile:
                //Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                fragment = new ProfileFragment();
                break;
              default:
                //Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                fragment = new FeedFragment();
                break;
            }

            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            return true;
          }
        });
  }
}
