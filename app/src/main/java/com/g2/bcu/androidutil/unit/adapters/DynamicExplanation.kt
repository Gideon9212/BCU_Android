package com.g2.bcu.androidutil.unit.adapters

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.Fragment
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbUnit

class DynamicExplanation : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(`val`: Int, data: Identifier<AbUnit>, titles: Array<String>): DynamicExplanation {
            val explanation = DynamicExplanation()
            val bundle = Bundle()

            bundle.putInt("Number", `val`)
            bundle.putStringArray("Title", titles)
            bundle.putString("Data", JsonEncoder.encode(data).toString())
            explanation.arguments = bundle

            return explanation
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.chara_explanation, container, false)
        val arg = arguments ?: return view

        val data = StaticStore.transformIdentifier<AbUnit>(arg.getString("Data")) ?: return view
        val fid = arg.getInt("Number", 0)

        val au = data.get() ?: return view
        val f = au.forms[fid]

        val unitname = view.findViewById<TextView>(R.id.charaexname)
        val name = MultiLangCont.get(f) ?: f.names.toString()
        unitname.text = name

        val explains = view.findViewById<TextView>(R.id.charaexp)
        explains.text = HtmlCompat.fromHtml(f.explanation.replace("\n","<br>"), HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
        LinkifyCompat.addLinks(explains, Linkify.WEB_URLS)
        explains.movementMethod = LinkMovementMethod.getInstance()
        explains.setPadding(0, 0, 0, StaticStore.dptopx(24f,requireActivity()))
        return view
    }
}