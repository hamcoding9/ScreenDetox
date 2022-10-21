package com.example.screendetox.dashboard

import android.app.usage.UsageStats
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.App

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Stats : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: AppsAdapter
    private lateinit var recyclerView: RecyclerView
    // apps 정보를 담아서 adapter에 전달할 appList
    private lateinit var appList : ArrayList<App>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // 데이터 받아오기
        val mySortedMap: HashMap<String, UsageStats> = arguments?.getSerializable("mySortedMap") as HashMap<String, UsageStats>
        // 데이터 화면에서 보여주기
        showAppUsage(mySortedMap)
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
    }


    private fun showAppUsage(mySortedMap: HashMap<String, UsageStats>) {
        
    }

}