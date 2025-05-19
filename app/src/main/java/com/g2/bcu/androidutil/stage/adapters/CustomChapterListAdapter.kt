package com.g2.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.LimitEditor
import com.g2.bcu.PackStageManager
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.PackData.UserPack
import common.util.stage.Limit
import common.util.stage.Limit.PackLimit
import common.util.stage.StageMap


class CustomChapterListAdapter(private val pack : UserPack, private val ctx: Activity) : RecyclerView.Adapter<CustomChapterListAdapter.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val chName: WatcherEditText = row.findViewById(R.id.cuschapter_name)
        val starRow: TableRow = row.findViewById(R.id.pk_chapterStarRow)
        val info: TableLayout = row.findViewById(R.id.cuschapter_info)
        val expand: ImageButton = row.findViewById(R.id.cuschapter_expand)

        val cost: WatcherEditText = row.findViewById(R.id.pk_chapterCost)
        val stageEdit: Button = row.findViewById(R.id.pk_stageEdit)
        val limitAdd: Button = row.findViewById(R.id.pk_limitAdd)
        val limitList: ListView = row.findViewById(R.id.pk_limitList)

        fun extend(ctx : Activity, size : Int) {
            limitList.layoutParams.height = StaticStore.dptopx(57f, ctx) * size

            info.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val height = info.measuredHeight
            val anim = ValueAnimator.ofInt(0, height)
            anim.addUpdateListener { animation ->
                val `val` = animation.animatedValue as Int
                val layout = info.layoutParams
                layout.height = `val`
                info.layoutParams = layout
            }
            anim.duration = 300
            anim.interpolator = DecelerateInterpolator()
            anim.start()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.cus_chapter_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return pack.mc.maps.size()
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val pos = holder.bindingAdapterPosition
        val subchapter = pack.mc.maps[pos]

        holder.chName.setWatcher {
            if (!holder.chName.hasFocus() || subchapter.names.toString() == holder.chName.text!!.toString())
                return@setWatcher
            subchapter.names.put(holder.chName.text!!.toString())
        }
        holder.chName.text = SpannableStringBuilder(subchapter.names.toString())

        holder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

            StaticStore.infoClick = SystemClock.elapsedRealtime()

            if (holder.info.height == 0) {
                holder.extend(ctx, subchapter.lim.size)
                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_more_black_24dp))
            } else {
                holder.info.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = holder.info.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.info.layoutParams
                    layout.height = `val`
                    holder.info.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_less_black_24dp))
            }
        })

        for (s in 0..3) {
            val stDif = holder.starRow.getChildAt(s) as WatcherEditText
            stDif.visibility = if (s > subchapter.stars.size)
                View.GONE else View.VISIBLE

            stDif.hint = "${s+1}★: ${if (s < subchapter.stars.size) subchapter.stars[s] else "N/A"}%"
            stDif.setWatcher {
                val star = CommonStatic.parseIntN(stDif.text!!.toString().ifBlank { stDif.hint.toString().substring(4) })
                if (!stDif.hasFocus() || star < 0)
                    return@setWatcher

                if (s == subchapter.stars.size-1 && star == 0) {
                    subchapter.stars = subchapter.stars.copyOf(subchapter.stars.size - 1)
                    if (s < 3)
                        holder.starRow.getChildAt(s+1).visibility = View.GONE
                } else if (star > 0) {
                    if (s == subchapter.stars.size) {
                        subchapter.stars = subchapter.stars.copyOf(subchapter.stars.size + 1)
                        if (s < 3)
                            holder.starRow.getChildAt(s+1).visibility = View.VISIBLE
                    }
                    subchapter.stars[s] = star
                }
            }
        }
        holder.cost.hint = "${ctx.getString(R.string.cuschapter_cost)}: ${subchapter.price+1}"
        holder.cost.setWatcher {
            val cost = CommonStatic.parseIntN(holder.cost.text!!.toString().ifBlank { holder.cost.hint.toString() })
            if (!holder.cost.hasFocus() || cost < 0)
                return@setWatcher
            subchapter.price = cost-1
        }

        holder.stageEdit.setOnClickListener {
            val intent = Intent(ctx, PackStageManager::class.java)
            intent.putExtra("map", JsonEncoder.encode(subchapter.id).toString())

            ctx.startActivity(intent)
        }
        val adp = LimitListAdapter(ctx, subchapter) { c, size -> holder.extend(c, size) }
        holder.limitList.adapter = adp

        holder.limitAdd.setOnClickListener {
            val lim = PackLimit()
            subchapter.lim.add(lim)
            adp.notifyDataSetChanged()
            holder.extend(ctx, subchapter.lim.size)
        }
    }

    private class LimitListAdapter(private val ctx : Activity, private val map : StageMap, private val exp : (ctx : Activity, size : Int) -> Unit) : ArrayAdapter<Limit>(ctx, R.layout.cus_chapter_info_layout, map.lim) {
        class ViewHolder(row: View) {
            var lim: TextView = row.findViewById(R.id.cuslim_name)
            var edit: Button = row.findViewById(R.id.cuslim_edit)
            var delete: FloatingActionButton = row.findViewById(R.id.cuslim_delete)
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val holder: ViewHolder
            val row: View

            if (view == null) {
                val inf = LayoutInflater.from(ctx)
                row = inf.inflate(R.layout.cus_limit_list_layout, parent, false)
                holder = ViewHolder(row)
                row.tag = holder
            } else {
                row = view
                holder = row.tag as ViewHolder
            }
            val lim = map.lim[position]
            val t = "${ctx.getString(R.string.stg_info_limit)} ${position+1}"
            holder.lim.text = t

            holder.edit.setOnClickListener {
                LimitEditor.lim = lim
                val intent = Intent(ctx, LimitEditor::class.java)
                intent.putExtra("name", "$map $t: $lim")

                ctx.startActivity(intent)
            }

            holder.delete.setOnClickListener {
                map.lim.remove(lim)
                notifyDataSetChanged()
                exp(ctx, map.lim.size)
            }
            return row
        }
    }
}