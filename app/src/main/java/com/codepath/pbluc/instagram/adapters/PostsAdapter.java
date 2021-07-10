package com.codepath.pbluc.instagram.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.util.Log;
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
import com.codepath.pbluc.instagram.models.Post;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

  private static final String TAG = "PostsAdapter";
  public static final int CAPTION_MAX_LENGTH = 125;

  private Context context;
  private List<Post> posts;

  private final ListItemClickListener mOnClickListener;

  public PostsAdapter(Context context, List<Post> posts, ListItemClickListener onClickListener) {
    this.context = context;
    this.posts = posts;
    this.mOnClickListener = onClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Post post = posts.get(position);
    holder.bind(post);

    holder.tvCaption.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.i(TAG, "View holder onClick success");
            Post post = posts.get(position);
            boolean expandCaption = post.isExpandCaption();

            InputFilter[] fArray = new InputFilter[1];
            if (expandCaption) {
              fArray[0] = new InputFilter.LengthFilter(CAPTION_MAX_LENGTH);
            } else {
              fArray[0] = new InputFilter.LengthFilter(post.getCaption().length());
            }
            holder.tvCaption.setFilters(fArray);
            holder.tvCaption.setText(post.getCaption());

            post.setExpandCaption(!expandCaption);
          }
        });
  }

  @Override
  public int getItemCount() {
    return posts.size();
  }

  // Clean all elements of the recycler
  public void clear() {
    posts.clear();
    notifyDataSetChanged();
  }

  // Add a list of items
  public void addAll(List<Post> list) {
    posts.addAll(list);
    notifyDataSetChanged();
  }

  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView tvUsername;
    private TextView tvCaption;
    private TextView tvCreatedAt;
    private ImageView ivImage;
    private ImageView ivProfileImage;
    private ImageView ivOpenComments;
    private ImageView ivSaveIcon;
    private CardView cardView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvUsername = itemView.findViewById(R.id.tvUsername);
      tvCaption = itemView.findViewById(R.id.tvCaption);
      tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
      ivImage = itemView.findViewById(R.id.ivImage);
      ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
      ivOpenComments = itemView.findViewById(R.id.ivOpenComments);
      cardView = itemView.findViewById(R.id.cardView);
      ivSaveIcon = itemView.findViewById(R.id.ivSaveIcon);

      tvUsername.setOnClickListener(this);
      cardView.setOnClickListener(this);
      ivOpenComments.setOnClickListener(this);
    }

    public void bind(Post post) {
      // Bind the post data to the view elements
      tvCaption.setText(post.getCaption());
      tvUsername.setText(post.getUser().getUsername());

      Date createdAt = post.getCreatedAt();
      String timeAgo = Post.calculateTimeAgo(createdAt);
      tvCreatedAt.setText(timeAgo);

      ParseFile image = post.getImage();
      if (image != null) {
        Glide.with(context).load(image.getUrl()).into(ivImage);
      }

      ParseFile profileImage = post.getUser().getParseFile("profileImg");
      if (profileImage != null) {
        Glide.with(context).load(profileImage.getUrl()).into(ivProfileImage);
      }
    }

    @Override
    public void onClick(View view) {
      int position = getAdapterPosition();
      switch (view.getId()) {
        case R.id.tvUsername:
        case R.id.cardView:
          mOnClickListener.onUserProfileTo(position);
          break;
        case R.id.ivOpenComments:
          mOnClickListener.openCommentsActivity(position);
        default:
          break;
      }
    }
  }

  public interface ListItemClickListener {
    void onUserProfileTo(int position);
    void openCommentsActivity(int position);
  }
}
