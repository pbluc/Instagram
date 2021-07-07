package com.codepath.pbluc.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.codepath.pbluc.instagram.adapters.PostsAdapter;
import com.codepath.pbluc.instagram.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

  private static final String TAG = "FeedActivity";
  private static final int QUERY_AMOUNT_LIMIT = 20;

  private RecyclerView rvPosts;
  private SwipeRefreshLayout swipeContainer;
  private EndlessRecyclerViewScrollListener scrollListener;
  private Button btnCreatePost;

  protected PostsAdapter adapter;
  protected List<Post> allPosts;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);

    if (ParseUser.getCurrentUser() == null) {
      Intent i = new Intent(this, LoginActivity.class);
      startActivity(i);
      finish();
    }

    rvPosts = findViewById(R.id.rvPosts);
    swipeContainer = findViewById(R.id.swipeContainer);
    btnCreatePost = findViewById(R.id.btnCreatePost);

    // Setup refresh listener which triggers new data loading
    swipeContainer.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            fetchTimelineAsync(0);
          }
        });

    btnCreatePost.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            goMainActivity();
          }
        });

    // initialize the array that will hold posts and create a PostsAdapter
    allPosts = new ArrayList<>();
    adapter = new PostsAdapter(this, allPosts);

    // set adapter on the recycler view
    rvPosts.setAdapter(adapter);
    // set the layout manager on the recycler view
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    rvPosts.setLayoutManager(linearLayoutManager);
    // Retain an instance so that we can call `resetState()` for fresh searches
    scrollListener =
        new EndlessRecyclerViewScrollListener(linearLayoutManager) {
          @Override
          public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to bottom of the list
            loadNextDataFromParse(page);
          }
        };
    // Adds the scroll listener to RecyclerView
    rvPosts.addOnScrollListener(scrollListener);
    // query posts from Parstagram
    queryPosts();
  }

  private void loadNextDataFromParse(int page) {
    int allPostsSize = allPosts.size();
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // query searches for posts older than posts currently populating and orders by creation date (newest first)
    query.whereLessThan("createdAt", allPosts.get(allPosts.size() - 1).getCreatedAt()).addDescendingOrder("createdAt");
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

  private void goMainActivity() {
    Intent i = new Intent(this, MainActivity.class);
    startActivity(i);
  }

  private void fetchTimelineAsync(int i) {
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order posts by creation date (newest first)
    query.addDescendingOrder("createdAt");
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting refreshed posts", e);
              return;
            }

            // Clear out old items before appending in the new ones
            adapter.clear();
            // The data has come back, add new items to adapter
            adapter.addAll(posts);
            // Call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
          }
        });
  }

  private void queryPosts() {
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order posts by creation date (newest first)
    query.addDescendingOrder("createdAt");
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting posts", e);
              return;
            }

            // save received posts to list and notify adapter of new data
            allPosts.addAll(posts);
            adapter.notifyDataSetChanged();
          }
        });
  }
}
