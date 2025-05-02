package com.example.nexthire

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.adapter.JobAdapter
import com.example.nexthire.model.Job
import com.google.gson.Gson

class SavedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private lateinit var emptyContainer: View
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentUser: String
    private val gson = Gson()
    private val savedJobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        recyclerView = view.findViewById(R.id.saved_jobs_recycler)
        emptyContainer = view.findViewById(R.id.empty_container)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUser = prefs.getString("email", "") ?: ""

        sharedPrefs = requireContext().getSharedPreferences("saved_jobs_$currentUser", Context.MODE_PRIVATE)

        adapter = JobAdapter(
            context = requireContext(),
            jobs = savedJobs,
            showWithdrawButton = false,
            currentUser = currentUser
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadSavedJobs()

        return view
    }

    private fun loadSavedJobs() {
        savedJobs.clear()

        // 🧠 Get all saved job entries (each is a key-value string)
        for ((_, value) in sharedPrefs.all) {
            try {
                val job = gson.fromJson(value.toString(), Job::class.java)
                savedJobs.add(job)
            } catch (_: Exception) { }
        }

        adapter.notifyDataSetChanged()
        toggleEmptyState(savedJobs.isEmpty())
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
