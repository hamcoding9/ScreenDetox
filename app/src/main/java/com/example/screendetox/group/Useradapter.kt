package com.example.screendetox.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ItemUserBinding

// UserAdapter Class

class UserAdapter(private val userList : ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>()
{
    inner class UserItemViewHolder(private val binding: ItemUserBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(userModel: User) {
            binding.nameTv.text = userModel.userName
            binding.durationTv.text = userModel.usageTotaltime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        return UserItemViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}