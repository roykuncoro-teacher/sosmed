package model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.example.R

// --- DATA MODELS ---
data class Story(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = ""
)

data class Post(
    val postId: String = "",
    val username: String = "",
    val userAvatarUrl: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val likesCount: Long = 0
)

// --- STORY ADAPTER ---
class StoryAdapter(private val storyList: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivStoryAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvStoryUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.tvUsername.text = story.username

        Glide.with(holder.itemView.context)
            .load(story.avatarUrl)
            .placeholder(R.drawable.img_placeholder)
            .into(holder.ivAvatar)
    }

    override fun getItemCount(): Int = storyList.size
}

// --- POST ADAPTER ---
class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivPostAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvPostUsername)
        val ivPostImage: ImageView = view.findViewById(R.id.ivPostImage)
        val tvLikesCount: TextView = view.findViewById(R.id.tvLikesCount)
        val tvCaption: TextView = view.findViewById(R.id.tvCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvUsername.text = post.username
        holder.tvCaption.text = post.caption
        holder.tvLikesCount.text = "${post.likesCount} suka"

        Glide.with(holder.itemView.context)
            .load(post.userAvatarUrl)
            .placeholder(R.drawable.img_placeholder)
            .into(holder.ivAvatar)

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .placeholder(R.drawable.img_placeholder)
            .into(holder.ivPostImage)
    }

    override fun getItemCount(): Int = postList.size
}