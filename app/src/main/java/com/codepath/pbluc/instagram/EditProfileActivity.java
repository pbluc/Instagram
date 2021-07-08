package com.codepath.pbluc.instagram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE = 100;

    private Uri imageUri;

    private ImageView ivCloseActivity;
    private ImageView ivSaveChanges;
    private ImageView ivProfileImage;
    private CardView cardView;
    private TextView tvChangeProfileImg;
    private EditText etFullName;
    private EditText etUsername;
    private EditText etPronouns;
    private EditText etWebsite;
    private EditText etBio;

    private ParseUser parseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivCloseActivity = findViewById(R.id.ivCloseActivity);
        ivSaveChanges = findViewById(R.id.ivSaveChanges);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        cardView = findViewById(R.id.cardView);
        tvChangeProfileImg = findViewById(R.id.tvChangeProfileImg);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPronouns = findViewById(R.id.etPronouns);
        etWebsite = findViewById(R.id.etWebsite);
        etBio = findViewById(R.id.etBio);

        parseUser = ParseUser.getCurrentUser();

        etBio.setText(parseUser.getString("bio"));
        etWebsite.setText(parseUser.getString("website"));
        etPronouns.setText(parseUser.getString("pronouns"));
        etUsername.setText(parseUser.getString("username"));
        etFullName.setText(parseUser.get("firstName") + " " + parseUser.getString("lastName"));

        tvChangeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewProfilePhoto();
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewProfilePhoto();
            }
        });

        ParseFile image = parseUser.getParseFile("profileImg");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivProfileImage);
        }

        ivCloseActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goProfileFragment();
            }
        });

        ivSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Save changes onclick");
                updateParseUser();
            }
        });


    }

    private void updateParseUser() {

        if(parseUser != null) {
            String fullName = etFullName.getText().toString().trim();
            String[] firstAndLastName = fullName.split(" ");

            if(firstAndLastName.length != 2) {
                Toast.makeText(this, "First and last name must be entered!", Toast.LENGTH_SHORT).show();
                return;
            }
            parseUser.put("firstName", firstAndLastName[0]);
            parseUser.put("lastName", firstAndLastName[1]);
            parseUser.put("pronouns", etPronouns.getText().toString().trim());
            parseUser.put("website", etWebsite.getText().toString().trim());
            parseUser.put("bio", etBio.getText().toString().trim());

            parseUser.setUsername(etUsername.getText().toString().trim());

            if(imageUri != null) {
                ParseFile image = new ParseFile(new File(imageUri.getPath()));
                parseUser.put("profileImg", image);
                Toast.makeText(this, "Image uri set", Toast.LENGTH_SHORT);
            }

            // Saves the object
            parseUser.saveInBackground(e -> {
                if(e == null) {
                    // Save successful
                    Log.i(TAG, "Save successful!");
                    Toast.makeText(this, "Save successful!", Toast.LENGTH_SHORT);
                    goProfileFragment();
                } else {
                    // Something went wrong while saving
                    Log.e(TAG, "Save unsuccessful: " + e);
                    Toast.makeText(this, "Save unsuccessful", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    private void goProfileFragment() {
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        intent.putExtra("openProfileFragment", true);
        startActivity(intent);
        finish();
    }

    private void getNewProfilePhoto() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(ivProfileImage);
        }
    }
}