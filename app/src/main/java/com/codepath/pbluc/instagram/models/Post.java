package com.codepath.pbluc.instagram.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {
  public static final String KEY_CAPTION = "caption";
  public static final String KEY_IMAGE = "image";
  public static final String KEY_USER = "user";
  public static final String KEY_COMMENTS = "comments";
  private static final String TAG = "Post.java";

  private boolean expandCaption = false;

  public String getCaption() {
    return getString(KEY_CAPTION);
  }

  public void setCaption(String caption) {
    put(KEY_CAPTION, caption);
  }

  public ParseFile getImage() {
    return getParseFile(KEY_IMAGE);
  }

  public void setImage(ParseFile parseFile) {
    put(KEY_IMAGE, parseFile);
  }

  public ParseUser getUser() {
    return getParseUser(KEY_USER);
  }

  public void setUser(ParseUser user) {
    put(KEY_USER, user);
  }

  public boolean isExpandCaption() {
    return expandCaption;
  }

  public void setExpandCaption(boolean captionState) {
    expandCaption = captionState;
  }

  public List<Comment> getCommentsArray() {
    return getList(KEY_COMMENTS);
  }

  public void setCommentArray(List<Comment> commentArray) {
    put(KEY_COMMENTS, commentArray);
  }

  @NonNull
  @Override
  public String toString() {
    return "{ User: " + getUser().getUsername() + "}";
  }

  public static String calculateTimeAgo(Date createdAt) {

    int SECOND_MILLIS = 1000;
    int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    int DAY_MILLIS = 24 * HOUR_MILLIS;

    try {
      createdAt.getTime();
      long time = createdAt.getTime();
      long now = System.currentTimeMillis();

      final long diff = now - time;
      if (diff < MINUTE_MILLIS) {
        return "just now";
      } else if (diff < 2 * MINUTE_MILLIS) {
        return "a minute ago";
      } else if (diff < 50 * MINUTE_MILLIS) {
        return diff / MINUTE_MILLIS + " m";
      } else if (diff < 90 * MINUTE_MILLIS) {
        return "an hour ago";
      } else if (diff < 24 * HOUR_MILLIS) {
        return diff / HOUR_MILLIS + " h";
      } else if (diff < 48 * HOUR_MILLIS) {
        return "yesterday";
      } else {
        return diff / DAY_MILLIS + " d";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "";
  }
}
