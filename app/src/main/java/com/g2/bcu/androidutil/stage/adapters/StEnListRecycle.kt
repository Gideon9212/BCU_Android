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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.EnemyInfo
import com.g2.bcu.R
import com.g2.bcu.androidutil.GetStrings
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.SingleClick
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.Enemy

class StEnListRecycle(private val activity: Activity, private val st: Stage, private var multi: Int, private var frse: Boolean) : RecyclerView.Adapter<StEnListRecycle.ViewHolder>() {

    init {
        if ((StaticStore.infoOpened?.size ?: 0) < st.data.datas.size) {
            StaticStore.infoOpened = BooleanArray(st.data.datas.size) {
                false
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val s = GetStrings(activity)
        val data = reverse(st.data.datas)

        val infos = StaticStore.infoOpened ?: return

        viewHolder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

            StaticStore.infoClick = SystemClock.elapsedRealtime()

            if (viewHolder.moreinfo.height == 0) {
                viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val height = viewHolder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(0, height)

                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.moreinfo.layoutParams
                    layout.height = `val`
                    viewHolder.moreinfo.layoutParams = layout
                }

                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()

                viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))

                infos[viewHolder.bindingAdapterPosition] = true
            } else {
                viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = viewHolder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.moreinfo.layoutParams
                    layout.height = `val`
                    viewHolder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
                infos[viewHolder.bindingAdapterPosition] = false
            }
        })

        if (infos[viewHolder.bindingAdapterPosition]) {
            viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val layout = viewHolder.moreinfo.layoutParams
            layout.height = viewHolder.moreinfo.measuredHeight
            viewHolder.moreinfo.layoutParams = layout
            viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
        }

        val id = data[viewHolder.bindingAdapterPosition]?.enemy ?: UserProfile.getBCData().enemies[0].id

        val em = Identifier.get(id) ?: return

        if(em !is Enemy)
            return

        val icon = em.anim?.edi?.img?.bimg()

        if(icon == null) {
            viewHolder.icon.setImageBitmap(StaticStore.empty(activity, 85f, 32f))
        } else {
            viewHolder.icon.setImageBitmap(StaticStore.getResizeb(icon as Bitmap,activity, 85f, 32f))
        }

        viewHolder.number.text = s.getNumber(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())


        viewHolder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, EnemyInfo::class.java)
                intent.putExtra("Data", JsonEncoder.encode(em.id).toString())
                intent.putExtra("Multiply", ((data[viewHolder.bindingAdapterPosition]?.multiple?.toFloat() ?: 0f) * multi.toFloat() / 100f).toInt())
                intent.putExtra("AMultiply", ((data[viewHolder.bindingAdapterPosition]?.mult_atk?.toFloat() ?: 0f) * multi.toFloat() / 100f).toInt())
                activity.startActivity(intent)
            }
        })

        viewHolder.multiply.text = s.getMultiply(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), multi)

        viewHolder.bh.text = s.getBaseHealth(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())

        if ((data[viewHolder.bindingAdapterPosition]?.boss ?: -1) == 0)
            viewHolder.isboss.text = activity.getString(R.string.unit_info_false)
        else
            viewHolder.isboss.text = activity.getString(R.string.unit_info_true)

        viewHolder.layer.text = s.getLayer(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())

        viewHolder.startb.setOnClickListener {
            if (viewHolder.start.text.toString().endsWith("f"))
                viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), false)
            else
                viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), true)
        }

        viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), frse)

        viewHolder.respawnb.setOnClickListener {
            if (viewHolder.respawn.text.toString().endsWith("f"))
                viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), false)
            else
                viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), true)
        }

        viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), frse)

        viewHolder.killcount.text = (data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).kill_count.toString()

        val build = StringBuilder((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doorchance.toString()).append("%")
        if ((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doorchance > 0) {
            build.append(": ").append((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doordis_0).append("%")
            if ((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doordis_0 != (data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doordis_1)
                build.append(" ~ ").append((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).doordis_1).append("%")
        } else {
            viewHolder.edor.visibility = View.GONE
            viewHolder.edoor.visibility = View.GONE
        }
        viewHolder.edoor.text = build.toString()

        if ((data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).rev != null) {
            viewHolder.erevc.layoutManager = LinearLayoutManager(activity)
            val adp = StEnRevival(activity, viewHolder.erevc, (data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).rev, multi.toFloat() / 100f)
            viewHolder.erevc.adapter = adp
            viewHolder.erev.setOnClickListener(View.OnClickListener {
                if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                    return@OnClickListener

                StaticStore.infoClick = SystemClock.elapsedRealtime()

                if (adp.expansions == 0) {
                    adp.expansions++
                    viewHolder.erevc.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val height = viewHolder.erevc.measuredHeight
                    val anim = ValueAnimator.ofInt(0, height)

                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val layout = viewHolder.erevc.layoutParams
                        layout.height = `val`
                        viewHolder.erevc.layoutParams = layout
                    }
                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()

                    viewHolder.erev.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
                } else {
                    viewHolder.erevc.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    val height = viewHolder.erevc.measuredHeight
                    val anim = ValueAnimator.ofInt(height, 0)
                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val layout = viewHolder.erevc.layoutParams
                        layout.height = `val`
                        viewHolder.erevc.layoutParams = layout
                    }
                    anim.doOnEnd {
                        adp.expansions = 0
                    }
                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()
                    viewHolder.erev.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
                }
            })
        } else {
            viewHolder.erv.visibility = View.GONE
            viewHolder.erev.visibility = View.GONE
            viewHolder.erevc.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return st.data.datas.size
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val expand = row.findViewById<ImageButton>(R.id.stgenlistexp)!!
        val icon = row.findViewById<ImageView>(R.id.stgenlisticon)!!
        val multiply = row.findViewById<TextView>(R.id.stgenlistmultir)!!
        val number = row.findViewById<TextView>(R.id.stgenlistnumr)!!
        val info = row.findViewById<ImageButton>(R.id.stgenlistinfo)!!
        val bh = row.findViewById<TextView>(R.id.enemlistbhr)!!
        val isboss = row.findViewById<TextView>(R.id.enemlistibr)!!
        val layer = row.findViewById<TextView>(R.id.enemlistlayr)!!
        val startb = row.findViewById<Button>(R.id.enemlistst)!!
        val start = row.findViewById<TextView>(R.id.enemliststr)!!
        val respawnb = row.findViewById<Button>(R.id.enemlistres)!!
        val respawn = row.findViewById<TextView>(R.id.enemlistresr)!!
        val moreinfo = row.findViewById<TableLayout>(R.id.stgenlistmi)!!
        val killcount = row.findViewById<TextView>(R.id.enemlistkilcr)!!
        val edor = row.findViewById<TextView>(R.id.enemlistedoor)!!
        val edoor = row.findViewById<TextView>(R.id.enemlistevrdr)!!
        val erv = row.findViewById<TextView>(R.id.enemlistrv)!!
        val erev = row.findViewById<ImageButton>(R.id.enemlistrev)!!
        val erevc = row.findViewById<RecyclerView>(R.id.enemlistrevcont)!!
    }

    private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line?> {
        val result = arrayOfNulls<SCDef.Line>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }
}