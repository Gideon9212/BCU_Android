package com.g2.bcu.androidutil.pack.adapters

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.pack.PackData
import common.pack.Source
import common.pack.UserProfile
import java.io.File
import java.text.DecimalFormat

class PackManagementAdapter(private val ac: Activity, private val pList: ArrayList<PackData.UserPack>) : ArrayAdapter<PackData.UserPack>(ac, R.layout.pack_manage_list_layout, pList) {
    class ViewHolder(v: View) {
        val id = v.findViewById<TextView>(R.id.pmanid)!!
        val name = v.findViewById<TextView>(R.id.pmanname)!!
        val desc = v.findViewById<TextView>(R.id.pmandesc)!!
        val more = v.findViewById<FloatingActionButton>(R.id.pmanmore)!!
        val icn = v.findViewById<ImageView>(R.id.packIcon)!!
    }

    var dialog = AlertDialog.Builder(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)

            row = inf.inflate(R.layout.pack_manage_list_layout, parent, false)
            holder = ViewHolder(row)

            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHolder
        }

        val p = pList[position]

        val title = if(p.desc.author == null || p.desc.author.isBlank()) { p.sid
        } else p.sid + " [${p.desc.author}]"

        holder.id.text = title
        holder.name.text = p.desc.names.toString().ifBlank { p.sid }
        if (p.icon == null)
            holder.icn.visibility = View.GONE
        else
            holder.icn.setImageBitmap(p.icon.img.bimg() as Bitmap)

        val f = (p.source as Source.ZipSource).packFile

        if(!f.exists()) {
            Log.w("PackManagementAdapter", "File ${f.absolutePath} not existing")

            return row
        }

        val desc = "${f.name} (${byteToMB(f.length())}MB)"
        holder.desc.text = desc

        row.setOnLongClickListener {
            val descPage = Dialog(ac)
            descPage.setContentView(R.layout.pack_description_viewer)
            descPage.setCancelable(true)

            val banner = descPage.findViewById<ImageView>(R.id.packBanner)
            if (p.banner == null) banner.visibility = View.GONE
            else banner.setImageBitmap(p.banner.img.bimg() as Bitmap)

            val pdesc = descPage.findViewById<TextView>(R.id.packDescription)
            val pstr = p.desc.info.toString()
            if (pstr.isNotBlank()) {
                pdesc.text = HtmlCompat.fromHtml(pstr.replace("\n","<br>"), HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
                LinkifyCompat.addLinks(pdesc, Linkify.WEB_URLS)
                pdesc.movementMethod = LinkMovementMethod.getInstance()
            } else descPage.findViewById<ScrollView>(R.id.scrollDesc).visibility = View.GONE

            val pver = descPage.findViewById<TextView>(R.id.packVersion)
            val pber = descPage.findViewById<TextView>(R.id.packBCUver)
            val t = "Version: ${p.desc.version}"
            pver.text = t
            val tb = "CORE: ${p.desc.FORK_VERSION} | ${p.desc.BCU_VERSION}"
            pber.text = tb

            val pcre = descPage.findViewById<TextView>(R.id.packCreateD)
            val pexp = descPage.findViewById<TextView>(R.id.packExportD)
            if (p.desc.creationDate != null || p.desc.exportDate != null) {
                val d = "Created: ${p.desc.creationDate?.substring(0, 9) ?: "N/A"}"
                pcre.text = d
                val db = "Exported: ${p.desc.exportDate?.substring(0, 9) ?: "N/A"}"
                pexp.text = db
            } else {
                pcre.visibility = View.GONE
                pexp.visibility = View.GONE
            }
            val psta = descPage.findViewById<TextView>(R.id.packSTCount)
            val pani = descPage.findViewById<TextView>(R.id.packCAnim)
            val s = "Stage Count: ${p.mc.stageCount}"
            psta.text = s

            val sb = "Can${(if (p.desc.allowAnim) "" else "'t")} copy anims"
            pani.text = sb

            val cancel = descPage.findViewById<Button>(R.id.packExitB)
            cancel.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    descPage.dismiss()
                }
            })
            if (!ac.isDestroyed && !ac.isFinishing)
                descPage.show()

            true
        }

        val popup = PopupMenu(context, holder.more)
        val menu = popup.menu
        popup.menuInflater.inflate(R.menu.pack_list_option_menu, menu)

        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.saveremove -> {
                    p.save.ulkUni.clear()
                    p.save.cSt.clear()
                    val sf = CommonStatic.ctx.getAuxFile("./saves/" + p.desc.id + ".packsave")
                    if (sf.exists())
                        sf.delete()
                    it.isVisible = false
                }
                R.id.packremove -> {
                    dialog.setTitle(R.string.pack_manage_remove_sure)
                    dialog.setMessage(R.string.pack_manage_remove_msg)

                    dialog.setPositiveButton(R.string.remove) { _, _ ->
                        deletePack(p, f)
                        rebuildPackList()
                        notifyDataSetChanged()
                        StaticStore.showShortMessage(context, R.string.pack_remove_result)

                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    dialog.setNegativeButton(R.string.main_file_cancel) {_, _ ->
                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    StaticStore.fixOrientation(ac)

                    if (!ac.isDestroyed && !ac.isFinishing) {
                        dialog.show()
                    }
                }
                R.id.packshare -> {
                    if(!f.exists()) {
                        StaticStore.showShortMessage(context, R.string.pack_share_notfound)

                        return@setOnMenuItemClickListener  false
                    }


                    val uri = FileProvider.getUriForFile(context,"com.g2.bcu.provider",f)

                    val intent = Intent()

                    intent.action = Intent.ACTION_SEND
                    intent.type = "*/*"
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)

                    val i = Intent.createChooser(intent, context.getString(R.string.pack_manage_share))

                    ac.startActivity(i)
                }
            }

            false
        }

        menu.getItem(1).isEnabled = !cantDelete(p)
        menu.getItem(2).isVisible = p.save?.cSt?.isNotEmpty() == true

        holder.more.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                popup.show()
            }
        })

        return row
    }

    override fun getCount(): Int {
        return pList.size
    }

    private fun byteToMB(bytes: Long) : String {
        val df = DecimalFormat("#.##")

        return df.format(bytes.toDouble()/1000000.0)
    }

    private fun deletePack(p: PackData.UserPack, pack: File) {
        if(pack.exists())
            pack.delete()

        val shared = context.getSharedPreferences(StaticStore.PACK, Context.MODE_PRIVATE)
        val editor = shared.edit()
        val mList = ArrayList<File>()
        val fList = File(StaticStore.dataPath+"music/").listFiles() ?: return

        for(f in fList)
            if(f.name.startsWith("${p.sid}-"))
                mList.add(f)

        for(m in mList) {
            Log.i("Definer::extractMusic", "Deleted music : ${m.absolutePath}")
            m.delete()
            editor.remove(m.name)
        }
        editor.remove(p.sid)
        editor.apply()
        UserProfile.unloadPack(p)
    }

    private fun cantDelete(p: PackData.UserPack) : Boolean {
        for(pack in UserProfile.getUserPacks()) {
            pack ?: continue
            if(pack.sid == p.sid)
                continue

            if (pack.desc.dependency.contains(p.sid))
                return true
        }
        return false
    }

    private fun rebuildPackList() {
        pList.clear()
        for(pack in UserProfile.getUserPacks())
            if (!pack.editable)
                pList.add(pack)
    }
}