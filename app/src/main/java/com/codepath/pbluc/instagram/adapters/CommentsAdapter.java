package com.codepath.pbluc.instagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.models.Comment;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

  private static final String TAG = "CommentsAdapter";

  private Context context;
  private List<Comment> comments;

  public CommentsAdapter(Context context, List<Comment> comments) {
    this.context = context;
    this.comments = comments;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
    return new CommentsAdapter.ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
    Comment comment = comments.get(position);
    holder.bind(comment);
  }

  // Clean all elements of the recycler
  public void clear() {
    comments.clear();
    notifyDataSetChanged();
  }

  // Add a list of items
  public void addAll(List<Comment> list) {
    comments.addAll(list);
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return comments.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivProfileImage;
    private ImageView ivLikeComment;
    private CardView cardView;
    private TextView tvComment;
    private TextView tvCreatedAt;
    private TextView tvLikes;

    public ViewHolder(@NonNull View view) {
      super(view);
      ivProfileImage = view.findViewById(R.id.ivProfileImage);
      ivLikeComment = view.findViewById(R.id.ivLikeComment);
      cardView = view.findViewById(R.id.cardView);
      tvComment = view.findViewById(R.id.tvComment);
      tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
      tvLikes = view.findViewById(R.id.tvLikes);
    }

    public void bind(Comment comment) {
      ParseFile profileImage = comment.getProfileImage();
      if (profileImage != null) {
        Glide.with(context).load(profileImage.getUrl()).into(ivProfileImage);
      }

      tvComment.setText(comment.getUser().getUsername() + " " + comment.getComment());

      Date createdAt = comment.getCreatedAt();
      String timeAgo = Post.calculateTimeAgo(createdAt);
      tvCreatedAt.setText(timeAgo);

      Integer likes = comment.getLikeCount();
      if (likes == 0) {
        tvLikes.setVisibility(View.GONE);
      } else if (likes == 1) {
        tvLikes.setVisibility(View.VISIBLE);
        tvLikes.setText("1 like");
      } else {
        tvLikes.setVisibility(View.VISIBLE);
        tvLikes.setText(likes + " likes");
      }
    }
  }
}
