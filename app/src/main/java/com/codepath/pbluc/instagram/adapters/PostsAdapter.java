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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.pbluc.instagram.R;
import com.codepath.pbluc.instagram.models.Post;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";
    private static final int CAPTION_MAX_LENGTH = 125;

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "View holder onClick success");

                Date createdAt = post.getCreatedAt();
                String timeAgo = Post.calculateTimeAgo(createdAt);
                holder.tvCreatedAt.setText(timeAgo);
                if (holder.tvCreatedAt.getVisibility() == View.VISIBLE) {
                    holder.tvCreatedAt.setVisibility(View.GONE);

                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(CAPTION_MAX_LENGTH);
                    holder.tvCaption.setFilters(fArray);
                    holder.tvCaption.setText(post.getCaption());
                } else {
                    holder.tvCreatedAt.setVisibility(View.VISIBLE);

                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(post.getCaption().length());
                    holder.tvCaption.setFilters(fArray);
                    holder.tvCaption.setText(post.getCaption());
                }


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

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private TextView tvCaption;
        private TextView tvCreatedAt;
        private ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            tvCaption.setText(post.getCaption());
            tvUsername.setText(post.getUser().getUsername());
            ParseFile image = post.getImage();
            if(image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }

        }
    }
}