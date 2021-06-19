package com.example.customapplauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customapplauncher.databinding.ActivityCustomLauncherBinding
import com.example.customapplauncher.databinding.ListItemAppBinding

private const val TAG = "CustomLauncherActivity"

class CustomLauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appRecyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
    }

    private fun setupAdapter() {
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activitiesResolveInfo = packageManager.queryIntentActivities(startupIntent, 0)
        activitiesResolveInfo.sortWith { a1, a2 ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a1.loadLabel(packageManager).toString(),
                a2.loadLabel(packageManager).toString()
            )
        }
        binding.appRecyclerView.adapter = ActivitiesRecyclerViewAdapter(activitiesResolveInfo)
        Log.i(TAG, "Found activities: ${activitiesResolveInfo.size}")
    }

    private inner class ActivityViewHolder(private val binding: ListItemAppBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        private lateinit var resolveInfo: ResolveInfo

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(resolveInfo: ResolveInfo) {
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val appIcon = resolveInfo.loadIcon(packageManager)
            binding.appNameTextView.text = appName
            binding.appIconImageView.setImageDrawable(appIcon)
        }

        override fun onClick(view: View) {
            val activityInfo = resolveInfo.activityInfo
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val context = view.context
            context.startActivity(intent)
        }
    }

    private inner class ActivitiesRecyclerViewAdapter(val activitiesResolveInfo: List<ResolveInfo>) :
        RecyclerView.Adapter<ActivityViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
            val itemBinding =
                ListItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ActivityViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
            holder.bind(activitiesResolveInfo[position])
        }

        override fun getItemCount() = activitiesResolveInfo.size
    }
}