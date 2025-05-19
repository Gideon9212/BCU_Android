package com.g2.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.content.Intent
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.g2.bcu.BattleSimulation
import com.g2.bcu.R
import com.g2.bcu.ReplayList
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.util.stage.Replay


class ReplayListAdapter(private val activity: ReplayList, private val replays : ArrayList<Replay>) : ArrayAdapter<Replay>(activity, R.layout.replay_list_layout, replays) {

    private class ViewHolder(row: View) {
        var name: WatcherEditText = row.findViewById(R.id.rplyname)
        var stgName: TextView = row.findViewById(R.id.rplystgname)
        var battle: Button = row.findViewById(R.id.rplystart)
        var expand: ImageButton = row.findViewById(R.id.rplyexpand)
        val moreinfo: TableRow = row.findViewById(R.id.rplymoreinfo)
        val delete: Button = row.findViewById(R.id.rplydelete)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.replay_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }
        val replay = replays[position]

        holder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

            StaticStore.infoClick = SystemClock.elapsedRealtime()

            if (holder.moreinfo.height == 0) {
                holder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val height = holder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(0, height)

                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.moreinfo.layoutParams
                    layout.height = `val`
                    holder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
            } else {
                holder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = holder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.moreinfo.layoutParams
                    layout.height = `val`
                    holder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
            }
        })
        holder.name.text = SpannableStringBuilder(replay.toString())
        holder.name.setWatcher {
            if (!holder.name.hasFocus() || replay.rl.id == holder.name.text!!.toString())
                return@setWatcher
            replay.rename(holder.name.text!!.toString())
        }
        if (replay.rl.pack == ResourceLocation.LOCAL || UserProfile.getUserPack(replay.rl.pack)?.editable == true) {
            holder.delete.setOnClickListener {
                val f = CommonStatic.ctx.getWorkspaceFile(replay.rl.path + ".replay")
                if (f.exists() && f.delete()) {
                    Replay.getMap().remove(replay.rl.id)
                    replays.remove(replay)
                    notifyDataSetChanged()
                }
            }
        } else
            holder.delete.visibility = View.GONE

        val stage = replay.st.safeGet()
        if (stage == null) {
            holder.battle.visibility = View.GONE
            val t = "${replay.st} (Corrupted)"
            holder.stgName.text = t
        } else {
            val t = "${stage.cont}: $stage"
            holder.stgName.text = t
            holder.battle.setOnClickListener {
                val intent = Intent(activity, BattleSimulation::class.java)

                intent.putExtra("Data", JsonEncoder.encode(replay.st).toString())
                intent.putExtra("star", replay.star)
                intent.putExtra("item", replay.cfg)
                intent.putExtra("replay", JsonEncoder.encode(replay.rl).toString())

                activity.startActivity(intent)
            }
        }
        return row
    }
}