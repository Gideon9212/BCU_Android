package com.g2.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.MaAnimEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.supports.DynamicListView
import com.g2.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaAnim
import common.util.anim.Part
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaAnimListAdapter(private val activity: MaAnimEditor, private val a : AnimCE) : StableArrayAdapter<Part>(activity, R.layout.maanim_list_layout, activity.getAnim(a).parts) {

    companion object {
        val mods = arrayOf("0 - Parent", "1 - ID", "2 - Sprite", "3 - Z-Order", "4 - Position X", "5 - Position Y", "6 - Pivot X", "7 - Pivot Y", "8 - Scale", "9 - Scale X",
            "10 - Scale Y", "11 - Angle", "12 - Opacity", "13 - H Flip", "14 - V Flip", "50 - Extend X", "51 - Random Extend X", "52 - Extend Y", "53 - Global Scale", "54 - Random Extend Y")
    }
    internal class ViewHolder(row: View) {
        val ipid: EditText = row.findViewById(R.id.maanim_pid)
        val imod: Spinner = row.findViewById(R.id.maanim_mod)
        val ilop: EditText = row.findViewById(R.id.maanim_lop)
        val iname: EditText = row.findViewById(R.id.maanim_name)
        val iplus: FloatingActionButton = row.findViewById(R.id.maanim_part_display)

        val ilist: DynamicListView = row.findViewById(R.id.maanim_part_list)
        val pAdd: Button = row.findViewById(R.id.mapart_ladd)
        val pRem: Button = row.findViewById(R.id.mapart_remove)

        fun setData(a : AnimCE, p : Part) {
            ipid.text = SpannableStringBuilder(p.ints[0].toString())
            val s = if (p.ints[1] < 15)
                p.ints[1]
            else
                p.ints[1] - 35
            imod.setSelection(s)
            ilop.text = SpannableStringBuilder(p.ints[2].toString())
            iname.text = SpannableStringBuilder(p.name)
            val hint = a.mamodel.strs0[p.ints[0]].ifBlank { a.imgcut.strs[a.mamodel.parts[p.ints[0]][2]] }
            iname.hint = hint
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.maanim_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }
        val manim = activity.getAnim(a)
        val ma = manim.parts[position]
        holder.imod.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.imod.adapter = ArrayAdapter(activity, R.layout.spinneradapter, mods)

        holder.setData(a, ma)
        setList(holder.ilist, ma)

        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ipid.doAfterTextChanged {
            if (!holder.ipid.hasFocus())
                return@doAfterTextChanged
            ma.ints[0] = MathUtil.clip(CommonStatic.parseIntN(holder.ipid.text.toString()), 0, a.mamodel.n - 1)
            ma.check(a)
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
        holder.ilop.doAfterTextChanged {
            if (!holder.ilop.hasFocus())
                return@doAfterTextChanged
            ma.ints[2] = max(CommonStatic.parseIntN(holder.ilop.text.toString()), -1)
            ma.check(a)
            activity.unSave(a,"maanim change $position loop count")
            voo.animationChanged()
        }
        holder.iname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            ma.name = holder.iname.text.toString()
            activity.unSave(a,"maanim change $position Name")
            false
        }
        holder.iplus.setOnClickListener {
            val vis = holder.ilist.visibility == View.GONE
            holder.ilist.visibility = if (vis)
                View.VISIBLE
            else
                View.GONE
            holder.pAdd.visibility = holder.ilist.visibility
            holder.pRem.visibility = holder.pAdd.visibility
        }
        holder.pAdd.setOnClickListener {
            addLine(ma)
            activity.unSave(a,"maanim add line")
            setList(holder.ilist, ma)
        }
        holder.pRem.setOnClickListener {
            val data: Array<Part?> = manim.parts
            data[position] = null
            manim.parts = arrayOfNulls(--manim.n)
            var ind = 0
            for (datum in data)
                if (datum != null)
                    manim.parts[ind++] = datum
            manim.validate()
            activity.unSave(a,"maanim remove part")
            remove(position)
            setList(holder.ilist, ma)
        }
        holder.ilist.setSwapListener { from, to ->
            val temp = ma.moves[from]
            ma.moves[from] = ma.moves[to]
            ma.moves[to] = temp
            activity.unSave(a,"maanim sort part")
        }

        return row
    }

    private fun setList(list : ListView, p : Part) {
        val h = StaticStore.dptopx(p.n * 60f, context)
        list.adapter = PartListAdapter(activity, a, p)
        list.layoutParams.height = h
    }

    private fun addLine(p: Part) {
        val data = p.moves
        p.moves = arrayOfNulls(++p.n)
        System.arraycopy(data, 0, p.moves, 0, data.size)
        val f = if (data.isNotEmpty())
            data[data.size-1][0]+1
        else
            0
        val newPart = intArrayOf(f, p.vd.toInt(), 0, 0)
        var si = 0
        for (i in p.n - 2 downTo 0) {
            if (p.moves[i][0] <= newPart[0]) {
                p.moves[(i + 1).also { si = it }] = newPart
                break
            }
            p.moves[i + 1] = p.moves[i]
        }
        if (si == 0)
            p.moves[0] = newPart
        p.validate()
        activity.getAnim(a).validate()
    }
}