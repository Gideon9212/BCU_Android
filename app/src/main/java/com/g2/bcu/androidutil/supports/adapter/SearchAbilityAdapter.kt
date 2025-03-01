package com.g2.bcu.androidutil.supports.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.system.files.VFile
import kotlin.math.ceil

class SearchAbilityAdapter(private val context: Context, private val abils: Array<IntArray>) : RecyclerView.Adapter<SearchAbilityAdapter.ViewHolder>() {
    private val up = ArrayList<Int>()

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val abil = if(isLandscape()) {
            arrayOf<CheckBox>(row.findViewById(R.id.abilicon1), row.findViewById(R.id.abilicon2), row.findViewById(R.id.abilicon3), row.findViewById(R.id.abilicon4), row.findViewById(R.id.abilicon5))
        } else
            arrayOf(row.findViewById(R.id.abilicon1), row.findViewById(R.id.abilicon2), row.findViewById(R.id.abilicon3))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.search_filter_ability_layout, parent, false)

        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return if(isLandscape()) {
            ceil(abils.size/5.0).toInt()
        } else {
            ceil(abils.size/3.0).toInt()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosit = if(isLandscape()) {
            5 * position
        } else {
            3 * position
        }

        val endReal = if(isLandscape()) {
            realPosit + 4
        } else {
            realPosit + 2
        }

        for(i in realPosit..endReal) {
            val ch = holder.abil[i-realPosit]

            if(i >= abils.size) {
                ch.visibility = View.INVISIBLE

                if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ch.compoundDrawablePadding = StaticStore.dptopx(16f, context)
                } else {
                    ch.compoundDrawablePadding = StaticStore.dptopx(8f, context)
                }
                continue
            }
            val checker = java.util.ArrayList<Int>()
            for (j in abils[i])
                checker.add(j)

            ch.isChecked = StaticStore.ability.contains(checker)
            ch.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getIcon(i), null)

            if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ch.compoundDrawablePadding = StaticStore.dptopx(16f, context)
            } else {
                ch.compoundDrawablePadding = StaticStore.dptopx(8f, context)
            }

            ch.setOnLongClickListener {
                StaticStore.showShortMessage(context, abils[i][0])
                true
            }

            ch.setOnCheckedChangeListener { _, check ->
                val abilval = ArrayList<Int>()

                for(j in abils[i]) {
                    abilval.add(j)
                }

                if(check)
                    StaticStore.ability.add(abilval)
                else
                    StaticStore.ability.remove(abilval)
            }


        }
    }

    fun updateList() {
        if(up.isEmpty()) {
            up.add(1)
        } else {
            up.clear()
        }
    }

    private fun getIcon(index: Int): Drawable? {
        if(index >= abils.size)
            return null

        if (abils[index].size > StaticStore.SF_PROC + 1) {
            return getDrawable(StaticStore.getMiscAbiIcon(abils[index]))
        } else if (abils[index][StaticStore.SF_TYPE] == 0) {
            for (i in 0..30)
                if ((1 shl i) and abils[index][StaticStore.SF_PROC] > 0)
                    return getDrawable(StaticStore.getAbiIcon(i))
            return getDrawable(StaticStore.empty(1, 1))
        }
        return getDrawable(StaticStore.getProcIcon(abils[index][StaticStore.SF_PROC]))
    }

    private fun getDrawable(b: Bitmap) : Drawable {
        return if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BitmapDrawable(context.resources, StaticStore.getResizeb(b, context, 40f))
        } else {
            BitmapDrawable(context.resources, StaticStore.getResizeb(b, context, 32f))
        }
    }

    private fun isLandscape() : Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}