package com.example.nexthire

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.adapter.JobAdapter
import com.example.nexthire.model.Job
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyApplicationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private lateinit var emptyContainer: View
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentUser: String
    private val gson = Gson()

    private val appliedJobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_applications, container, false)

        recyclerView = view.findViewById(R.id.applied_jobs_recycler)
        emptyContainer = view.findViewById(R.id.empty_state_container)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUser = prefs.getString("email", "") ?: ""

        sharedPrefs = requireContext().getSharedPreferences("applied_jobs_$currentUser", Context.MODE_PRIVATE)

        adapter = JobAdapter(
            context = requireContext(),
            jobs = appliedJobs,
            showWithdrawButton = true,
            currentUser = currentUser
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadAppliedJobs()

        return view
    }

    private fun loadAppliedJobs() {
        appliedJobs.clear()
        val json = sharedPrefs.getString("jobs", null)

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Job>>() {}.type
            val jobs = gson.fromJson<List<Job>>(json, type)
            appliedJobs.addAll(jobs)
        }

        adapter.notifyDataSetChanged()
        toggleEmptyState(appliedJobs.isEmpty())
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
