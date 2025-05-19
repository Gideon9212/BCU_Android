package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.pack.adapters.PackCreationAdapter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.pack.Source.Workspace
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PackCreation : AppCompatActivity() {
    //This page will be used to manage the creation of custom packs

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

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
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_pack_creation)
        lifecycleScope.launch {
            //Prepare
            val list = findViewById<ListView>(R.id.pcuslist)
            val more = findViewById<FloatingActionButton>(R.id.pcusoption)
            val bck = findViewById<FloatingActionButton>(R.id.pcusbck)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(list, more)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@PackCreation, { _ -> }, { t -> runOnUiThread { st.text = t }})
            }

            val adp = PackCreationAdapter(this@PackCreation, ArrayList())
            list.adapter = adp
            more.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val str = Workspace.validateWorkspace(Workspace.generatePackID())
                    val pac = UserProfile.initJsonPack(str)
                    pac.desc.author = "WIP"
                    adp.add(pac)
                }
            })
            prog.isIndeterminate = true

            bck.setOnClickListener {
                Workspace.saveWorkspace(false)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackCreation, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            StaticStore.setDisappear(st, prog)
            StaticStore.setAppear(list, more)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }
}