package com.codepath.pbluc.instagram.apps;

import android.app.Application;

import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.models.Comment;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    // Register your parse models
    ParseObject.registerSubclass(Post.class);
    ParseObject.registerSubclass(Comment.class);
    // Add initialization code here
    Parse.initialize(
        new Parse.Configuration.Builder(this)
            .applicationId(getString(R.string.back4app_app_id))
            .clientKey(getString(R.string.back4app_client_key))
            .server(getString(R.string.back4app_server_url))
            .build());
  }
}
