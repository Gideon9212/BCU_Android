package com.g2.bcu.androidutil.unit.adapters

import android.app.Activity
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.util.unit.AbUnit
import common.util.unit.Unit

class DynamicFruit(private val activity: Activity, private val data: Identifier<AbUnit>, private val forTrueForm: Boolean) : PagerAdapter() {
    private val imgid = intArrayOf(R.id.fruit1, R.id.fruit2, R.id.fruit3, R.id.fruit4, R.id.fruit5, R.id.xp)
    private val txid = intArrayOf(R.id.fruittext1, R.id.fruittext2, R.id.fruittext3, R.id.fruittext4, R.id.fruittext5, R.id.xptext)
    private val cfdeid = intArrayOf(R.id.cfinf1, R.id.cfinf2, R.id.cfinf3)

    private val fruits = arrayOfNulls<ImageView>(6)
    private val fruittext = arrayOfNulls<TextView>(6)
    private val cfdesc = arrayOfNulls<TextView>(3)

    private val ids = listOf(
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 160, 161, 164, 167, 168, 169,
        170, 171, 179, 180, 181, 182, 183, 184
    )

    private val cftooltip = intArrayOf(
        R.string.fruit1, R.string.fruit2, R.string.fruit3, R.string.fruit4,
        R.string.fruit5, R.string.fruit6, R.string.fruit7, R.string.fruit8, R.string.fruit9,
        R.string.fruit10, R.string.fruit11, R.string.fruit12, R.string.fruit13,
        R.string.fruit14, R.string.fruit15, R.string.fruit16, R.string.fruit1,
        R.string.fruit18, R.string.fruit19, R.string.fruit20, R.string.fruit21, R.string.fruit22,
        R.string.fruit23, R.string.fruit24, R.string.fruit25, R.string.fruit26, R.string.fruit27,
        R.string.fruit28, R.string.fruit29
    )

    private val exist = booleanArrayOf(false, false, false, false, false, true)

    override fun instantiateItem(group: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.fruit_table, group, false) as ViewGroup

        val au = data.get() ?: return layout
        val u = au as Unit

        for (i in fruits.indices) {
            fruits[i] = layout.findViewById(imgid[i])
            fruittext[i] = layout.findViewById(txid[i])
        }

        for (i in cfdesc.indices) {
            cfdesc[i] = layout.findViewById(cfdeid[i])
        }

        val evo = if (forTrueForm) {
            u.info.evo
        } else {
            u.info.zeroEvo
        }

        val icon = StaticStore.fruit?.get((StaticStore.fruit?.size ?: 1) - 1) ?: StaticStore.empty(1, 1)

        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fruits[5]!!.setImageBitmap(StaticStore.getResizeb(icon, activity, 48f))
        } else {
            fruits[5]!!.setImageBitmap(StaticStore.getResizeb(icon, activity, 40f))
        }

        fruittext[5]!!.text = u.info.xp.toString()

        for (i in 0 until fruits.size - 1) {
            try {
                val ic = StaticStore.fruit?.get(ids.indexOf(evo[i][0])) ?: StaticStore.empty(1,1)

                if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    fruits[i]!!.setImageBitmap(StaticStore.getResizeb(ic, activity, 48f))
                } else {
                    fruits[i]!!.setImageBitmap(StaticStore.getResizeb(ic , activity, 40f))
                }

                exist[i] = true
            } catch (e: IndexOutOfBoundsException) {
                fruits[i]!!.setImageBitmap(StaticStore.empty(activity, 48f, 48f))
            }

            if(evo[i][0] != 0) {
                fruits[i]!!.setOnLongClickListener {
                    StaticStore.showShortMessage(activity, activity.getString(cftooltip[ids.indexOf(evo[i][0])]))
                    true
                }
            }

            if (exist[i])
                fruittext[i]!!.text = evo[i][1].toString()
            else
                fruittext[i]!!.text = ""
        }

        val catfruitExplanation = if (forTrueForm) {
            u.info.catfruitExplanation
        } else {
            u.info.ultraFormEvolveExplanation
        }

        if(catfruitExplanation != null) {
            val lines = catfruitExplanation.split("\n")

            for (i in cfdesc.indices) {
                if (i >= lines.size) {
                    cfdesc[i]!!.visibility = View.GONE
                    cfdesc[i]!!.setPadding(0, 0, 0, 0)
                } else {
                    if (i == lines.size - 1 && i != cfdesc.size - 1)
                        cfdesc[i]!!.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))

                    cfdesc[i]!!.text = lines[i]
                }
            }
        } else {
            for (i in cfdesc.indices) {
                cfdesc[i]!!.visibility = View.GONE
            }
        }

        group.addView(layout)

        return layout
    }

    override fun getCount(): Int {
        return 1
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }
}