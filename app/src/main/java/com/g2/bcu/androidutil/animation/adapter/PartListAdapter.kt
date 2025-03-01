package com.g2.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.MaAnimEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaAnim
import common.util.anim.Part

class PartListAdapter(private val activity: MaAnimEditor, private val a : AnimCE, private val p : Part) : StableArrayAdapter<IntArray>(activity, R.layout.maanim_part_list_layout, p.moves) {

    companion object {
        val eases = arrayOf("0 - Linear", "1 - Instant", "2 - Exponential", "3 - Polynomial", "4 - Sinusoidal")
    }
    internal class ViewHolder(row: View) {
        val ifr: EditText = row.findViewById(R.id.mapart_frame)
        val idat: EditText = row.findViewById(R.id.mapart_mod)
        val iea: Spinner = row.findViewById(R.id.mapart_ease)
        val ipa: EditText = row.findViewById(R.id.mapart_param)
        val ire: FloatingActionButton = row.findViewById(R.id.mapart_delete)

        fun setData(ma : IntArray) {
            ifr.text = SpannableStringBuilder(ma[0].toString())
            idat.text = SpannableStringBuilder(ma[1].toString())
            iea.setSelection(ma[2])
            ipa.text = SpannableStringBuilder(ma[3].toString())

            ipa.visibility = if (ma[2] == 2 || ma[2] == 4)
                View.VISIBLE
            else
                View.GONE
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.maanim_part_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }
        val pa = p.moves[position]
        holder.iea.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.iea.adapter = ArrayAdapter(activity, R.layout.spinneradapter, eases)
        holder.setData(pa)

        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ifr.doAfterTextChanged {
            if (!holder.ifr.hasFocus())
                return@doAfterTextChanged
            pa[0] = CommonStatic.parseIntN(holder.ifr.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position frame")
            voo.animationChanged()
        }
        holder.idat.doAfterTextChanged {
            if (!holder.idat.hasFocus())
                return@doAfterTextChanged
            pa[1] = CommonStatic.parseIntN(holder.idat.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position effect")
            voo.animationChanged()
        }
        holder.iea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                if (pa[2] == position)
                    return
                pa[2] = position
                p.check(a)
                activity.unSave(a,"maanim change part move $position easing")
                holder.ipa.visibility = if (pa[2] == 2 || pa[2] == 4)
                    View.VISIBLE
                else
                    View.GONE
                voo.animationChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        holder.ipa.doAfterTextChanged {
            if (!holder.ipa.hasFocus())
                return@doAfterTextChanged
            pa[3] = CommonStatic.parseIntN(holder.ipa.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position effect")
            voo.animationChanged()
        }
        holder.ire.setOnClickListener {
            val ma : MaAnim = activity.getAnim(a)
            val data: Array<Part?> = ma.parts
            data[position] = null
            ma.parts = arrayOfNulls<Part>(--ma.n)
            var ind = 0
            for (datum in data)
                if (datum != null)
                    ma.parts[ind++] = datum
            ma.validate()
            activity.unSave(a,"maanim remove part")
            remove(position)
            voo.animationChanged()
        }
        return row
    }
}