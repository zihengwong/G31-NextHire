package com.example.nexthire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.adapter.JobAdapter
import com.example.nexthire.model.Job

class HomeFragment : Fragment() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var currentUser: String

    private val jobList = listOf(
        Job("1", "Software Engineer", "Google", "California", "Full-time", "$110k - $130k", "Develop scalable backend services.", "3+ years"),
        Job("2", "UI/UX Designer", "Apple", "California", "Contract", "$90k - $110k", "Design intuitive user experiences.", "2+ years"),
        Job("3", "Backend Developer", "Amazon", "Seattle", "Full-time", "$120k - $140k", "Work on AWS microservices.", "4+ years"),
        Job("4", "Android Developer", "Meta", "New York", "Remote", "$115k - $135k", "Develop Android apps for billions.", "3+ years"),
        Job("5", "Data Scientist", "Netflix", "Los Angeles", "Full-time", "$130k - $160k", "Analyze viewer data patterns.", "5+ years")
    )


    private val filteredList = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUser = prefs.getString("email", "") ?: ""

        searchInput = view.findViewById(R.id.search_input)
        recyclerView = view.findViewById(R.id.job_recycler_view)

        filteredList.addAll(jobList)

        jobAdapter = JobAdapter(
            context = requireContext(),
            jobs = filteredList,
            showWithdrawButton = false,
            currentUser = currentUser
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jobAdapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                filteredList.clear()
                filteredList.addAll(jobList.filter {
                    it.title.lowercase().contains(query) ||
                            it.company.lowercase().contains(query) ||
                            it.location.lowercase().contains(query)
                })
                jobAdapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }
}
