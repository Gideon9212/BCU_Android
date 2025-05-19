package com.g2.bcu.androidutil.pack.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.PackCreation
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.pack.PackData.PackDesc
import common.pack.PackData.UserPack
import common.pack.SortedPackSet
import common.pack.UserProfile

class PackParentAdapter(private val ctx : PackCreation, private val desc : PackDesc, val parent : Boolean) : RecyclerView.Adapter<PackParentAdapter.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val icon: ImageView = row.findViewById(R.id.spinnericon)
        val txt: TextView = row.findViewById(R.id.spinnertext)
    }

    override fun getItemCount(): Int {
        return if (parent)
            desc.dependency.size
        else getParentablePacks().size
    }

    override fun onCreateViewHolder(group: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.list_layout_text_icon, group, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val pos = holder.bindingAdapterPosition
        val parpack = if (parent) UserProfile.getUserPack(desc.dependency[pos]) else getParentablePacks()[pos]

        if (parpack.icon?.img?.bimg() != null)
            holder.icon.setImageBitmap(parpack.icon.img.bimg() as Bitmap)
        else
            holder.icon.setImageBitmap(StaticStore.empty(64, 64))
        holder.txt.text = parpack.toString()
    }

    fun getParentablePacks() : SortedPackSet<UserPack> {
        val valid = SortedPackSet<UserPack>()
        for (pack in UserProfile.getUserPacks()) {
            if (pack.sid == desc.id || desc.dependency.contains(pack.sid) || pack.desc.dependency.contains(desc.id))
                continue
            valid.add(pack)
        }
        return valid
    }
}