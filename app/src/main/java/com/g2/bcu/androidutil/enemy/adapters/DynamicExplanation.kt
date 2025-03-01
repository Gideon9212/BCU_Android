package com.g2.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import androidx.viewpager.widget.PagerAdapter
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Character

class DynamicExplanation(private val activity: Activity, private val data: Identifier<*>) : PagerAdapter() {

    override fun instantiateItem(group: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.chara_explanation, group, false) as ViewGroup
        val title = layout.findViewById<TextView>(R.id.charaexname)

        val c = data.get() ?: return layout
        if(c !is Character) return layout

        val name = MultiLangCont.get(c) ?: c.names.toString()

        title.text = name
        val exps = layout.findViewById<TextView>(R.id.charaexp)

        exps.text = HtmlCompat.fromHtml(c.explanation.replace("\n","<br>"), HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
        LinkifyCompat.addLinks(exps, Linkify.WEB_URLS)
        exps.movementMethod = LinkMovementMethod.getInstance()
        exps.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))
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