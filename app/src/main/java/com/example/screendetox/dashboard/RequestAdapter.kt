package com.example.screendetox.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User

class RequestAdapter(private val userNameList: ArrayList<User>) :
    RecyclerView.Adapter<RequestAdapter.UserNameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserNameViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_request,
            parent, false
        )
        return UserNameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserNameViewHolder, position: Int) {
        val currentItem = userNameList[position]
        holder.user_name.text = currentItem.userName
    }

    override fun getItemCount(): Int {
        return userNameList.size
    }

    class UserNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user_name: TextView = itemView.findViewById(R.id.requestNameTv)
    }
}