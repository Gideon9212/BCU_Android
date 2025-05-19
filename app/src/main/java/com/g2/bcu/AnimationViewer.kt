package com.g2.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import com.g2.bcu.androidutil.unit.adapters.UnitListPager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimationViewer : AppCompatActivity() {
    private var sele = false
    private var pack : PackData.UserPack? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                supportFragmentManager.fragments.forEach {
                    if (it is UnitListPager)
                        it.validate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
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
        setContentView(R.layout.activity_animation_viewer)

        val extra = intent.extras
        if(extra != null) {
            pack = UserProfile.getUserPack(extra.getString("pack", ""))
            sele = extra.getBoolean("sele", false)
        }

        lifecycleScope.launch {
            //Prepare
            val back = findViewById<FloatingActionButton>(R.id.animbck)
            val search = findViewById<FloatingActionButton>(R.id.animsch)
            val tab = findViewById<TabLayout>(R.id.unittab)
            val pager = findViewById<ViewPager2>(R.id.unitpager)
            val searchBar = findViewById<TextInputEditText>(R.id.animschname)
            val layout = findViewById<TextInputLayout>(R.id.animschnamel)
            val st = findViewById<TextView>(R.id.status)
            val progression = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(tab, pager, searchBar, layout)
            search.hide()

            progression.isIndeterminate = false
            progression.max = 10000

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@AnimationViewer, { p -> runOnUiThread { progression.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
            }

            //Load UI
            progression.isIndeterminate = true

            if(StaticStore.entityname != "") {
                searchBar.setText(StaticStore.entityname)
            }

            searchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    StaticStore.entityname = s.toString()

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            supportFragmentManager.fragments.forEach {
                                if (it is UnitListPager)
                                    it.validate()
                            }
                        }
                    }
                }
            })

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            val keys = getExistingPack()
            pager.adapter = UnitListTab()
            pager.offscreenPageLimit = keys.size

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if(position == 0) {
                    getString(R.string.pack_default)
                } else {
                    val pack = PackData.getPack(keys[position])

                    if(pack == null) {
                        keys[position]
                    }

                    val name = when (pack) {
                        is PackData.DefPack -> {
                            getString(R.string.pack_default)
                        }
                        is PackData.UserPack -> {
                            StaticStore.getPackName(pack.sid)
                        }
                        else -> {
                            ""
                        }
                    }
                    name.ifEmpty {
                        keys[position]
                    }
                }
            }.attach()

            back.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    StaticStore.filterReset()
                    StaticStore.entityname = ""
                    finish()
                }
            })
            search.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    gotoFilter()
                }
            })
            onBackPressedDispatcher.addCallback(this@AnimationViewer, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back.performClick()
                }
            })

            if(keys.size != 1)
                StaticStore.setAppear(tab)
            else {
                val collapse = findViewById<CollapsingToolbarLayout>(R.id.animcollapse)
                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0
                collapse.layoutParams = param
            }

            StaticStore.setAppear(pager, searchBar, layout)
            search.show()

            StaticStore.setDisappear(st, progression)
        }
    }

    private fun gotoFilter() {
        val intent = Intent(this@AnimationViewer, SearchFilter::class.java)
        if (pack != null)
            intent.putExtra("pack", pack!!.sid)
        resultLauncher.launch(intent)
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
        res.add(Identifier.DEF)

        if (pack != null) {
            if (!pack!!.units.isEmpty)
                res.add(pack!!.sid)

            for(str in pack!!.desc.dependency)
                if(!UserProfile.getUserPack(str).units.isEmpty)
                    res.add(str)
        } else {
            val packs = UserProfile.getUserPacks()
            for (p in packs)
                if (!p.units.isEmpty)
                    res.add(p.sid)
        }
        return res
    }

    inner class UnitListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return UnitListPager.newInstance(keys[position], position, sele)
        }
    }
}