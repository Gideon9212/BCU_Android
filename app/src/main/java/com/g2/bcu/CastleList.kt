package com.g2.bcu

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
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
import com.g2.bcu.androidutil.castle.CsListPager
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData.UserPack
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CastleList : AppCompatActivity() {

    var pack : UserPack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.clear()

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
        setContentView(R.layout.activity_castle_list)
        pack = UserProfile.getUserPack(intent.extras?.getString("pack") ?: "")
        
        lifecycleScope.launch {
            //Prepare
            val castleList = findViewById<NestedScrollView>(R.id.cslistscroll)
            val status = findViewById<TextView>(R.id.status)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val tab = findViewById<TabLayout>(R.id.cslisttab)
            val pager = findViewById<ViewPager2>(R.id.cslistpager)
            val bck = findViewById<FloatingActionButton>(R.id.csbck)

            StaticStore.setDisappear(castleList)
            
            progression.isIndeterminate = true
            
            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@CastleList, { _ -> }, { t -> runOnUiThread { status.text = t }})
            }

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            val keys = getExistingPack()
            pager.adapter = CsListTab()
            pager.offscreenPageLimit = keys.size

            TabLayoutMediator(tab, pager) { t, position ->
                val def = getString(R.string.pack_default)

                t.text = when(position) {
                    0 -> "$def - RC"
                    1 -> "$def - EC"
                    2 -> "$def - WC"
                    3 -> "$def - SC"
                    else -> StaticStore.getPackName(keys[position])
                }
            }.attach()

            if(keys.size == 1) {
                tab.visibility = View.GONE

                val collapse = findViewById<CollapsingToolbarLayout>(R.id.cscollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }

            bck.setOnClickListener {
                finish()
            }

            StaticStore.setAppear(castleList)
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

    private fun getExistingPack() : ArrayList<String> {
        val res = ArrayList<String>()
        res.add(Identifier.DEF+"-0")
        res.add(Identifier.DEF+"-1")
        res.add(Identifier.DEF+"-2")
        res.add(Identifier.DEF+"-3")

        if (pack != null) {
            if (!pack!!.castles.isEmpty)
                res.add(pack!!.sid)

            for(str in pack!!.desc.dependency)
                if(!UserProfile.getUserPack(str).castles.isEmpty)
                    res.add(str)
        } else {
            val packs = UserProfile.getUserPacks()
            for(p in packs)
                if(!p.castles.isEmpty)
                    res.add(p.desc.id)
        }
        return res
    }

    inner class CsListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return CsListPager.newInstance(keys[position], pack != null)
        }
    }
}