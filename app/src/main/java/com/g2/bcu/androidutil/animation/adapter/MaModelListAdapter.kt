package com.g2.bcu.androidutil.animation.adapter

import android.app.Dialog
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.MaModelEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaModel
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaModelListAdapter(private val activity: MaModelEditor, private val a : AnimCE) : StableArrayAdapter<IntArray>(activity, R.layout.mamodel_list_layout, a.mamodel.parts) {

    companion object {
        val glows = arrayOf("-1 - Substract", "0 - None", "1 - Add", "2 - Multiply", "3 - Screen")
    }
    internal class ViewHolder(row: View) {
        val iid: Button = row.findViewById(R.id.mamodel_id)
        val ipar: EditText = row.findViewById(R.id.mamodel_par)
        val ispr: EditText = row.findViewById(R.id.mamodel_spr)
        val iz: EditText = row.findViewById(R.id.mamodel_z)
        val ix: EditText = row.findViewById(R.id.mamodel_x)
        val iy: EditText = row.findViewById(R.id.mamodel_y)
        val ipx: EditText = row.findViewById(R.id.mamodel_px)
        val ipy: EditText = row.findViewById(R.id.mamodel_py)
        val isx: EditText = row.findViewById(R.id.mamodel_sx)
        val isy: EditText = row.findViewById(R.id.mamodel_sy)
        val irot: EditText = row.findViewById(R.id.mamodel_rot)
        val iopa: EditText = row.findViewById(R.id.mamodel_opa)
        val iglw: Spinner = row.findViewById(R.id.mamodel_glw)
        val iname: EditText = row.findViewById(R.id.mamodel_name)
        val del: FloatingActionButton = row.findViewById(R.id.mamodel_part_delete)

        fun setData(ic : IntArray) {
            ipar.text = SpannableStringBuilder(ic[0].toString())
            ispr.text = SpannableStringBuilder(ic[2].toString())
            iz.text = SpannableStringBuilder(ic[3].toString())
            ix.text = SpannableStringBuilder(ic[4].toString())
            iy.text = SpannableStringBuilder(ic[5].toString())
            ipx.text = SpannableStringBuilder(ic[6].toString())
            ipy.text = SpannableStringBuilder(ic[7].toString())
            isx.text = SpannableStringBuilder(ic[8].toString())
            isy.text = SpannableStringBuilder(ic[9].toString())
            irot.text = SpannableStringBuilder(ic[10].toString())
            iopa.text = SpannableStringBuilder(ic[11].toString())
            iglw.setSelection(ic[12] + 1)
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.mamodel_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val mo = a.mamodel.parts[position]
        holder.iglw.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.iglw.adapter = ArrayAdapter(activity, R.layout.spinneradapter, glows)
        holder.iid.text = position.toString()

        holder.setData(mo)
        holder.iname.text = SpannableStringBuilder(a.mamodel.strs0[position])
        holder.iname.hint = a.imgcut.strs[mo[2]]
        holder.iname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            a.mamodel.strs0[position] = holder.iname.text.toString()
            false
        }

        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ipar.doAfterTextChanged {
            if (!holder.ipar.hasFocus())
                return@doAfterTextChanged
            mo[0] = MathUtil.clip(CommonStatic.parseIntN(holder.ipar.text.toString()), -1, a.mamodel.n - 1)
            a.mamodel.check(a)
            activity.unSave(a,"mamodel change $position Parent")
            voo.animationChanged()
        }
        holder.ispr.doAfterTextChanged {
            if (!holder.ispr.hasFocus())
                return@doAfterTextChanged
            mo[2] = MathUtil.clip(CommonStatic.parseIntN(holder.ispr.text.toString()), 0, a.imgcut.n - 1)
            activity.unSave(a,"mamodel change $position Sprite")
            voo.animationChanged()
        }
        holder.iz.doAfterTextChanged {
            if (!holder.iz.hasFocus())
                return@doAfterTextChanged
            mo[3] = CommonStatic.parseIntN(holder.iz.text.toString())
            activity.unSave(a,"mamodel change $position Z-Order")
            voo.animationChanged()
        }
        holder.ix.doAfterTextChanged {
            if (!holder.ix.hasFocus())
                return@doAfterTextChanged
            mo[4] = CommonStatic.parseIntN(holder.ix.text.toString())
            activity.unSave(a,"mamodel change $position x")
            voo.animationChanged()
        }
        holder.iy.doAfterTextChanged {
            if (!holder.iy.hasFocus())
                return@doAfterTextChanged
            mo[5] = CommonStatic.parseIntN(holder.iy.text.toString())
            activity.unSave(a,"mamodel change $position y")
            voo.animationChanged()
        }
        holder.ipx.doAfterTextChanged {
            if (!holder.ipx.hasFocus())
                return@doAfterTextChanged
            mo[6] = CommonStatic.parseIntN(holder.ipx.text.toString())
            activity.unSave(a,"mamodel change $position pivot x")
            voo.animationChanged()
        }
        holder.ipy.doAfterTextChanged {
            if (!holder.ipy.hasFocus())
                return@doAfterTextChanged
            mo[7] = CommonStatic.parseIntN(holder.ipy.text.toString())
            activity.unSave(a,"mamodel change $position pivot y")
            voo.animationChanged()
        }
        holder.isx.doAfterTextChanged {
            if (!holder.isx.hasFocus())
                return@doAfterTextChanged
            mo[8] = CommonStatic.parseIntN(holder.isx.text.toString())
            activity.unSave(a,"mamodel change $position scale x")
            voo.animationChanged()
        }
        holder.isy.doAfterTextChanged {
            if (!holder.isy.hasFocus())
                return@doAfterTextChanged
            mo[9] = CommonStatic.parseIntN(holder.isy.text.toString())
            activity.unSave(a,"mamodel change $position scale y")
            voo.animationChanged()
        }
        holder.irot.doAfterTextChanged {
            if (!holder.irot.hasFocus())
                return@doAfterTextChanged
            mo[10] = CommonStatic.parseIntN(holder.irot.text.toString())
            activity.unSave(a,"mamodel change $position angle")
            voo.animationChanged()
        }
        holder.iopa.doAfterTextChanged {
            if (!holder.iopa.hasFocus())
                return@doAfterTextChanged
            mo[11] = max(0, CommonStatic.parseIntN(holder.iopa.text.toString()))
            activity.unSave(a,"mamodel change $position opacity")
            voo.animationChanged()
        }
        holder.iglw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                if (mo[12] == position - 1)
                    return
                mo[12] = position - 1
                activity.unSave(a,"mamodel change $position glow")
                voo.animationChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        holder.iid.setOnClickListener {
            voo.anim.sele = if (voo.anim.sele == position) -1 else position
            voo.animationChanged()
        }
        holder.del.setOnClickListener {
            val parts = StringBuilder()
            for (i in a.mamodel.parts.indices)
                if (a.mamodel.parts[i][0] == position)
                    parts.append("MaModel ").append(activity.getString(R.string.def_part)).append(" $i (").append(a.mamodel.strs0[i]).append(")\n")
            for (i in a.anims.indices)
                for (part in a.anims[i].parts)
                    if (part.ints[0] == position) {
                        parts.append("MaAnim: ${a.types[i]}").append("\n")
                        break
                    }
            if (parts.isBlank())
                removePart(voo, position)
            else {
                val delPop = Dialog(context)
                delPop.setContentView(R.layout.animation_part_delete_confirm)
                delPop.setCancelable(true)

                val parList = delPop.findViewById<TextView>(R.id.usedPartList)
                parList.text = parts.toString()
                val del = delPop.findViewById<Button>(R.id.part_delete_tree)
                del.setOnClickListener {
                    removePart(voo, position)
                    delPop.dismiss()
                }
                val cancel = delPop.findViewById<Button>(R.id.part_nodelete)
                cancel.setOnClickListener { delPop.dismiss() }
                if (!activity.isDestroyed && !activity.isFinishing)
                    delPop.show()
            }
        }
        holder.del.visibility = if (a.mamodel.n == 1 || position == 0)
            View.INVISIBLE
        else
            View.VISIBLE
        return row
    }

    private fun removePart(voo : AnimationEditView, position : Int) {
        val mm : MaModel = a.mamodel

        val bs = BooleanArray(mm.n)
        bs[position] = true
        val total = 1 + mm.getChild(bs)
        mm.clearAnim(bs, a.anims)
        val inds = IntArray(mm.n)
        val move = IntArray(mm.n - total)
        var j = 0
        for (i in 0 until mm.n)
            if (!bs[i]) {
                move[j] = i
                inds[i] = j
                j++
            } else
                remove(j)
        a.reorderModel(inds)
        mm.n = move.size
        mm.reorder(move)
        activity.unSave(a,"mamodel remove part $position")
        voo.animationChanged()
    }
}