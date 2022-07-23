package ai.deepar.deepar_example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private Context context;
    private ArrayList<FeedPost> feedPosts;

    FeedAdapter(Context context,ArrayList<FeedPost> feedPosts){
        this.context = context;
        this.feedPosts = feedPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.feed_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedPost feedPost = feedPosts.get(position);
        Glide.with(context).load(feedPost.url).into(holder.feedImage);
        holder.caption.setText(feedPost.caption);
        holder.profileName.setText(feedPost.user.getUserName());
        Glide.with(context).load(feedPost.user.getProfileUrl()).into(holder.profileImage);


    }

    @Override
    public int getItemCount() {
        return feedPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView feedImage;
        private ImageView profileImage;
        private TextView caption;
        private TextView profileName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            feedImage = itemView.findViewById(R.id.feed_image);
            profileImage = itemView.findViewById(R.id.feed_profile_image);
            caption = itemView.findViewById(R.id.caption);
            profileName = itemView.findViewById(R.id.profile_name);
        }
    }
}
