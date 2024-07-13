package com.vatarni

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StatusAdapter(
    private val context: Context,
    private val userStatuses: ArrayList<UserStatus>
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val lastUpdated: TextView = view.findViewById(R.id.lastUpdated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val userStatus = userStatuses[position]
        holder.name.text = userStatus.getName()
        holder.lastUpdated.text = userStatus.getLastUpdated().toString()

         Glide.with(context)
             .load(userStatus)
             .error(R.drawable.user)
             .into(holder.profileImage)
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }
}
