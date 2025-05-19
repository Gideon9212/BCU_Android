package com.g2.bcu.androidutil.animation.adapter

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Environment
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.g2.bcu.AnimationManagement
import com.g2.bcu.ImgCutEditor
import com.g2.bcu.MaAnimEditor
import com.g2.bcu.MaModelEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.fakeandroid.FIBM
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Source.BasePath
import common.system.VImg
import common.util.anim.AnimCE
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AnimationListAdapter(private val activity: AnimationManagement, private val anims: ArrayList<AnimCE>) : ArrayAdapter<AnimCE>(activity, R.layout.animation_list_layout, anims) {

    private class ViewHolder(row: View) {
        val name: TextInputEditText = row.findViewById(R.id.anim_name)
        val icon: ImageView = row.findViewById(R.id.anim_uni)
        val more: FloatingActionButton = row.findViewById(R.id.anim_icon_set_button)!!
        val imgc: Button = row.findViewById(R.id.anim_imgcut_btn)
        val mamo: Button = row.findViewById(R.id.anim_mamodel_btn)
        val maan: Button = row.findViewById(R.id.anim_maanim_btn)
    }
    private var ici : Int = -1
    var dialog = AlertDialog.Builder(context)

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.animation_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val a = anims[position]
        holder.name.text = SpannableStringBuilder(a.toString())
        holder.name.setOnEditorActionListener { _, _, _ ->
            if (a.id.id == holder.name.text!!.toString())
                return@setOnEditorActionListener false
            a.check()
            a.renameTo(holder.name.text!!.toString())
            false
        }

        holder.icon.setOnClickListener {
            setIcon(holder.icon, a)
        }
        setIcon(holder.icon, a)

        val popup = PopupMenu(context, holder.more)
        val menu = popup.menu
        popup.menuInflater.inflate(R.menu.animation_list_option_menu, menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.anim_btn_set_edi -> {
                    activity.getImage(fun(img : Bitmap) {
                        if (img.height != 32 || img.width != 85) {
                            val s = activity.getString(R.string.image_import_wrongsize).replace("_ow","${img.width}").replace("_oh","${img.height}")
                            StaticStore.showShortMessage(context, s.replace("_nw","85").replace("_nh","32"))
                            return
                        }
                        a.edi = VImg(FIBM(img))
                        a.saveIcon()
                    })
                }
                R.id.anim_btn_set_uni -> {
                    activity.getImage(fun(img : Bitmap) {
                        if (img.height != 85 || img.width != 110) {
                            val s = activity.getString(R.string.image_import_wrongsize).replace("_ow","${img.width}").replace("_oh","${img.height}")
                            StaticStore.showShortMessage(context, s.replace("_nw","110").replace("_nh","85"))
                            return
                        }
                        a.uni = VImg(FIBM(img))
                        a.saveUni()
                    })
                }
                R.id.anim_btn_set_prev -> {
                    activity.getImage(fun(img : Bitmap) {
                        if (img.height != 72 || img.width != 72) {
                            val s = activity.getString(R.string.image_import_wrongsize).replace("_ow","${img.width}").replace("_oh","${img.height}")
                            StaticStore.showShortMessage(context, s.replace("_nw","72").replace("_nh","72"))
                            return
                        }
                        a.setPreview(VImg(FIBM(img)))
                        a.savePreview()
                    })
                }
                R.id.anim_btn_delete -> {
                    dialog.setTitle(R.string.anim_manager_delete)
                    dialog.setMessage(R.string.anim_manager_delwarn)

                    dialog.setPositiveButton(R.string.remove) { _, _ ->
                        anims.remove(a)
                        a.delete()
                        notifyDataSetChanged()
                        StaticStore.showShortMessage(context, R.string.anim_manager_deleted)
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                    dialog.setNegativeButton(R.string.main_file_cancel) {_, _ ->
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                    StaticStore.fixOrientation(activity)
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        dialog.show()
                    }
                }
                R.id.anim_btn_export -> {
                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()+"/bcu/exports/anim_${a.id.id}"
                    val g = File(path)
                    if(!g.exists())
                        g.mkdirs()

                    val data = CommonStatic.ctx.getWorkspaceFile(a.id.path)
                    for (f in data.listFiles()!!) {
                        val dest = File(g,f.name)
                        if (!dest.exists())
                            dest.createNewFile()

                        val fos = FileOutputStream(dest)
                        val ins = FileInputStream(f)

                        val b = ByteArray(65536)
                        var len: Int
                        while(ins.read(b).also { l -> len = l } != -1)
                            fos.write(b, 0, len)

                        ins.close()
                        fos.close()
                    }
                    StaticStore.showShortMessage(activity, activity.getString(R.string.file_extract_semi).replace("_", g.name))
                }
            }
            false
        }
        menu.getItem(1).isVisible = a.id.base == BasePath.ANIM
        menu.getItem(2).isVisible = a.id.base == BasePath.ANIM
        menu.getItem(3).isEnabled = a.deletable()

        holder.imgc.setOnClickListener {
            val intent = Intent(context, ImgCutEditor::class.java)
            intent.putExtra("Data", JsonEncoder.encode(a.id).toString())

            activity.startActivity(intent)
        }
        holder.mamo.setOnClickListener {
            val intent = Intent(context, MaModelEditor::class.java)
            intent.putExtra("Data", JsonEncoder.encode(a.id).toString())

            activity.startActivity(intent)
        }
        holder.maan.setOnClickListener {
            val intent = Intent(context, MaAnimEditor::class.java)
            intent.putExtra("Data", JsonEncoder.encode(a.id).toString())

            activity.startActivity(intent)
        }

        holder.more.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                popup.show()
            }
        })
        return row
    }

    private fun setIcon(icon : ImageView, a : AnimCE) {
        ici = (ici + 1) % 3
        when(ici) {
            0 ->
                if (a.uni != CommonStatic.getBCAssets().slot[0])
                    icon.setImageBitmap(a.uni.img.bimg() as Bitmap)
                else
                    setIcon(icon, a)
            1 ->
                if (a.previewIcon != null)
                    icon.setImageBitmap(a.previewIcon.img.bimg() as Bitmap)
                else
                    setIcon(icon, a)
            2 ->
                if (a.edi != null)
                    icon.setImageBitmap(a.edi.img.bimg() as Bitmap)
                else if (a.uni != CommonStatic.getBCAssets().slot[0]) {
                    setIcon(icon, a)
                } else {
                    ici = -1
                    icon.setImageBitmap(CommonStatic.getBCAssets().slot[0].img.bimg() as Bitmap)
                }
        }
    }
}
