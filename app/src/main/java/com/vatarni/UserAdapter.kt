package com.vatarni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vatarni.databinding.RowChatBinding

class UserAdapter(private val context: Chat, private val userList: ArrayList<UserData>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowChatBinding = RowChatBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowChatBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.userName.text = user.getName()
        Glide.with(context).load(user.getProfilePictureUrl())
            .error(R.drawable.user)
            .into(holder.binding.userProfilePicture)
    }

}
