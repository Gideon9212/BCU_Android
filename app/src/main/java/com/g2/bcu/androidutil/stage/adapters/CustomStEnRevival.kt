package com.g2.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.EnemyInfo
import com.g2.bcu.EnemyList
import com.g2.bcu.MusicList
import com.g2.bcu.PackStageEnemyManager
import com.g2.bcu.R
import com.g2.bcu.androidutil.GetStrings
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.SingleClick
import com.g2.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.UserProfile
import common.util.stage.Revival
import common.util.stage.SCDef.Line
import common.util.stage.Stage

class CustomStEnRevival(private val ctx: PackStageEnemyManager, private val par : CustomStEnList, private val r : RecyclerView, private val st : Stage, private val l: Line, private val pos : Int) : RecyclerView.Adapter<CustomStEnRevival.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val icon = row.findViewById<ImageView>(R.id.cusstrevlisticon)!!
        val info = row.findViewById<ImageButton>(R.id.cusstrevlistinfo)!!
        val type = row.findViewById<Spinner>(R.id.cusstrevlistptype)!!
        val mulh = row.findViewById<WatcherEditText>(R.id.cusstrevlistmultir)!!
        val bgm = row.findViewById<Button>(R.id.cusstrevlistbgm)!!
        val soul = row.findViewById<Spinner>(R.id.cusstrevlistsoul)!!
        val erev = row.findViewById<ImageButton>(R.id.cusenemlistrev)!!
        val eremv = row.findViewById<Button>(R.id.cusenemlistremv)!!
    }

    var expansions : Int = 0

    override fun onCreateViewHolder(group: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.cus_stage_enemy_revival_layout, group, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return expansions
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val p = holder.bindingAdapterPosition

        var crev = l.rev
        for (j in 0 until p)
            crev = crev.rev

        val icon = (crev.enemy?.get()?.icon?.img?.bimg() ?: StaticStore.empty(ctx, 85f, 32f)) as Bitmap
        holder.icon.setImageBitmap(StaticStore.getResizeb(icon,ctx, 85f, 32f))
        holder.icon.setOnClickListener {
            ctx.revi[0] = st.data.datas.size - 1 - pos
            ctx.revi[1] = p
            ctx.notif = { notifyItemChanged(p) }

            val intent = Intent(ctx, EnemyList::class.java)
            intent.putExtra("mode", EnemyList.Mode.SELECTION.name)
            intent.putExtra("pack", st.mc.sid)

            ctx.resultLauncher.launch(intent)
        }
        holder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(ctx, EnemyInfo::class.java)
                intent.putExtra("Data", JsonEncoder.encode(crev.enemy).toString())
                intent.putExtra("Multiply", crev.mhp)
                intent.putExtra("AMultiply", crev.matk)
                ctx.startActivity(intent)
            }
        })

        holder.soul.setPopupBackgroundResource(R.drawable.spinner_popup)
        val lis = ArrayList(UserProfile.getBCData().souls.list)
        val pk = UserProfile.getUserPack(st.mc.sid)
        lis.addAll(pk.souls)
        for (d in pk.desc.dependency)
            lis.addAll(UserProfile.getUserPack(d).souls)
        val ar = Array(lis.size + 1) { if (it == 0) "None" else lis[it - 1].toString()}
        holder.soul.adapter = ArrayAdapter(ctx, R.layout.spinneradapter, ar)
        holder.soul.setSelection(if (crev.soul == null) 0 else lis.indexOf(crev.soul.get()))
        holder.soul.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(par: AdapterView<*>, v: View?, position: Int, id: Long) { crev.soul = if (position == 0) null else lis[position - 1].id }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        holder.mulh.setWatcher {
            val ints = CommonStatic.parseIntsN(holder.mulh.text!!.toString())
            if (ints.isEmpty())
                return@setWatcher
            val atk = if (ints.size >= 2) ints[1] else ints[0]
            if (ints[0] > 0)
                crev.mhp = ints[0]
            if (atk > 0)
                crev.matk = atk
        }
        val txt = if(crev.mhp == crev.matk)
            "${crev.mhp}%"
        else
            "${crev.mhp}% / ${crev.matk}%"
        holder.mulh.text = SpannableStringBuilder(txt)

        holder.bgm.setOnClickListener {
            ctx.revi[0] = st.data.datas.size - 1 - pos
            ctx.revi[1] = p
            ctx.notif = { holder.bgm.text = "${ctx.getString(R.string.stg_info_music)}: ${st.mus0?.get()}" }

            val intent = Intent(ctx, MusicList::class.java)
            intent.putExtra("pack", st.mc.sid)
            ctx.resultLauncher.launch(intent)
        }
        holder.bgm.text = "${ctx.getString(R.string.stg_info_music)}: ${st.mus0?.get()}"

        holder.type.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.type.adapter = ArrayAdapter(ctx, R.layout.spinneradapter, GetStrings(ctx).getStrings(R.string.e_is_boss0, R.string.e_is_boss1, R.string.e_is_boss2))
        holder.type.setSelection(crev.boss.toInt())
        holder.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(par: AdapterView<*>, v: View?, position: Int, id: Long) { crev.boss = position.toByte() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        setRevivalButton(holder, crev, p)

        holder.eremv.setOnClickListener {
            expansions = p
            if (p == 0) {
                l.rev = null
                par.notifyItemChanged(pos)
            } else {
                r.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = r.measuredHeight
                val anim = ValueAnimator.ofInt(r.height, height)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = r.layoutParams
                    layout.height = `val`
                    r.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()

                var drev = l.rev
                while (drev.rev != crev)
                    drev = drev.rev
                drev.rev = null
                notifyItemChanged(p - 1)
            }
        }
    }

    private fun setRevivalButton(holder : ViewHolder, crev : Revival, p : Int, click : Boolean = false) {
        if (crev.rev == null) {
            holder.erev.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_add_black_24dp))
            holder.erev.setOnClickListener {
                ctx.revi[0] = st.data.datas.size - 1 - pos
                ctx.revi[1] = p + 1
                ctx.notif = {
                    expansions++
                    setRevivalButton(holder, crev, p, true)
                }
                val intent = Intent(ctx, EnemyList::class.java)
                intent.putExtra("mode", EnemyList.Mode.SELECTION.name)
                intent.putExtra("pack", st.mc.sid)

                ctx.resultLauncher.launch(intent)
            }
        } else {
            holder.erev.setOnClickListener {
                if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                    return@setOnClickListener
                StaticStore.infoClick = SystemClock.elapsedRealtime()

                val oh = r.measuredHeight
                if (expansions == p + 1) {
                    expansions++
                    holder.erev.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_more_black_24dp))
                } else {
                    expansions = p + 1
                    holder.erev.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_less_black_24dp))
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
            if (click)
                holder.erev.performClick()
            else
                holder.erev.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_less_black_24dp))
        }
    }
}