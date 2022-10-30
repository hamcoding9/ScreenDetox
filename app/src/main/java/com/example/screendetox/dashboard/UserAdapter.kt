package com.example.screendetox.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ItemUserBinding

class UserAdapter : ListAdapter<User, UserAdapter.ViewHolder>(diffUtil){
    inner class ViewHolder(var binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: User){
            binding.nameTv.text = item.userId
            binding.durationTv.text = item.totalTime

            itemView.setOnClickListener {
                Intent(itemView.context, FriendDetailActivity::class.java).apply {
                    putExtra("userID", item.userId)
                    putExtra("userTotalTime", item.totalTime)
                    putExtra("userMostUsedApp", item.mostUsedApp)
                }.run { itemView.context.startActivity(this) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<User>(){
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}

/*class UserAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_user,
            parent, false
        )
        return UserItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.userName.text = currentItem.userId
        holder.durationTime.text = currentItem.totalTime

        // UserList에서 Item을 누르면 FriendDetailActivity 실행
        holder.itemView.setOnClickListener {
            Intent(holder.itemView.context, FriendDetailActivity::class.java).apply {
                putExtra("userID", currentItem.userId)
                putExtra("userTotalTime", currentItem.totalTime)
                putExtra("userMostUsedApp", currentItem.mostUsedApp)
            }.run { holder.itemView.context.startActivity(this) }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.nameTv)
        val durationTime: TextView = itemView.findViewById(R.id.durationTv)
    }
}*/

