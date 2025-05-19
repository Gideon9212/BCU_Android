package com.g2.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.EnemyInfo
import com.g2.bcu.R
import com.g2.bcu.androidutil.GetStrings
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.SingleClick
import common.io.json.JsonEncoder
import common.util.stage.Revival

class StEnRevival(private val activity: Activity, private val r : RecyclerView, private val rev : Revival, private val mul : Float) : RecyclerView.Adapter<StEnRevival.ViewHolder>() {

    var expansions : Int = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_revival_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return expansions
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val p = viewHolder.bindingAdapterPosition

        var crev = rev
        for (j in 0 until p)
            crev = crev.rev

        val icon = crev.enemy?.get()?.icon?.img?.bimg()
        if(icon == null) {
            viewHolder.icon.setImageBitmap(StaticStore.empty(activity, 85f, 32f))
        } else
            viewHolder.icon.setImageBitmap(StaticStore.getResizeb(icon as Bitmap,activity, 85f, 32f))

        val ht = (crev.mhp * mul).toInt()
        val at = (crev.matk * mul).toInt()
        val txt = if(ht == at)
            "$ht%"
        else
            "$ht% / $at%"
        viewHolder.mulh.text = txt

        if (crev.bgm?.get() == null)
            viewHolder.bgm.visibility = View.GONE
        else
            viewHolder.bgm.text = crev.bgm.get().toString()

        if (crev.soul?.get() == null)
            viewHolder.soul.visibility = View.GONE
        else
            viewHolder.soul.text = crev.soul.get().toString()

        viewHolder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, EnemyInfo::class.java)
                intent.putExtra("Data", JsonEncoder.encode(crev.enemy).toString())
                intent.putExtra("Multiply", (crev.mhp * mul).toInt())
                intent.putExtra("AMultiply", (crev.matk * mul).toInt())
                activity.startActivity(intent)
            }
        })

        viewHolder.type.text = activity.getString(when (crev.boss.toInt()) {
            1 -> R.string.e_is_boss1
            2 -> R.string.e_is_boss2
            else -> R.string.e_is_boss0
        })

        if (crev.rev == null) {
            viewHolder.revRow.visibility = View.GONE
        } else {
            viewHolder.erev.setOnClickListener {
                if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                    return@setOnClickListener
                StaticStore.infoClick = SystemClock.elapsedRealtime()

                val oh = r.measuredHeight
                if (expansions == p + 1) {
                    expansions++
                    viewHolder.erev.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
                } else {
                    expansions = p + 1
                    viewHolder.erev.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
                }
                r.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = r.measuredHeight
                val anim = ValueAnimator.ofInt(oh, height)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = r.layoutParams
                    layout.height = `val`
                    r.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
            }
        }
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val icon = row.findViewById<ImageView>(R.id.strevlisticon)!!
        val info = row.findViewById<ImageButton>(R.id.strevlistinfo)!!
        val type = row.findViewById<TextView>(R.id.strevlistptype)!!
        val mulh = row.findViewById<TextView>(R.id.strevlistmultir)!!
        val bgm = row.findViewById<TextView>(R.id.strevlistbgm)!!
        val soul = row.findViewById<TextView>(R.id.strevlistsoul)!!
        val revRow = row.findViewById<TableRow>(R.id.nextRevRow)!!
        val erev = row.findViewById<ImageButton>(R.id.enemlistrev)!!
    }
}