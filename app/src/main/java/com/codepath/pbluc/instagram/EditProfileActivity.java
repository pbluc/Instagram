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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

  private ProgressBar pb;

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

    pb = findViewById(R.id.pbLoading);

    parseUser = ParseUser.getCurrentUser();

    etBio.setText(parseUser.getString("bio"));
    etWebsite.setText(parseUser.getString("website"));
    etPronouns.setText(parseUser.getString("pronouns"));
    etUsername.setText(parseUser.getString("username"));
    etFullName.setText(parseUser.get("firstName") + " " + parseUser.getString("lastName"));

    tvChangeProfileImg.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            getNewProfilePhoto();
          }
        });

    cardView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            getNewProfilePhoto();
          }
        });

    ParseFile image = parseUser.getParseFile("profileImg");
    if (image != null) {
      Glide.with(this).load(image.getUrl()).into(ivProfileImage);
    }

    ivCloseActivity.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            goProfileFragment();
          }
        });

    ivSaveChanges.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.i(TAG, "Save changes onclick");
            try {
              pb.setVisibility(View.VISIBLE);
              updateParseUser();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }

  private void updateParseUser() throws IOException {

    if (parseUser != null) {

      if (imageUri != null) {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        byte[] imageByteArr = getBytes(inputStream);
        String photoName = parseUser.getObjectId() + "_profile_picture.png";

        ParseFile image = new ParseFile(photoName, imageByteArr);

        image.saveInBackground(
            new SaveCallback() {
              @Override
              public void done(ParseException e) {
                if (e == null) {
                  Log.i(TAG, "Parse file successfully saved");

                  String fullName = etFullName.getText().toString().trim();
                  String[] firstAndLastName = fullName.split(" ");

                  if (firstAndLastName.length != 2) {
                    Toast.makeText(
                            EditProfileActivity.this,
                            "First and last name must be entered!",
                            Toast.LENGTH_SHORT)
                        .show();
                    return;
                  }
                  parseUser.put("firstName", firstAndLastName[0]);
                  parseUser.put("lastName", firstAndLastName[1]);
                  parseUser.put("pronouns", etPronouns.getText().toString().trim());
                  parseUser.put("website", etWebsite.getText().toString().trim());
                  parseUser.put("bio", etBio.getText().toString().trim());
                  parseUser.put("profileImg", image);

                  parseUser.setUsername(etUsername.getText().toString().trim());

                  // Saves the object
                  parseUser.saveInBackground(
                      new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                          // Save successful
                          if (e == null) {
                            Log.i(TAG, "Save successful!");
                            pb.setVisibility(View.INVISIBLE);
                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "Save successful!",
                                    Toast.LENGTH_SHORT)
                                .show();
                            goProfileFragment();
                          } else {
                            // Something went wrong while saving
                            Log.e(TAG, "Save unsuccessful: " + e);
                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "Save unsuccessful",
                                    Toast.LENGTH_SHORT)
                                .show();
                          }
                        }
                      });
                } else {
                  Log.e(TAG, "Parse file was not saved: ", e);
                }
              }
            });
      }
    }
  }

  private byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
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
    if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
      imageUri = data.getData();
      Glide.with(this).load(imageUri).into(ivProfileImage);
    }
  }
}
