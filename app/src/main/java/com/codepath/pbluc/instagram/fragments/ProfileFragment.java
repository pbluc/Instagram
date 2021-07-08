package com.codepath.pbluc.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ProfileFragment extends FeedFragment {

  private static final String TAG = "ProfileFragment";
  private static final int NUMBER_OF_COLUMNS = 3;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    rvPosts.setLayoutManager(new GridLayoutManager(getContext(), NUMBER_OF_COLUMNS));
  }

  @Override
  protected void queryPosts() {
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
    query.setLimit(QUERY_AMOUNT_LIMIT);
    query.addDescendingOrder(Post.KEY_CREATED_AT);
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting posts", e);
              return;
            }
            Log.i(TAG, "Posts: " + posts.toString());
            allPosts.addAll(posts);
            adapter.notifyDataSetChanged();
          }
        });
  }

  @Override
  protected void loadNextDataFromParse(int page) {
    int allPostsSize = allPosts.size();
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // query searches for posts older than posts currently populating and orders by creation date
    // (newest first)
    query
        .whereLessThan(Post.KEY_CREATED_AT, allPosts.get(allPosts.size() - 1).getCreatedAt())
        .addDescendingOrder(Post.KEY_CREATED_AT);
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            Log.i(TAG, "Loaded posts: " + posts.toString());
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting more loaded posts", e);
              return;
            }

            // save received posts to list and notify adapter of new data
            allPosts.addAll(posts);
            adapter.notifyItemRangeInserted(allPostsSize, posts.size());
          }
        });
  }
}
