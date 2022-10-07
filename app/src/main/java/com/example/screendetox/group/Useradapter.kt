package com.example.screendetox.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User

class UserAdapter(private val userList : ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user,
            parent, false)
        return UserItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.userName.text = currentItem.userId
        holder.durationTime.text = currentItem.totalTime
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userName : TextView = itemView.findViewById(R.id.nameTv)
        val durationTime : TextView = itemView.findViewById(R.id.durationTv)
    }
}