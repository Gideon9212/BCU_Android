package com.g2.bcu.androidutil.supports.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import com.g2.bcu.androidutil.fakeandroid.CVGraphics
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.pack.Background
import kotlin.math.round

class BGListPager : Fragment() {
    private var pid = Identifier.DEF
    private var sele = false

    companion object {
        fun newInstance(pid: String, sele: Boolean) : BGListPager {
            val blp = BGListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            bundle.putBoolean("sele", sele)

            blp.arguments = bundle

            return blp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF
        sele = arguments?.getBoolean("sele") ?: false

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = UserProfile.getPack(pid) ?: return view

        if(p.bgs.size() != 0) {
            nores.visibility = View.GONE

            val c = activity ?: return view
            val adapter = BackgroundAdapter(list, c, p.bgs.list)
            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener
                StaticStore.bglistClick = SystemClock.elapsedRealtime()

                if (sele) {
                    val intent = Intent()
                    intent.putExtra("Data", JsonEncoder.encode(p.bgs[position].id).toString())
                    c.setResult(Activity.RESULT_OK, intent)
                    c.finish()
                } else {
                    val intent = Intent(c, ImageViewer::class.java)
                    if(pid == Identifier.DEF)
                        intent.putExtra("BGNum", position)
                    intent.putExtra("Data", JsonEncoder.encode(p.bgs[position].id).toString())
                    intent.putExtra("Img", ImageViewer.ViewerType.BACKGROUND.name)
                    c.startActivity(intent)
                }
            }
        } else {
            list.visibility = View.GONE
        }

        return view
    }

    internal class BackgroundAdapter(private val l : ListView, private val c : Context, private val bgs : List<Background>) : ArrayAdapter<Background>(c, R.layout.list_layout_text_icon, bgs) {

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

            holder.text.text = generateName(bgs[pos].id)
            try {
                val width = 64f
                val height = 64f

                val paint = Paint().apply {
                    isFilterBitmap = true
                    isAntiAlias = true
                }

                val b = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(b)

                val bg = bgs[pos]
                bg.check()
                val cv = CVGraphics(canvas, Paint(), paint, false)

                if (bg.top) {
                    val groundPart = bg.parts[Background.BG]
                    val skyPart = bg.parts[Background.TOP]

                    val totalHeight = groundPart.height + skyPart.height
                    val ratio = height / totalHeight.toFloat()
                    val imageWidth = round(groundPart.width * ratio)
                    val groundHeight = round(groundPart.height * ratio)
                    val skyHeight = round(skyPart.height * ratio)

                    var groundGradient = round(height * 0.1f)
                    val skyGradient = round(height * 0.1f)
                    if (groundGradient + groundHeight + skyHeight + skyGradient != height)
                        groundGradient += height - (groundGradient + groundHeight + skyHeight + skyGradient)

                    var currentX = 0f
                    while (currentX < width) {
                        cv.drawImage(skyPart, currentX, skyGradient, imageWidth, skyHeight)
                        cv.drawImage(groundPart, currentX, skyGradient + skyHeight, imageWidth, groundHeight)
                        currentX += imageWidth
                    }

                    cv.gradRect(0f, skyGradient + skyHeight + groundHeight, width, groundGradient, 0f,
                        skyGradient + skyHeight + groundHeight, getColorData(bg, 2), 0f, height, getColorData(bg, 3)
                    )
                    cv.gradRect(0f, 0f, width, skyGradient, 0f, 0f, getColorData(bg, 0), 0f,
                        skyGradient, getColorData(bg, 1)
                    )
                } else {
                    val groundPart = bg.parts[Background.BG]
                    val ratio = height / (groundPart.height * 2f)

                    val imageWidth = round(groundPart.width * ratio)
                    val groundHeight = round(groundPart.height * ratio)
                    var groundGradient = round(height * 0.1f)
                    val skyGradient = round(height * 0.1f + groundHeight)
                    if (groundGradient + groundHeight + skyGradient != height)
                        groundGradient += height - (groundGradient + groundHeight + skyGradient)

                    var currentX = 0f

                    while (currentX < width) {
                        cv.drawImage(groundPart, currentX, skyGradient, imageWidth, groundHeight)
                        currentX += imageWidth
                    }
                    cv.gradRect(0f, skyGradient + groundHeight, width, groundGradient, 0f, skyGradient + groundHeight,
                        getColorData(bg, 2), 0f, height, getColorData(bg, 3)
                    )
                    cv.gradRect(0f, 0f, width, skyGradient, 0f, 0f, getColorData(bg, 0), 0f, skyGradient,
                        getColorData(bg, 1)
                    )
                }
                holder.icon.setImageBitmap(b)
                bg.unload()
            } catch (e : Exception) {
                e.printStackTrace()
            }

            return row
        }

        private fun generateName(id: Identifier<Background>) : String {
            return if(id.pack == Identifier.DEF) {
                "${c.getString(R.string.pack_default)} - ${Data.trio(id.id)}${if (id.get().bgEffect?.get() != null) " (Eff ${id.get().bgEffect.get()})" else ""}"
            } else
                "${StaticStore.getPackName(id.pack)} - ${Data.trio(id.id)}${if (id.get().bgEffect?.get() != null) " (Eff ${id.get().bgEffect.get()})" else ""}"
        }

        private fun getColorData(bg: Background, mode: Int) : IntArray {
            bg.cs ?: return intArrayOf(0, 0, 0)

            if(bg.cs.isEmpty())
                return intArrayOf(0, 0, 0)

            return when(mode) {
                0 -> bg.cs[0] ?: return intArrayOf(0, 0, 0)
                1 -> bg.cs[1] ?: return intArrayOf(0, 0, 0)
                2 -> bg.cs[2] ?: return intArrayOf(0, 0, 0)
                3 -> bg.cs[3] ?: return intArrayOf(0, 0, 0)
                else -> intArrayOf(0, 0, 0)
            }
        }
    }
}