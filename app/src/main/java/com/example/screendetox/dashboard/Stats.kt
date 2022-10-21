package com.example.screendetox.dashboard

import android.app.usage.UsageStats
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.App
import java.util.*
import java.util.concurrent.TimeUnit

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
        // adapter로 전달
        recyclerView = view.findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = AppsAdapter(appList)
        recyclerView.adapter = adapter
    }

    private fun showAppUsage(mySortedMap: HashMap<String, UsageStats>) {
        appList = arrayListOf<App>()
        val usageStatsList: List<UsageStats> = java.util.ArrayList(mySortedMap.values)
        // sort the application by time spent in foreground
        Collections.sort(usageStatsList) {
                z1: UsageStats, z2: UsageStats -> java.lang.Long.compare(
            z1.totalTimeInForeground,
            z2.totalTimeInForeground
        )
        }
        // get total time of apps usage to calculate the usagePercentage for each app
        val totalTime = usageStatsList.stream().map { obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong {obj : Long -> obj }.sum()

        // fill the appsList
        for (usageStats in usageStatsList){
            try {
                val packageName = usageStats.packageName
                var icon = ContextCompat.getDrawable(requireContext(),R.drawable.no_image)
                val packageNames = packageName.split("\\.").toTypedArray()
                var appName = packageNames[packageNames.size - 1].trim{ it <= ' '}
                if (isAppInfoAvailable(usageStats)){
                    val ai = requireContext().applicationContext.packageManager.getApplicationInfo(packageName, 0)
                    icon = requireContext().applicationContext.packageManager.getApplicationIcon(ai)
                    appName = requireContext().applicationContext.packageManager.getApplicationLabel(ai).toString()
                }
                val usageDuration = getDurationBreakdown(usageStats.totalTimeInForeground)
                val usagePercentage = (usageStats.totalTimeInForeground * 100 / totalTime).toInt()
                val usageStatDTO = App(icon!!, appName, usagePercentage, usageDuration)
                appList.add(usageStatDTO)
            } catch (e: PackageManager.NameNotFoundException){
                e.printStackTrace()
            }
        }
        // reverse the list to get most usage first
        appList.reverse()
    }

    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try {
            requireContext().applicationContext.packageManager.getApplicationInfo(
                usageStats.packageName,
                0
            )
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
    private fun getDurationBreakdown(millis: Long): String {
        var millis = millis
        require(millis >= 0) { " Duration must be greater than zero! "}
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= java.util.concurrent.TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return "$hours h $minutes m $seconds s"
    }