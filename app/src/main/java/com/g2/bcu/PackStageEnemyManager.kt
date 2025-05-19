package com.g2.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import com.g2.bcu.androidutil.stage.adapters.CustomStEnList
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.util.stage.MapColc.PackMapColc
import common.util.stage.Music
import common.util.stage.Revival
import common.util.stage.SCDef
import common.util.stage.SCDef.Line
import common.util.stage.Stage
import common.util.unit.AbEnemy
import kotlinx.coroutines.launch

class PackStageEnemyManager : AppCompatActivity() {

    private lateinit var list : SCDef
    lateinit var notif : () -> Unit
    val revi = intArrayOf(-1, 0)

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data

        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val e = StaticStore.transformIdentifier(data.getStringExtra("Data"))?.get() ?: return@registerForActivityResult
            if (e is Music) {
                var r = list.datas[revi[0]].rev
                for (i in 0 until revi[1])
                    r = r.rev
                r.bgm = e.id
            }
            if (e !is AbEnemy)
                return@registerForActivityResult

            if (revi[0] == -1) {
                val nl = Array(list.datas.size + 1) {
                    if (it == 0) {
                        val l = Line()
                        l.enemy = e.id
                        l
                    } else
                        list.datas[it - 1]
                }
                list.datas = nl
            } else if (revi[0] <= -2) {
                list.datas[-(revi[0] + 2)].enemy = e.id
            } else {
                var r = list.datas[revi[0]].rev
                for (i in 0 until revi[1])
                    r = r.rev

                if (r == null) {
                    if (revi[1] == 0)
                        list.datas[revi[0]].rev = Revival(e.id)
                    else {
                        r = list.datas[revi[0]].rev
                        for (i in 0 until revi[1] - 1)
                            r = r.rev
                        r.rev = Revival(r, e.id)
                    }
                } else
                    r.enemy = e.id
            }
            notif()
        }
    }

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
        setContentView(R.layout.activity_pack_stage_enemy)

        val result = intent
        val extra = result.extras ?: return

        val st = StaticStore.transformIdentifier<Stage>(extra.getString("stage"))?.get() ?: return
        val pack = (st.mc as PackMapColc).pack
        list = st.data

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cusstenebck)
            bck.setOnClickListener {
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackStageEnemyManager, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val nam = findViewById<TextView>(R.id.cusstenename)
            nam.text = st.toString()

            val elist = findViewById<RecyclerView>(R.id.cusstenelist)
            elist.layoutManager = LinearLayoutManager(this@PackStageEnemyManager)
            val adp = CustomStEnList(this@PackStageEnemyManager, st)
            elist.adapter = adp
            val touch = ItemTouchHelper(object: ItemTouchHelper.Callback() {
                override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
                }
                override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                    val from = list.datas.size - src.bindingAdapterPosition - 1
                    val to = list.datas.size - dest.bindingAdapterPosition - 1
                    val temp = list.datas[from]
                    list.datas[from] = list.datas[to]
                    list.datas[to] = temp
                    adp.notifyItemMoved(src.bindingAdapterPosition, dest.bindingAdapterPosition)

                    return false
                }
                override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                    val pos = list.datas.size - holder.bindingAdapterPosition - 1
                    val nl = Array(list.datas.size - 1) {
                        if (it < pos) list.datas[it]
                        else list.datas[it + 1]
                    }
                    list.datas = nl
                    adp.notifyItemRemoved(holder.bindingAdapterPosition)
                }
            })
            touch.attachToRecyclerView(elist)

            val addenemy = findViewById<FloatingActionButton>(R.id.cussteneadd)
            addenemy.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    revi[0] = -1
                    notif = { adp.notifyItemInserted(list.datas.size - 1) }

                    val intent = Intent(this@PackStageEnemyManager, EnemyList::class.java)
                    intent.putExtra("mode", EnemyList.Mode.SELECTION.name)
                    intent.putExtra("pack", pack.sid)

                    resultLauncher.launch(intent)
                }
            })
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }
}