package com.codepath.pbluc.instagram.models;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
  public static final String KEY_USER_COMMENT = "userCommentor";
  public static final String KEY_USER_POST = "userRelatedPost";
  public static final String KEY_PROF_IMG = "profileImg";
  public static final String KEY_COMMENT = "comment";
  public static final String KEY_LIKES = "likes";


  public ParseUser getUserCommentor() {
    return getParseUser(KEY_USER_COMMENT);
  }

  public void setUserCommentor(ParseUser user) {
    put(KEY_USER_COMMENT, user);
  }

  public ParseUser getUserRelatedPost() {
    return getParseUser(KEY_USER_POST);
  }

  public void setUserRelatedPost(ParseUser user) {
    put(KEY_USER_POST, user);
  }

  public ParseFile getProfileImage() {
    return getParseFile(KEY_PROF_IMG);
  }

  public void setProfileImage(ParseFile parseFile) {
    put(KEY_PROF_IMG, parseFile);
  }

  public String getComment() throws ParseException {
    return fetchIfNeeded().getString(KEY_COMMENT);
  }

  public void setComment(String comment) {
    put(KEY_COMMENT, comment);
  }

  public Integer getLikeCount() {
    return getInt(KEY_LIKES);
  }


  @NonNull
  @Override
  public String toString() {
    try {
      return "{Comment: " + getComment() + "}";
    } catch (ParseException e) {
      e.printStackTrace();
      return e.toString();
    }
  }
}
