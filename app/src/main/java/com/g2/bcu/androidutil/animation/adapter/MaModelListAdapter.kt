package com.g2.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.MaModelEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaModel
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaModelListAdapter(private val ctx: MaModelEditor, private val a : AnimCE) : RecyclerView.Adapter<MaModelListAdapter.ViewHolder>() {

    companion object {
        val glows = arrayOf("-1 - Substract", "0 - None", "1 - Add", "2 - Multiply", "3 - Screen")
    }
    var mv = false

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val iid: Button = row.findViewById(R.id.mamodel_id)
        val ipar: WatcherEditText = row.findViewById(R.id.mamodel_par)
        val ispr: WatcherEditText = row.findViewById(R.id.mamodel_spr)
        val iz: WatcherEditText = row.findViewById(R.id.mamodel_z)
        val ix: WatcherEditText = row.findViewById(R.id.mamodel_x)
        val iy: WatcherEditText = row.findViewById(R.id.mamodel_y)
        val ipx: WatcherEditText = row.findViewById(R.id.mamodel_px)
        val ipy: WatcherEditText = row.findViewById(R.id.mamodel_py)
        val isx: WatcherEditText = row.findViewById(R.id.mamodel_sx)
        val isy: WatcherEditText = row.findViewById(R.id.mamodel_sy)
        val irot: WatcherEditText = row.findViewById(R.id.mamodel_rot)
        val iopa: WatcherEditText = row.findViewById(R.id.mamodel_opa)
        val iglw: Spinner = row.findViewById(R.id.mamodel_glw)
        val iname: WatcherEditText = row.findViewById(R.id.mamodel_name)

        fun setData(ic : IntArray) {
            mv = true
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
            mv = false
        }

        fun setID(id : Int, voo : AnimationEditView) {
            iid.text = id.toString()
            iid.setOnClickListener {
                voo.anim.sele = if (voo.anim.sele == id) -1 else id
                voo.animationChanged()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.mamodel_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val pos = holder.bindingAdapterPosition
        val mo = a.mamodel.parts[pos]
        holder.iglw.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.iglw.adapter = ArrayAdapter(ctx, R.layout.spinneradapter, glows)

        holder.setData(mo)
        holder.iname.text = SpannableStringBuilder(a.mamodel.strs0[pos])
        holder.iname.hint = a.imgcut.strs[mo[2]]
        holder.iname.setWatcher {
            if (mv || !holder.iname.hasFocus() || a.mamodel.strs0[pos] == holder.iname.text!!.toString())
                return@setWatcher
            a.mamodel.strs0[pos] = holder.iname.text!!.toString()
        }

        val voo = ctx.findViewById<AnimationEditView>(R.id.animationView)
        holder.setID(pos, voo)
        holder.ipar.setWatcher {
            if (mv || !holder.ipar.hasFocus())
                return@setWatcher
            mo[0] = MathUtil.clip(CommonStatic.parseIntN(holder.ipar.text!!.toString()), -1, a.mamodel.n - 1)
            a.mamodel.check(a)
            ctx.unSave(a,"mamodel change $pos Parent")
            voo.animationChanged()
        }
        holder.ispr.setWatcher {
            if (mv || !holder.ispr.hasFocus())
                return@setWatcher
            mo[2] = MathUtil.clip(CommonStatic.parseIntN(holder.ispr.text!!.toString()),0,a.imgcut.n - 1)
            ctx.unSave(a,"mamodel change $pos Sprite")
            voo.animationChanged()
        }
        holder.iz.setWatcher {
            if (mv || !holder.iz.hasFocus())
                return@setWatcher
            mo[3] = CommonStatic.parseIntN(holder.iz.text!!.toString())
            ctx.unSave(a,"mamodel change $pos Z-Order")
            voo.animationChanged()
        }
        holder.ix.setWatcher {
            if (mv || !holder.ix.hasFocus())
                return@setWatcher
            mo[4] = CommonStatic.parseIntN(holder.ix.text!!.toString())
            ctx.unSave(a,"mamodel change $pos x")
            voo.animationChanged()
        }
        holder.iy.setWatcher {
            if (mv || !holder.iy.hasFocus())
                return@setWatcher
            mo[5] = CommonStatic.parseIntN(holder.iy.text!!.toString())
            ctx.unSave(a,"mamodel change $pos y")
            voo.animationChanged()
        }
        holder.ipx.setWatcher {
            if (mv || !holder.ipx.hasFocus())
                return@setWatcher
            mo[6] = CommonStatic.parseIntN(holder.ipx.text!!.toString())
            ctx.unSave(a,"mamodel change $pos pivot x")
            voo.animationChanged()
        }
        holder.ipy.setWatcher {
            if (mv || !holder.ipy.hasFocus())
                return@setWatcher
            mo[7] = CommonStatic.parseIntN(holder.ipy.text!!.toString())
            ctx.unSave(a,"mamodel change $pos pivot y")
            voo.animationChanged()
        }
        holder.isx.setWatcher {
            if (mv || !holder.isx.hasFocus())
                return@setWatcher
            mo[8] = CommonStatic.parseIntN(holder.isx.text!!.toString())
            ctx.unSave(a,"mamodel change $pos scale x")
            voo.animationChanged()
        }
        holder.isy.setWatcher {
            if (mv || !holder.isy.hasFocus())
                return@setWatcher
            mo[9] = CommonStatic.parseIntN(holder.isy.text!!.toString())
            ctx.unSave(a,"mamodel change $pos scale y")
            voo.animationChanged()
        }
        holder.irot.setWatcher {
            if (mv || !holder.irot.hasFocus())
                return@setWatcher
            mo[10] = CommonStatic.parseIntN(holder.irot.text!!.toString())
            ctx.unSave(a,"mamodel change $pos angle")
            voo.animationChanged()
        }
        holder.iopa.setWatcher {
            if (mv || !holder.iopa.hasFocus())
                return@setWatcher
            mo[11] = max(0, CommonStatic.parseIntN(holder.iopa.text!!.toString()))
            ctx.unSave(a,"mamodel change $pos opacity")
            voo.animationChanged()
        }
        holder.iglw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                if (mo[12] == position - 1)
                    return
                mo[12] = position - 1
                ctx.unSave(a,"mamodel change $position glow")
                voo.animationChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    fun removePart(voo : AnimationEditView, position : Int) {
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
                notifyItemRemoved(j)
        a.reorderModel(inds)
        mm.n = move.size
        mm.reorder(move)
        ctx.unSave(a,"mamodel remove part $position")
        voo.animationChanged()
    }

    override fun getItemCount(): Int {
        return a.mamodel.n
    }
}