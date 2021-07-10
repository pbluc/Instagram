package com.codepath.pbluc.instagram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.codepath.pbluc.instagram.EditProfileActivity;
import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.adapters.PostsAdapter;
import com.codepath.pbluc.instagram.adapters.ProfileAdapter;
import com.codepath.pbluc.instagram.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends FeedFragment {

  private static final String TAG = "ProfileFragment";
  private static final int NUMBER_OF_COLUMNS = 3;

  private TextView tvUsername;
  private TextView tvPostsCount;
  private TextView tvFollowersCount;
  private TextView tvFollowingCount;
  private TextView tvUserFullName;
  private TextView tvPronouns;
  private TextView tvBio;
  private TextView tvWebsite;
  private Button btnEditProfile;
  private ImageView ivProfileImage;

  private SwipeRefreshLayout swipeContainer;
  private EndlessRecyclerViewScrollListener scrollListener;
  private RecyclerView rvPosts;
  private ProgressBar pb;

  private List<Post> allPosts;
  private ProfileAdapter adapter;

  private ParseUser profileUser;

  public ProfileFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    tvUsername = view.findViewById(R.id.tvUsername);
    tvPostsCount = view.findViewById(R.id.tvPostsCount);
    tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
    tvFollowingCount = view.findViewById(R.id.tvFollowingCount);
    tvUserFullName = view.findViewById(R.id.tvUserFullName);
    tvPronouns = view.findViewById(R.id.tvPronouns);
    tvBio = view.findViewById(R.id.tvBio);
    tvWebsite = view.findViewById(R.id.tvWebsite);
    btnEditProfile = view.findViewById(R.id.btnEditProfile);
    ivProfileImage = view.findViewById(R.id.ivProfileImage);
    swipeContainer = view.findViewById(R.id.swipeContainer);
    rvPosts = view.findViewById(R.id.rvPosts);
    pb = view.findViewById(R.id.pbLoading);

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      profileUser = bundle.getParcelable("otherParseUser");
    } else {
      profileUser = ParseUser.getCurrentUser();
    }

    tvUsername.setText(profileUser.getUsername());
    tvPostsCount.setText(profileUser.getString("posts"));
    tvFollowersCount.setText(profileUser.getString("followers"));
    tvFollowingCount.setText(profileUser.getString("following"));
    tvUserFullName.setText(
        profileUser.getString("firstName") + " " + profileUser.getString("lastName"));
    tvPronouns.setText(profileUser.getString("pronouns"));
    tvBio.setText(profileUser.getString("bio"));
    tvWebsite.setText(profileUser.getString("website"));

    ParseFile image = profileUser.getParseFile("profileImg");
    if (image != null) {
      Glide.with(getContext()).load(image.getUrl()).into(ivProfileImage);
    }

    allPosts = new ArrayList<>();
    adapter = new ProfileAdapter(getContext(), allPosts);
    // set adapter on the recycler view
    rvPosts.setAdapter(adapter);
    // set the layout manager on the recycler view
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), NUMBER_OF_COLUMNS);
    rvPosts.setLayoutManager(gridLayoutManager);

    if (!profileUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
      // Profile displayed is not that of current user
      btnEditProfile.setVisibility(View.GONE);
    } else {
      btnEditProfile.setVisibility(View.VISIBLE);
      btnEditProfile.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              goEditProfileActivity();
            }
          });
    }

    swipeContainer.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            fetchTimelineAsync();
          }
        });

    // Retain an instance so that we can call `resetState()` for fresh searches
    scrollListener =
        new EndlessRecyclerViewScrollListener(gridLayoutManager) {
          @Override
          public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to bottom of the list
            pb.setVisibility(View.VISIBLE);
            loadNextDataFromParse(page);
          }
        };
    // Adds the scroll listener to RecyclerView
    rvPosts.addOnScrollListener(scrollListener);

    pb.setVisibility(View.VISIBLE);
    // query posts from Parstagram
    queryPosts();
  }

  private void goEditProfileActivity() {
    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
    startActivity(intent);
  }

  private void queryPosts() {
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_USER, profileUser);
    query.setLimit(QUERY_AMOUNT_LIMIT);
    query.addDescendingOrder(Post.KEY_CREATED_AT);
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            // check for errors
            if (e != null) {
              return;
            }

            pb.setVisibility(View.INVISIBLE);

            allPosts.addAll(posts);
            adapter.notifyDataSetChanged();
          }
        });
  }

  private void loadNextDataFromParse(int page) {
    int allPostsSize = allPosts.size();
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_USER, profileUser);
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
              return;
            }

            pb.setVisibility(View.INVISIBLE);

            // save received posts to list and notify adapter of new data
            allPosts.addAll(posts);
            adapter.notifyItemRangeInserted(allPostsSize, posts.size());
          }
        });
  }

  private void fetchTimelineAsync() {
    // specify what type of data we want to query - Post.class
    ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
    // include data referred by user key
    query.include(Post.KEY_USER);
    query.whereEqualTo(Post.KEY_USER, profileUser);
    // limit query to latest 20 items
    query.setLimit(QUERY_AMOUNT_LIMIT);
    // order posts by creation date (newest first)
    query.addDescendingOrder(Post.KEY_CREATED_AT);
    // start an asynchronous call for posts
    query.findInBackground(
        new FindCallback<Post>() {
          @Override
          public void done(List<Post> posts, ParseException e) {
            // check for errors
            if (e != null) {
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
}
