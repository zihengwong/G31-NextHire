package com.example.nexthire.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.JobDetailsActivity
import com.example.nexthire.R
import com.example.nexthire.data.JobRepository
import com.example.nexthire.model.NotificationItem

class NotificationAdapter(private val notifications: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.notificationMessage)
        val timeText: TextView = view.findViewById(R.id.notificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.messageText.text = item.message
        holder.timeText.text = item.time

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val job = JobRepository.jobs.find { job -> job.id == item.jobId }

            if (job != null) {
                val intent = Intent(context, JobDetailsActivity::class.java).apply {
                    putExtra("title", job.title)
                    putExtra("company", job.company)
                    putExtra("location", job.location)
                    putExtra("salary", job.salary)
                    putExtra("type", job.type)
                    putExtra("experience", job.experience)
                    putExtra("description", job.description)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "❗ Matching job not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
