package com.codepath.pbluc.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.pbluc.instagram.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "MainActivity";

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

    bottomNavigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
              case R.id.action_home:
                Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_LONG).show();
                break;
              case R.id.action_compose:
                Toast.makeText(MainActivity.this, "Compose!", Toast.LENGTH_LONG).show();
                break;
              case R.id.action_profile:
              default:
                Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_LONG).show();
                break;
            }
            return true;
          }
        });
    // Set default selection
    bottomNavigationView.setSelectedItemId(R.id.action_compose);
  }
}
