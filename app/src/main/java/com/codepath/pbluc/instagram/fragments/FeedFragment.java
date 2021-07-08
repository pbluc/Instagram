package com.codepath.pbluc.instagram.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.adapters.PostsAdapter;
import com.codepath.pbluc.instagram.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/** A simple {@link Fragment} subclass. */
public class FeedFragment extends Fragment {

  private static final String TAG = "FeedFragment";
  protected static final int QUERY_AMOUNT_LIMIT = 20;

  private RecyclerView rvPosts;
  private SwipeRefreshLayout swipeContainer;
  private EndlessRecyclerViewScrollListener scrollListener;

  private PostsAdapter adapter;
  private List<Post> allPosts;

  public FeedFragment() {
      // Required empty public constructor
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_feed, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    rvPosts = view.findViewById(R.id.rvPosts);
    swipeContainer = view.findViewById(R.id.swipeContainer);

    // Setup refresh listener which triggers new data loading
    swipeContainer.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            fetchTimelineAsync(0);
          }
        });

    // initialize the array that will hold posts and create a PostsAdapter
    allPosts = new ArrayList<>();
    adapter = new PostsAdapter(getContext(), allPosts);

    // set adapter on the recycler view
    rvPosts.setAdapter(adapter);
    // set the layout manager on the recycler view
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
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
    // query searches for posts older than posts currently populating and orders by creation date
    // (newest first)
    query
        .whereLessThan("createdAt", allPosts.get(allPosts.size() - 1).getCreatedAt())
        .addDescendingOrder("createdAt");
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