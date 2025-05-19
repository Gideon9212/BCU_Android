package com.g2.bcu.androidutil.castle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.g2.bcu.ImageViewer
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.util.stage.CastleImg
import common.util.stage.CastleList

class CsListPager : Fragment() {
    companion object {
        fun newInstance(pid: String, sele : Boolean) : CsListPager {
            val cs = CsListPager()

            val bundle = Bundle()

            bundle.putString("pid", pid)
            bundle.putBoolean("sele", sele)
            cs.arguments = bundle

            return cs
        }
    }

    private var pid = Identifier.DEF
    private var sele = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val c = context ?: return null

        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF
        sele = arguments?.getBoolean("sele") ?: false

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p: PackData
        var index = -1

        if(pid.startsWith(Identifier.DEF)) {
            val d = pid.split("-")
            p = UserProfile.getPack(d[0])

            index = if(d.size == 1)
                0
            else
                d[1].toInt()
        } else
            p = UserProfile.getPack(pid)

        if(p is PackData.DefPack) {
            nores.visibility = View.GONE
            val csList = CastleList.defset().toList()[if(index == -1) 0 else index]

            val adapter = CastleAdapter(c, csList.list)
            list.adapter = adapter
            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, posit, _ ->
                if(SystemClock.elapsedRealtime() - StaticStore.cslistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener
                StaticStore.cslistClick = SystemClock.elapsedRealtime()

                if (sele) {
                    val intent = Intent()
                    intent.putExtra("Data", JsonEncoder.encode(csList[posit].id).toString())
                    activity?.setResult(Activity.RESULT_OK, intent)
                    activity?.finish()
                } else {
                    val intent = Intent(c, ImageViewer::class.java)
                    intent.putExtra("Data", JsonEncoder.encode(csList[posit].id).toString())
                    intent.putExtra("Img", ImageViewer.ViewerType.CASTLE.name)
                    c.startActivity(intent)
                }
            }
        } else if(p is PackData.UserPack && p.castles.list.isNotEmpty()) {
            nores.visibility = View.GONE
            val csList = p.castles

            val adapter = CastleAdapter(c, csList.list)
            list.adapter = adapter
            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, posit, _ ->
                if(SystemClock.elapsedRealtime() - StaticStore.cslistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener
                StaticStore.cslistClick = SystemClock.elapsedRealtime()

                val intent = Intent(c, ImageViewer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(csList[posit].id).toString())
                intent.putExtra("Img", ImageViewer.ViewerType.CASTLE.name)

                c.startActivity(intent)
            }
        }

        return view
    }

    internal class CastleAdapter(private val c : Context, private val imgs : List<CastleImg>) : ArrayAdapter<CastleImg>(c, R.layout.list_layout_text_icon, imgs) {

        private class ViewHolder(view: View) {
            val text: TextView = view.findViewById(R.id.spinnertext)
            val icon: ImageView = view.findViewById(R.id.spinnericon)
        }

        override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
            val holder: ViewHolder
            val row: View

            if (view == null) {
                val inf = LayoutInflater.from(context)
                row = inf.inflate(R.layout.list_layout_text_icon, parent, false)
                holder = ViewHolder(row)
                row.tag = holder
            } else {
                row = view
                holder = row.tag as ViewHolder
            }
            holder.text.text = StaticStore.generateIdName(imgs[pos].id, c)

            val img = imgs[pos].img.img
            val w = if (img.width >= img.height) 64f else 64f * (img.width.toFloat() / img.height)
            val h = if (img.height >= img.width) 64f else 64f * (img.height.toFloat() / img.width)
            holder.icon.setImageBitmap(StaticStore.getResizeb(img.bimg() as Bitmap, c, w, h))

            return row
        }
    }
}