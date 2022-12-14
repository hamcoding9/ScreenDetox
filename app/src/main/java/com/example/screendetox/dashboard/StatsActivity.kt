package com.example.screendetox.dashboard

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.App
import com.example.screendetox.data.appNameMap
import com.example.screendetox.databinding.ActivityStatsBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

// 나의 사용 시간을 보는 Activity
class StatsActivity : AppCompatActivity() {
    // 뷰 바인딩
    private lateinit var binding: ActivityStatsBinding

    // recycler view
    private lateinit var adapter : AppsAdapter
    private lateinit var recyclerView : RecyclerView
    // App 정보를 담아서 adapter에 전달할 appList
    private lateinit var appsList : ArrayList<App>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.selectedItemId = R.id.stats
        // 네비게이션 탭바 터치에 따른 액티비티 이동
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.stats -> {
                    true
                }
                else -> false
            }
        }
        recyclerView = binding.appsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadTodayDate()
    }

    override fun onStart(){
       super.onStart()
       loadStatistics()
    }

    private fun loadTodayDate() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        binding.statsDateTv.text = formatted
    }

    private fun loadStatistics() {
        appsList = arrayListOf<App>()
        val usm = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        //val midnight : Long = (System.currentTimeMillis() / 86400000) * 86400000 - (9 * 3600000)
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 3600 * 24,
            System.currentTimeMillis())
        // 지난 24시간 동안 사용한 어플리케이션(사용 시간 > 0인 것만 filter)만 불러오기
        // totalTimeInForeground: Get the total time this package spent in the foreground, measured in milliseconds.
        appList = appList.stream().filter{ app: UsageStats -> app.totalTimeInForeground > 0}
            .collect(Collectors.toList())
        showAppUsage(appList)
    }

    private fun showAppUsage(appList: List<UsageStats>) {
        var appList = appList.sortedBy { it.totalTimeInForeground }
        // get total time of apps usage to calculate the usagePercentage for each app
        val totalTime = appList.stream().map { obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong {obj : Long -> obj }.sum()
        binding.statsUserDuration.text = getDurationBreakdown(totalTime)
        // fill the appsList
        for (usageStats in appList){
            try {
                val packageName = usageStats.packageName
                var icon = getDrawable(R.drawable.no_image)
                val packageNames = packageName.split("\\.").toTypedArray()
                var appName = packageNames[packageNames.size - 1].trim{ it <= ' '}
                if (isAppInfoAvailable(usageStats)){
                    val ai = applicationContext.packageManager.getApplicationInfo(packageName, 0)
                    icon = applicationContext.packageManager.getApplicationIcon(ai)
                    appName = applicationContext.packageManager.getApplicationLabel(ai).toString()
                }
                if (appNameMap.contains(appName)) {
                    appName = appNameMap[appName]!!
                    val iconId = this.resources.getIdentifier(appName.lowercase(), "drawable", this.packageName)
                    Log.i("StatsActivity", iconId.toString())
                    icon = ResourcesCompat.getDrawable(resources, iconId, null)
                }
                val usageDuration = getDurationBreakdown(usageStats.totalTimeInForeground)
                val usagePercentage = (usageStats.totalTimeInForeground * 100 / totalTime).toInt()
                val usageStatDTO = App(icon!!, appName, usagePercentage, usageDuration)
                appsList.add(usageStatDTO)
            } catch (e: PackageManager.NameNotFoundException){
                e.printStackTrace()
            }
        }
        // reverse the list to get most usage first
        appsList.reverse()
        adapter = AppsAdapter(appsList)
        recyclerView.adapter = adapter
    }

    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try{
            applicationContext.packageManager.getApplicationInfo(usageStats.packageName, 0)
            true
        }
        catch (e: PackageManager.NameNotFoundException){
            false
        }
    }

    private fun getDurationBreakdown(millis:Long): String{
        var millis = millis
        require(millis >= 0) { " Duration must be greater than zero! "}
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        return "$hours 시간 $minutes 분"
    }
}