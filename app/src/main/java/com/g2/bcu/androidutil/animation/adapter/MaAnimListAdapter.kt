package com.g2.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.MaAnimEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.Part
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaAnimListAdapter(private val activity: MaAnimEditor, private val a : AnimCE) : RecyclerView.Adapter<MaAnimListAdapter.ViewHolder>() {

    companion object {
        val mods = arrayOf("0 - Parent", "1 - ID", "2 - Sprite", "3 - Z-Order", "4 - Position X", "5 - Position Y", "6 - Pivot X", "7 - Pivot Y", "8 - Scale", "9 - Scale X",
            "10 - Scale Y", "11 - Angle", "12 - Opacity", "13 - H Flip", "14 - V Flip", "50 - Extend X", "51 - Random Extend X", "52 - Extend Y", "53 - Global Scale", "54 - Random Extend Y")
    }
    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val ipid: WatcherEditText = row.findViewById(R.id.maanim_pid)
        val imod: Spinner = row.findViewById(R.id.maanim_mod)
        val ilop: WatcherEditText = row.findViewById(R.id.maanim_lop)
        val iname: WatcherEditText = row.findViewById(R.id.maanim_name)
        val iplus: FloatingActionButton = row.findViewById(R.id.maanim_part_display)

        val ilist: RecyclerView = row.findViewById(R.id.maanim_part_list)
        val pAdd: Button = row.findViewById(R.id.mapart_ladd)

        fun setData(a : AnimCE, p : Part) {
            ipid.text = SpannableStringBuilder(p.ints[0].toString())
            val s = if (p.ints[1] < 15)
                p.ints[1]
            else
                p.ints[1] - 35
            imod.setSelection(s)
            ilop.text = SpannableStringBuilder(p.ints[2].toString())
            iname.text = SpannableStringBuilder(p.name)
            iname.hint = a.mamodel.strs0[p.ints[0]].ifBlank { a.imgcut.strs[a.mamodel.parts[p.ints[0]][2]] }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.maanim_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return activity.getAnim(a).n
    }

    override fun onBindViewHolder(holder: ViewHolder, indx: Int) {
        val position = holder.bindingAdapterPosition
        val manim = activity.getAnim(a)
        val ma = manim.parts[position]
        holder.imod.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.imod.adapter = ArrayAdapter(activity, R.layout.spinneradapter, mods)

        holder.setData(a, ma)
        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ipid.setWatcher {
            if (!holder.ipid.hasFocus())
                return@setWatcher
            ma.ints[0] = MathUtil.clip(CommonStatic.parseIntN(holder.ipid.text!!.toString()), 0, a.mamodel.n - 1)
            ma.check(a)
            holder.iname.hint = a.mamodel.strs0[ma.ints[0]].ifBlank { a.imgcut.strs[a.mamodel.parts[ma.ints[0]][2]] }
            activity.unSave(a,"maanim change $position ID")
            voo.animationChanged()
        }
        holder.imod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                val ind = if (position >= 15)
                    position + 35
                else
                    position
                if (ma.ints[1] == ind)
                    return
                ma.ints[1] = ind
                ma.check(a)
                activity.unSave(a,"maanim change $position Modification")
                voo.animationChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        holder.ilop.setWatcher {
            if (!holder.ilop.hasFocus())
                return@setWatcher
            ma.ints[2] = max(CommonStatic.parseIntN(holder.ilop.text!!.toString()), -1)
            ma.check(a)
            activity.unSave(a,"maanim change $position loop count")
            activity.animationChanged(voo)
        }
        holder.iname.setWatcher {
            if (!holder.iname.hasFocus() || ma.name == holder.iname.text!!.toString())
                return@setWatcher
            ma.name = holder.iname.text!!.toString()
        }
        holder.iplus.setOnClickListener {
            val vis = holder.ilist.visibility == View.GONE
            holder.ilist.visibility = if (vis)
                View.VISIBLE
            else
                View.GONE
            holder.pAdd.visibility = holder.ilist.visibility
        }
        holder.ilist.layoutManager = LinearLayoutManager(activity)
        val adp = PartListAdapter(activity, a, ma)
        holder.ilist.adapter = adp
        val touch = ItemTouchHelper(object: ItemTouchHelper.Callback() {
            var moved : Boolean = false

            override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
            }

            override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return current.itemViewType == target.itemViewType
            }

            override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                val from = src.bindingAdapterPosition
                val to = dest.bindingAdapterPosition

                val temp = ma.moves[from]
                ma.moves[from] = ma.moves[to]
                ma.moves[to] = temp
                moved = true
                voo.animationChanged()
                adp.notifyItemMoved(from, to)
                return false
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                // Action finished
                if (moved)
                    activity.unSave(a,"maanim sort part")
                moved = false
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                val pos = holder.bindingAdapterPosition
                val data: Array<Part?> = manim.parts
                data[pos] = null
                manim.parts = arrayOfNulls(--manim.n)
                var ind = 0
                for (datum in data)
                    if (datum != null)
                        manim.parts[ind++] = datum
                manim.validate()
                activity.unSave(a,"maanim remove part")
                adp.notifyItemRemoved(pos)
            }
        })
        touch.attachToRecyclerView(holder.ilist)
        holder.pAdd.setOnClickListener {
            val data = ma.moves
            ma.moves = arrayOfNulls(++ma.n)
            System.arraycopy(data, 0, ma.moves, 0, data.size)
            val f = if (data.isNotEmpty())
                data[data.size-1][0]+1
            else
                0
            val newPart = intArrayOf(f, ma.vd.toInt(), 0, 0)
            var si = 0
            for (i in ma.n - 2 downTo 0) {
                if (ma.moves[i][0] <= newPart[0]) {
                    ma.moves[(i + 1).also { si = it }] = newPart
                    break
                }
                ma.moves[i + 1] = ma.moves[i]
            }
            if (si == 0)
                ma.moves[0] = newPart
            ma.validate()
            activity.getAnim(a).validate()

            activity.unSave(a,"maanim add line")
            adp.notifyItemInserted(ma.n-1)
        }
    }
}