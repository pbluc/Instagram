package com.codepath.pbluc.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {

  public static final String TAG = "LoginActivity";

  private EditText etUsername;
  private EditText etPassword;
  private Button btnLogin;
  private Button btnSignUp;
  private ProgressBar pb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    if (ParseUser.getCurrentUser() != null) {
      goFeedActivity();
    }

    etUsername = findViewById(R.id.etUsername);
    etPassword = findViewById(R.id.etPassword);
    btnLogin = findViewById(R.id.btnLogin);
    btnSignUp = findViewById(R.id.btnSignUp);
    pb = findViewById(R.id.pbLoading);

    btnLogin.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.i(TAG, "onClick login button");
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            pb.setVisibility(View.VISIBLE);
            loginUser(username, password);
          }
        });

    btnSignUp.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.i(TAG, "onClick sign up button");
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            pb.setVisibility(View.VISIBLE);
            signUpUser(username, password);
          }
        });
  }

  private void signUpUser(String username, String password) {

    // Create the ParseUser
    ParseUser user = new ParseUser();
    // Set core properties
    user.setUsername(username);
    user.setPassword(password);
    // Invoke signUpInBackground
    user.signUpInBackground(
        new SignUpCallback() {
          @Override
          public void done(ParseException e) {
            if (e != null) {
              Toast.makeText(LoginActivity.this, "Issue with signup!!", Toast.LENGTH_LONG).show();
              return;
            }
            pb.setVisibility(View.INVISIBLE);
            goFeedActivity();
          }
        });
  }

  private void loginUser(String username, String password) {

    ParseUser.logInInBackground(
        username,
        password,
        new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException e) {
            if (e != null) {
              Toast.makeText(LoginActivity.this, "Issue with login!!", Toast.LENGTH_LONG).show();
              return;
            }
            pb.setVisibility(View.INVISIBLE);
            goFeedActivity();
          }
        });
  }

  private void goFeedActivity() {
    Intent i = new Intent(this, MainActivity.class);
    startActivity(i);
    finish();
  }
}
