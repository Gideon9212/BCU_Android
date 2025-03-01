package com.g2.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import com.g2.bcu.androidutil.GetStrings
import common.util.stage.Limit

class LimitRecycle(private val activity: Activity, val l: Limit?) : RecyclerView.Adapter<LimitRecycle.ViewHolder>() {
    private val limits: Array<String>
    private val collapseText: Array<String>
    private val collapsed: Array<Boolean>
    private var ind = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var limit: TextView = itemView.findViewById(R.id.limitst)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stg_limit_layout, viewGroup, false)

        val i = ind
        val holder = ViewHolder(row)
        holder.limit.setOnClickListener {_ ->
            collapsed[i] = !collapsed[i]
            val text = if (collapsed[i]) collapseText[i]+" ("+activity.getText(R.string.stg_info_expand)+")"
                else limits[i]
            holder.limit.text = text
        }
        ind++
        return holder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.limit.text = limits[viewHolder.bindingAdapterPosition]
    }

    override fun getItemCount(): Int {
        return limits.size
    }

    init {
        val s = GetStrings(activity)
        val lim = s.getLimit(l)
        limits = lim.values.toTypedArray()
        collapseText = lim.keys.toTypedArray()
        collapsed = Array(limits.size) { false }
    }
}