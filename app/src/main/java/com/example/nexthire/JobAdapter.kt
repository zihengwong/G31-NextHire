package com.example.nexthire.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.JobDetailsActivity
import com.example.nexthire.R
import com.example.nexthire.model.Job
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JobAdapter(
    private val context: Context,
    private val jobs: MutableList<Job>,
    private val showWithdrawButton: Boolean = false,
    private val currentUser: String
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.job_title)
        private val company: TextView = itemView.findViewById(R.id.job_company)
        private val location: TextView = itemView.findViewById(R.id.job_location)
        private val withdrawBtn: Button = itemView.findViewById(R.id.withdraw_button)

        fun bind(job: Job) {
            title.text = job.title
            company.text = job.company
            location.text = job.location

            // 📦 Withdraw Logic
            if (showWithdrawButton) {
                withdrawBtn.visibility = View.VISIBLE
                withdrawBtn.setOnClickListener {
                    withdrawApplication(job, adapterPosition)
                }
            } else {
                withdrawBtn.visibility = View.GONE
            }

            // 🔗 Open Job Details
            itemView.setOnClickListener {
                val intent = Intent(context, JobDetailsActivity::class.java).apply {
                    putExtra("title", job.title)
                    putExtra("company", job.company)
                    putExtra("location", job.location)
                    putExtra("salary", job.salary)
                    putExtra("jobType", job.type)
                    putExtra("description", job.description)
                    putExtra("experience", job.experience)
                }
                context.startActivity(intent)
            }
        }

        private fun withdrawApplication(job: Job, position: Int) {
            val prefs = context.getSharedPreferences("applied_jobs_$currentUser", Context.MODE_PRIVATE)
            val json = prefs.getString("jobs", null)
            val type = object : TypeToken<MutableList<Job>>() {}.type
            val list = Gson().fromJson<MutableList<Job>>(json, type) ?: mutableListOf()

            val updatedList = list.filterNot {
                it.title == job.title && it.company == job.company
            }.toMutableList()

            prefs.edit().putString("jobs", Gson().toJson(updatedList)).apply()
            Toast.makeText(context, "Withdrawn from ${job.title}", Toast.LENGTH_SHORT).show()

            // ✅ Update UI
            jobs.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_item, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount(): Int = jobs.size
}
