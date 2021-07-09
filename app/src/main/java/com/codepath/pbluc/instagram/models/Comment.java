package com.codepath.pbluc.instagram.models;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
  public static final String KEY_USER = "user";
  public static final String KEY_PROF_IMG = "profileImg";
  public static final String KEY_COMMENT = "comment";
  public static final String KEY_LIKES = "likes";

  private static final String TAG = "Comment.java";

  public ParseUser getUser() {
    return getParseUser(KEY_USER);
  }

  public void setUser(ParseUser user) {
    put(KEY_USER, user);
  }

  public ParseFile getProfileImage() {
    return getParseFile(KEY_PROF_IMG);
  }

  public void setProfileImage(ParseFile parseFile) {
    put(KEY_PROF_IMG, parseFile);
  }

  public String getComment() {
    return getString(KEY_COMMENT);
  }

  public void setComment(String comment) {
    put(KEY_COMMENT, comment);
  }

  public Integer getLikeCount() {
    return getInt(KEY_LIKES);
  }

  public void updateLikeCount() {
    put(KEY_LIKES, getLikeCount() + 1);
  }

  @NonNull
  @Override
  public String toString() {
    return "{Comment: " + getComment() + "}";
  }
}
