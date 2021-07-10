package com.codepath.pbluc.instagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.pbluc.instagram.LoginActivity;
import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/** A simple {@link Fragment} subclass. */
public class ComposeFragment extends Fragment {

  private static final String TAG = "ComposeFragment";
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
  private File photoFile;
  public String photoFileName = "photo.jpg";

  private EditText etCaption;
  private Button btnLogout;
  private Button btnCaptureImage;
  private ImageView ivPostImage;
  private Button btnSubmit;

  private ProgressBar pb;

  public ComposeFragment() {
    // Required empty public constructor
  }

  // The onCreateView method is called when Fragment should create its View object hierarchy
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_compose, container, false);
  }

  // This event is triggered soon after onCreateView()
  // Any view setup should occur here. E.g., view lookups and attaching view listeners.
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    etCaption = view.findViewById(R.id.etCaption);
    btnLogout = view.findViewById(R.id.btnLogout);
    btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
    ivPostImage = view.findViewById(R.id.ivPostImage);
    btnSubmit = view.findViewById(R.id.btnSubmit);
    pb = view.findViewById(R.id.pbLoading);

    btnLogout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            logoutUser();
            goLoginActivity();
          }
        });

    btnCaptureImage.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            launchCamera();
          }
        });

    btnSubmit.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String caption = etCaption.getText().toString().trim();
            if (caption.isEmpty()) {
              Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_LONG).show();
              return;
            }
            if (photoFile == null || ivPostImage.getDrawable() == null) {
              Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_LONG).show();
              return;
            }
            ParseUser currentUser = ParseUser.getCurrentUser();

            pb.setVisibility(ProgressBar.VISIBLE);
            savePost(caption, currentUser, photoFile);
          }
        });
  }

  private void launchCamera() {
    // Create Intent to take a picture and return control to the calling application
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Create a File reference to access to future access
    photoFile = getPhotoFileUri(photoFileName);

    // wrap File object into content provider
    Uri fileProvider =
        FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

    // If you call startActivityForResult() using an intent that no app can handle, your app will
    // crash.
    // So as long as the result is not null, it's safe to use the intent.
    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
      // STart the image capture intent to take photo
      startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        // by this point we have the camera photo on disk
        Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        // RESIZE BITMAP, see section below
        // Load the taken image into a preview
        ivPostImage.setImageBitmap(takenImage);
      } else { // Result was a failure
        Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
      }
    }
  }

  // Returns the file for a photo stored on disk given the fileName
  private File getPhotoFileUri(String photoFileName) {
    // Get safe storage directory for photos
    // Use 'getExternalFilesDir' on Context to access package-specific directories
    // This way, we don't need to request external read/write runtime permissions
    File mediaStorageDir =
        new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
      Log.d(TAG, "failed to create directory");
    }

    // Return the file target for the photo based on filename
    return new File(mediaStorageDir.getPath() + File.separator + photoFileName);
  }

  private void savePost(String caption, ParseUser currentUser, File photoFile) {
    Post post = new Post();
    post.setCaption(caption);
    post.setImage(new ParseFile(photoFile));
    post.setUser(currentUser);
    post.saveInBackground(
        new SaveCallback() {
          @Override
          public void done(ParseException e) {
            if (e != null) {
              Log.e(TAG, "Error while saving", e);
              Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "Post save was successful!!");
            pb.setVisibility(ProgressBar.INVISIBLE);
            etCaption.setText("");
            ivPostImage.setImageResource(0);
          }
        });
  }

  private void logoutUser() {
    ParseUser.logOut();
    ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
  }

  private void goLoginActivity() {
    Intent i = new Intent(getContext(), LoginActivity.class);
    startActivity(i);
  }
}
