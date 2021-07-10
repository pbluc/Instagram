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

    // initialize the array that will hold posts and create a PostsAdapter
    allComments = new ArrayList<>();
    adapter = new CommentsAdapter(this, allComments);

    // set adapter on the recycler view
    rvComments.setAdapter(adapter);
    // set the layout manager on the recycler view
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    rvComments.setLayoutManager(linearLayoutManager);

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
              saveComment(parseUser, relatedPost.getUser(), parseUserProfileImg, comment);
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
            fetchTimelineAsync();
          }
        });

    // Retain an instance so that we can call `resetState()` for fresh searches
    scrollListener =
        new EndlessRecyclerViewScrollListener(linearLayoutManager) {
          @Override
          public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to bottom of the list
            pb.setVisibility(View.VISIBLE);
            loadNextDataFromParse();
          }
        };
    // Adds the scroll listener to RecyclerView
    rvComments.addOnScrollListener(scrollListener);

    pb.setVisibility(View.VISIBLE);
    // query comments from Parstagram
    queryComments();
  }

  private void saveComment(
      ParseUser userCommentor,
      ParseUser userRelatedPost,
      ParseFile parseUserProfileImg,
      String comment) {
    Comment newComment = new Comment();
    newComment.setComment(comment);
    newComment.setProfileImage(parseUserProfileImg);
    newComment.setUserCommentor(userCommentor);
    newComment.setUserRelatedPost(userRelatedPost);
    newComment.saveInBackground(
        new SaveCallback() {
          @Override
          public void done(ParseException e) {
            if (e != null) {
              Toast.makeText(CommentsActivity.this, "Error while saving!", Toast.LENGTH_LONG)
                  .show();
              return;
            }
            pb.setVisibility(ProgressBar.INVISIBLE);
            etAddComment.setText("");

            if (relatedPost.getCommentsArray() != null) {
              // Post has comments
              List<Comment> existingComments = relatedPost.getCommentsArray();
              existingComments.add(newComment);

              relatedPost.setCommentArray(existingComments);
            } else {
              // Post does not have comments
              List<Comment> newCommentArray = new ArrayList<>();
              newCommentArray.add(newComment);

              relatedPost.setCommentArray(newCommentArray);
            }

            relatedPost.saveInBackground(
                new SaveCallback() {
                  @Override
                  public void done(ParseException e) {
                    // Save successful
                    pb.setVisibility(View.INVISIBLE);
                    if (e == null) {
                      allComments.add(newComment);
                      adapter.notifyItemInserted(allComments.size() - 1);
                    } else {
                      // Something went wrong while saving
                      return;
                    }
                  }
                });
          }
        });
  }

  private void queryComments() {
    // Specify what type of date we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order comments by creation date (oldest first)
    query.addAscendingOrder(Comment.KEY_CREATED_AT);
    // start an asynchronous call for comments
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            pb.setVisibility(ProgressBar.INVISIBLE);
            if (e == null) {
              // Query was successful

              List<Comment> relatedPostComments = new ArrayList<>();
              for (Comment c : comments) {
                if (c.getUserRelatedPost()
                    .getObjectId()
                    .equals(relatedPost.getUser().getObjectId())) {
                  relatedPostComments.add(c);
                }
              }

              if (!relatedPostComments.isEmpty()) {
                // Save received posts to list and notify adapter of new data
                allComments.clear();
                allComments.addAll(relatedPostComments);
                adapter.notifyDataSetChanged();
              }

            } else {
              // Query was not successful
              return;
            }
          }
        });
  }

  private void loadNextDataFromParse() {
    int allCommentsSize = allComments.size();
    if (allCommentsSize < 1) {
      return;
    }

    // Specify what type of date we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // query searches for posts older than posts currently populating
    query.whereLessThan(Comment.KEY_CREATED_AT, allComments.get(0).getCreatedAt());
    // order comments by creation date (oldest first)
    query.addAscendingOrder(Comment.KEY_CREATED_AT);
    // start an asynchronous call for comments
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            pb.setVisibility(ProgressBar.INVISIBLE);
            if (e == null) {
              // Query was successful

              List<Comment> relatedPostComments = new ArrayList<>();
              for (Comment c : comments) {
                if (c.getUserRelatedPost().getObjectId().equals(relatedPost.getUser().getObjectId())
                    && !allComments.contains(c)) {
                  relatedPostComments.add(c);
                }
              }

              if (!relatedPostComments.isEmpty()) {
                // Save received posts to list and notify adapter of new data
                allComments.addAll(relatedPostComments);
                adapter.notifyItemRangeInserted(allCommentsSize, relatedPostComments.size());
              }

            } else {
              // Query was not successful
              return;
            }
          }
        });
  }

  private void fetchTimelineAsync() {
    // Specify what type of date we want to query - Comment.class
    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order comments by creation date (oldest first)
    query.addAscendingOrder(Comment.KEY_CREATED_AT);
    // start an asynchronous call for comments
    query.findInBackground(
        new FindCallback<Comment>() {
          @Override
          public void done(List<Comment> comments, ParseException e) {
            swipeContainer.setRefreshing(false);
            if (e == null) {
              // Query was successful

              List<Comment> relatedPostComments = new ArrayList<>();
              for (Comment c : comments) {
                if (c.getUserRelatedPost()
                    .getObjectId()
                    .equals(relatedPost.getUser().getObjectId())) {
                  relatedPostComments.add(c);
                }
              }

              if (!relatedPostComments.isEmpty()) {
                // Save received posts to list and notify adapter of new data
                adapter.clear();
                adapter.addAll(relatedPostComments);
              }
            } else {
              // Query was not successful
              return;
            }
          }
        });
  }
}
