package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.stage.adapters.CustomChapterListAdapter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Source.Workspace
import common.pack.UserProfile
import common.util.stage.StageMap
import common.util.stage.info.CustomStageInfo
import kotlinx.coroutines.launch

class PackChapterManager : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.clear()
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (!shared.contains("initial")) {
            val ed = shared.edit()
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
        setContentView(R.layout.activity_pack_chapter)

        val result = intent
        val extra = result.extras

        val pack = UserProfile.getUserPack(extra?.getString("pack")) ?: return

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cuschapterbck)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            val addc = findViewById<Button>(R.id.cuschapteradd)
            val chlist = findViewById<RecyclerView>(R.id.chapterList)
            StaticStore.setDisappear(addc, chlist)

            val chname = findViewById<TextView>(R.id.cuschaptername)
            chname.text = pack.toString()
            chlist.layoutManager = LinearLayoutManager(this@PackChapterManager)
            val adp = CustomChapterListAdapter(pack, this@PackChapterManager)
            chlist.adapter = adp
            val touch = ItemTouchHelper(object: ItemTouchHelper.Callback() {
                override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
                }
                override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                    val from = src.bindingAdapterPosition
                    val to = dest.bindingAdapterPosition
                    pack.mc.maps.reorder(from, to)
                    adp.notifyItemMoved(from, to)
                    return false
                }
                override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                    val pos = holder.bindingAdapterPosition
                    val subchapter = pack.mc.maps[pos]
                    pack.mc.maps.remove(subchapter)
                    for (s in subchapter.list) {
                        if (s.info != null)
                            (s.info as CustomStageInfo).destroy(false)
                        for (si in pack.mc.si)
                            si.remove(s)
                    }
                    if (pack.mc.maps.isEmpty)
                        StaticStore.allMCs.remove(pack.sid)
                    adp.notifyItemRemoved(pos)
                }
            })
            touch.attachToRecyclerView(chlist)

            addc.setOnClickListener {
                val map = pack.mc.add{ StageMap(it) }
                adp.notifyItemInserted(pack.mc.maps.indexOf(map))
                if (pack.mc.maps.size() == 1) {
                    StaticStore.allMCs.clear()

                    StaticStore.allMCs.addAll(StaticStore.BCMapCodes)
                    val packs = UserProfile.getUserPacks()
                    for(p in packs)
                        if(p.mc.maps.list.isNotEmpty())
                            StaticStore.allMCs.add(p.sid)
                }
            }

            bck.setOnClickListener {
                Workspace.saveWorkspace(false)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackChapterManager, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            StaticStore.setAppear(addc, chlist)
            StaticStore.setDisappear(st, prog)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }
}