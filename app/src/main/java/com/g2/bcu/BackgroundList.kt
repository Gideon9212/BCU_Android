package com.g2.bcu

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import com.g2.bcu.androidutil.supports.adapter.BGListPager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.PackData.UserPack
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

class BackgroundList : AppCompatActivity() {

    var pack : UserPack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.putBoolean("frame", true)
            ed.putBoolean("apktest", false)
            ed.putInt("default_level", 50)
            ed.putInt("Language", 0)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_background_list)
        pack = UserProfile.getUserPack(intent.extras?.getString("pack") ?: "")

        lifecycleScope.launch {
            val tab = findViewById<TabLayout>(R.id.bglisttab)
            val pager = findViewById<ViewPager2>(R.id.bglistpager)
            val bck = findViewById<FloatingActionButton>(R.id.bgbck)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val status = findViewById<TextView>(R.id.status)

            StaticStore.setDisappear(tab, pager, bck)

            withContext(Dispatchers.IO) {
                Definer.define(this@BackgroundList, { _ -> }, { t -> runOnUiThread { status.text = t }})
            }
            val keys = getExistingPack()
            pager.adapter = BGListTab()
            pager.offscreenPageLimit = keys.size

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if (position == 0) {
                    getString(R.string.pack_default)
                } else {
                    val pack = UserProfile.getUserPack(keys[position])

                    if (pack == null) {
                        keys[position]
                    }

                    val name = pack?.desc?.names.toString()

                    name.ifEmpty {
                        keys[position]
                    }
                }
            }.attach()

            if(keys.size == 1) {
                tab.visibility = View.GONE

                val collapse = findViewById<CollapsingToolbarLayout>(R.id.bgcollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }

            bck.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    finish()
                }
            })

            StaticStore.setAppear(tab, pager, bck)
            StaticStore.setDisappear(progression, status)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun getExistingPack(): ArrayList<String> {
        val res = ArrayList<String>()
        res.add(Identifier.DEF)

        if (pack != null) {
            if (!pack!!.bgs.isEmpty)
                res.add(pack!!.sid)

            for(str in pack!!.desc.dependency)
                if(!UserProfile.getUserPack(str).bgs.isEmpty)
                    res.add(str)
        } else {
            val packs = UserProfile.getUserPacks()
            for(p in packs)
                if(!p.bgs.isEmpty)
                    res.add(p.desc.id)
        }
        return res
    }

    inner class BGListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return BGListPager.newInstance(keys[position], pack != null)
        }
    }
}