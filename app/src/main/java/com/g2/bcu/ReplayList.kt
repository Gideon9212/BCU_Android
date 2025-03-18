package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.stage.adapters.ReplayListAdapter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.util.stage.MapColc
import common.util.stage.MapColc.DefMapColc
import common.util.stage.MapColc.PackMapColc
import common.util.stage.Replay
import kotlinx.coroutines.launch

class ReplayList : AppCompatActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
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
        setContentView(R.layout.activity_replay_list)

        val intent = intent
        val bundle = intent.extras ?: return

        lifecycleScope.launch {
            val mc = MapColc.get(bundle.getString("mc"))

            val maplist = findViewById<ListView>(R.id.rplyList)
            val st = findViewById<TextView>(R.id.status)
            val stageset = findViewById<Spinner>(R.id.rplyspin)
            val prog = findViewById<ProgressBar>(R.id.prog)

            val replayList = replayList(mc)
            val mapCollectionResult = ArrayList<String>()
            val collectionName = StaticStore.collectMapCollectionNames(this@ReplayList)

            for (mcc in MapColc.values()) {
                val ind = StaticStore.allMCs.indexOf(mcc.sid)
                if (ind == -1)
                    continue

                if (mcc is PackMapColc && mcc.pack.replays.isNotEmpty()) {
                    mapCollectionResult.add(collectionName[ind])
                } else if (mcc is DefMapColc) {
                    for (r in replayList)
                        if (r.st.safeGet()?.mc == mcc) {
                            mapCollectionResult.add(collectionName[ind])
                            break
                        }
                }
            }

            StaticStore.setDisappear(maplist)

            var maxWidth = 0
            val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this@ReplayList, R.layout.spinneradapter, mapCollectionResult) {
                override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, converView, parent)
                    (v as TextView).setTextColor(ContextCompat.getColor(this@ReplayList, R.color.TextPrimary))
                    val eight = StaticStore.dptopx(8f, this@ReplayList)
                    v.setPadding(eight, eight, eight, eight)

                    if(maxWidth < v.measuredWidth)
                        maxWidth = v.measuredWidth
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent)
                    (v as TextView).setTextColor(ContextCompat.getColor(this@ReplayList, R.color.TextPrimary))
                    v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    return v
                }
            }

            val layout = stageset.layoutParams
            layout.width = maxWidth
            stageset.layoutParams = layout
            stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    try {
                        var index = StaticStore.allMCs.indexOf(mapCollectionResult[position])
                        if (index == -1)
                            index = 0
                        val mcc = MapColc.get(StaticStore.allMCs[index]) ?: return

                        val mapListAdapter = ReplayListAdapter(this@ReplayList, replayList(mcc))
                        maplist.adapter = mapListAdapter
                    } catch (e: Exception) {
                        ErrorLogWriter.writeLog(e)
                    }
                }
            }
            stageset.adapter = adapter
            maplist.adapter = ReplayListAdapter(this@ReplayList, replayList)

            val back = findViewById<FloatingActionButton>(R.id.rplybck)
            back.setOnClickListener { finish() }
            onBackPressedDispatcher.addCallback(this@ReplayList, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back.performClick()
                }
            })

            StaticStore.setAppear(maplist)
            StaticStore.setDisappear(st, prog)
        }
    }

    private fun replayList(mc : MapColc) : ArrayList<Replay> {
        val replayList = ArrayList<Replay>()
        for (replay in Replay.getMap().values)
            if (replay.st.safeGet()?.mc == mc)
                replayList.add(replay)

        if (mc is PackMapColc)
            replayList.addAll(mc.pack.replays)
        return replayList
    }
}