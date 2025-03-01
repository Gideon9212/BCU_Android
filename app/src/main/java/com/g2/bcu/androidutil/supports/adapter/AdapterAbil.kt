package com.g2.bcu.androidutil.supports.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore

class AdapterAbil(private val ability: List<String>, private val procs: List<String>, private val abilicon: List<Int>, private val context: Context) : RecyclerView.Adapter<AdapterAbil.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.ability_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (viewHolder.bindingAdapterPosition < ability.size) {
            viewHolder.abiltext.text = ability[viewHolder.bindingAdapterPosition]

            val icon = StaticStore.getAbiIcon(abilicon[viewHolder.bindingAdapterPosition])

            val resized: Bitmap = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                StaticStore.getResizeb(icon, context, 28f)
            } else {
                StaticStore.getResizeb(icon, context, 24f)
            }
            viewHolder.abilicon.setImageBitmap(resized)
        } else {
            val location = viewHolder.bindingAdapterPosition - abilicon.size

            val info = procs[location].split("\\")

            if(info.size != 2) {
                Log.e("AdapterAbil","Invalid proc name "+procs[location])
                return
            }

            viewHolder.abiltext.text = info[1]

            val id = info[0].toInt()

            val resized: Bitmap
            val icon = StaticStore.getProcIcon(id)

            resized = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                StaticStore.getResizeb(icon, context, 28f)
            } else {
                StaticStore.getResizeb(icon, context, 24f)
            }
            viewHolder.abilicon.setImageBitmap(resized)
        }
    }

    override fun getItemCount(): Int {
        return abilicon.size + procs.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var abilicon: ImageView = itemView.findViewById(R.id.abilicon)
        var abiltext: TextView = itemView.findViewById(R.id.ability)
    }
}