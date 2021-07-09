package com.codepath.pbluc.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.pbluc.instagram.adapters.CommentsAdapter;
import com.codepath.pbluc.instagram.adapters.PostsAdapter;
import com.codepath.pbluc.instagram.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.pbluc.instagram.models.Comment;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

  private static final String TAG = "CommentsActivity";
  private static final int QUERY_AMOUNT_LIMIT = 20;

  private ImageView ivReturnHome;
  private ImageView ivProfileImage;
  private EditText etAddComment;
  private TextView tvPostComment;
  private SwipeRefreshLayout swipeContainer;
  private EndlessRecyclerViewScrollListener scrollListener;
  private RecyclerView rvComments;

  private ProgressBar pb;

  private CommentsAdapter adapter;
  private List<Comment> allComments;

  private ParseUser parseUser;
  private Post relatedPost;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_comments);

    ivReturnHome = findViewById(R.id.ivReturnHome);
    swipeContainer = findViewById(R.id.swipeContainer);
    rvComments = findViewById(R.id.rvComments);
    etAddComment = findViewById(R.id.etAddComment);
    tvPostComment = findViewById(R.id.tvPostComment);
    ivProfileImage = findViewById(R.id.ivProfileImage);

    pb = findViewById(R.id.pbLoading);

    parseUser = ParseUser.getCurrentUser();
    ParseFile parseUserProfileImg = parseUser.getParseFile("profileImg");
    relatedPost = getIntent().getParcelableExtra("relatedPostToComments");

    Glide.with(this).load(parseUserProfileImg.getUrl()).into(ivProfileImage);

    ivReturnHome.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            // TODO: Go back to post where comments came from
          }
        });

    tvPostComment.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String comment = etAddComment.getText().toString().trim();
            if (!comment.equals("")) {
              pb.setVisibility(View.VISIBLE);
              saveComment(parseUser, parseUserProfileImg, comment);
            } else {
              Toast.makeText(CommentsActivity.this, "Did not enter a comment!", Toast.LENGTH_SHORT)
                  .show();
              return;
            }
          }
        });

    // Setup refresh listener which triggers new data loading
    swipeContainer.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            fetchTimelineAsync(0);
          }
        });

    // initialize the array that will hold posts and create a PostsAdapter
    allComments = new ArrayList<>();
    adapter = new CommentsAdapter(this, allComments);

    // set adapter on the recycler view
    rvComments.setAdapter(adapter);
    // set the layout manager on the recycler view
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    rvComments.setLayoutManager(linearLayoutManager);
    // Retain an instance so that we can call `resetState()` for fresh searches
    scrollListener =
        new EndlessRecyclerViewScrollListener(linearLayoutManager) {
          @Override
          public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to bottom of the list
            pb.setVisibility(View.VISIBLE);
            loadNextDataFromParse(page);
          }
        };
    // Adds the scroll listener to RecyclerView
    rvComments.addOnScrollListener(scrollListener);

    pb.setVisibility(View.VISIBLE);
    // query comments from Parstagram
    queryComments();
  }

  private void saveComment(ParseUser parseUser, ParseFile parseUserProfileImg, String comment) {
    Comment newComment = new Comment();
    newComment.setComment(comment);
    newComment.setProfileImage(parseUserProfileImg);
    newComment.setUser(parseUser);
    newComment.saveInBackground(
        new SaveCallback() {
          @Override
          public void done(ParseException e) {
            if (e != null) {
              Log.e(TAG, "Error while saving", e);
              Toast.makeText(CommentsActivity.this, "Error while saving!", Toast.LENGTH_LONG)
                  .show();
              return;
            }
            Log.i(TAG, "Comment save was successful!!");
            pb.setVisibility(ProgressBar.INVISIBLE);
            etAddComment.setText("");

            // TODO: Add newly saved comment to Post object in Parse
              if(relatedPost.getCommentsArray() != null) {
                  // Post has comments


              } else {
                  // Post does not have comments
                  ArrayList<Comment> list = new ArrayList<>();
                  list.add(newComment);

                  relatedPost.put(Post.KEY_COMMENTS, list);

                  relatedPost.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException e) {
                          // Save successful
                          if(e == null) {
                              Log.i(TAG, "Save successful!");
                          } else {
                             //Something went wrong while saving
                              Log.e(TAG, "Save unsuccessful: " + e);
                          }
                      }
                  });

              }

            allComments.add(newComment);
            adapter.notifyItemInserted(allComments.size() - 1);
          }
        });
  }

  private void queryComments() {
    // specify what type of data we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // include data referred by user key
    query.include(Comment.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order comments by creation date (newest first)
    query.addDescendingOrder("createdAt");
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting comments", e);
              return;
            }
            pb.setVisibility(View.INVISIBLE);

            // save received posts to list and notify adapter of new data
            allComments.addAll(comments);
            adapter.notifyDataSetChanged();
          }
        });
  }

  private void loadNextDataFromParse(int page) {
    int allCommentsSize = allComments.size();
    // specify what type of data we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // include data referred by user key
    query.include(Comment.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // query searches for posts older than comments currently populating and orders by creation date
    // (newest first)
    query
        .whereLessThan("createdAt", allComments.get(allComments.size() - 1).getCreatedAt())
        .addDescendingOrder("createdAt");
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            Log.i(TAG, "Loaded comments: " + comments.toString());
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting more loaded comments", e);
              return;
            }
            pb.setVisibility(View.INVISIBLE);

            // save received posts to list and notify adapter of new data
            allComments.addAll(comments);
            adapter.notifyItemRangeInserted(allCommentsSize, comments.size());
          }
        });
  }

  private void fetchTimelineAsync(int i) {
    // specify what type of data we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // include data referred by user key
    query.include(Comment.KEY_USER);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order comments by creation date (newest first)
    query.addDescendingOrder("createdAt");
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            // check for errors
            if (e != null) {
              Log.e(TAG, "Issue with getting refreshed comments", e);
              return;
            }

            // Clear out old items before appending in the new ones
            adapter.clear();
            // The data has come back, add new items to adapter
            adapter.addAll(comments);
            // Call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
          }
        });
  }
}
