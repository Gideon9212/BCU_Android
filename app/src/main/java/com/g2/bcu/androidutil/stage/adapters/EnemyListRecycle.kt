package com.g2.bcu.androidutil.stage.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.util.stage.Stage

open class EnemyListRecycle : RecyclerView.Adapter<EnemyListRecycle.ViewHolder> {
    private val activity: Activity
    private val st: Stage
    private var multi = 0

    constructor(activity: Activity, st: Stage) {
        this.activity = activity
        this.st = st
    }

    constructor(activity: Activity, st: Stage, multi: Int) {
        this.activity = activity
        this.st = st
        this.multi = multi
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var listView: RecyclerView = row.findViewById(R.id.stginfoenemlist)
        var frse: Button = row.findViewById(R.id.stginfoenfrse)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        viewHolder.listView.layoutManager = LinearLayoutManager(activity)

        ViewCompat.setNestedScrollingEnabled(viewHolder.listView, false)

        val listAdapter = StEnListRecycle(activity, st, multi, shared.getBoolean("frame", true))

        viewHolder.listView.adapter = listAdapter

        if (!shared.getBoolean("frame", true))
            viewHolder.frse.text = activity.getString(R.string.config_seconds)

        viewHolder.frse.setOnClickListener {
            if (viewHolder.frse.text.toString() == activity.getString(R.string.config_frames)) {
                val listAdapter1 = StEnListRecycle(activity, st, multi, false)
                viewHolder.listView.adapter = listAdapter1
                viewHolder.frse.text = activity.getString(R.string.config_seconds)
            } else {
                val listAdapter2 = StEnListRecycle(activity, st, multi, true)
                viewHolder.listView.adapter = listAdapter2
                viewHolder.frse.text = activity.getString(R.string.config_frames)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }
}