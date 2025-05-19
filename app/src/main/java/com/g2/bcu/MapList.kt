package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.StaticStore.filter
import com.g2.bcu.androidutil.filter.FilterStage
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.stage.adapters.MapListAdapter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.stage.MapColc
import common.util.stage.StageMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapList : AppCompatActivity() {
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            filter = FilterStage.setFilter(
                StaticStore.stgschname,
                StaticStore.stmschname,
                StaticStore.stgenem,
                StaticStore.stgenemorand,
                StaticStore.stgmusic,
                StaticStore.stgbg,
                StaticStore.stgstar,
                StaticStore.stgbh,
                StaticStore.bhop,
                StaticStore.stgcontin,
                StaticStore.stgboss
            )
            applyFilter()
        }
    }

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
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_map_list)
        
        lifecycleScope.launch {
            val maplist = findViewById<ListView>(R.id.maplist)
            val st = findViewById<TextView>(R.id.status)
            val stageset = findViewById<Spinner>(R.id.stgspin)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(maplist)

            st.text = getString(R.string.stg_info_stgs)

            prog.isIndeterminate = false
            prog.max = 10000

            withContext(Dispatchers.IO) {
                Definer.define(this@MapList, { p -> runOnUiThread { prog.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
            }

            if(filter == null) {
                val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this@MapList, R.layout.spinneradapter, StaticStore.collectMapCollectionNames(this@MapList)) {
                    override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                        val v = super.getView(position, converView, parent)
                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))
                        val eight = StaticStore.dptopx(8f, this@MapList)
                        v.setPadding(eight, eight, eight, eight)
                        return v
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val v = super.getDropDownView(position, convertView, parent)
                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))
                        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                        return v
                    }
                }
                
                stageset.adapter = adapter
                stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        try {
                            val mc = MapColc.get(StaticStore.allMCs[position])
                            val names = ArrayList<Identifier<StageMap>>()
                            try {
                                for (i in mc.maps.list.indices) {
                                    val stm = mc.maps.list[i]
                                    if (mc.getSave(false)?.unlocked(stm) != false || mc.getSave(true)?.nearUnlock(stm) == true)
                                        names.add(stm.id)
                                }
                            } catch (e : java.lang.IndexOutOfBoundsException) {
                                ErrorLogWriter.writeLog(e)
                                return
                            }

                            val mapListAdapter = MapListAdapter(this@MapList, names)
                            maplist.adapter = mapListAdapter
                        } catch (e: NullPointerException) {
                            ErrorLogWriter.writeLog(e)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                val name = ArrayList<Identifier<StageMap>>()
                stageset.setSelection(0)
                val mc = MapColc.get(StaticStore.allMCs[stageset.selectedItemPosition]) ?: return@launch
                for(i in mc.maps.list.indices) {
                    val stm = mc.maps[i]
                    name.add(stm.id)
                }
                val rply = findViewById<FloatingActionButton>(R.id.stgreplay)
                rply.setOnClickListener {
                    val intent = Intent(this@MapList, ReplayList::class.java)
                    intent.putExtra("mc", mc.sid)
                    startActivity(intent)
                }

                val mapListAdapter = MapListAdapter(this@MapList, name)
                maplist.adapter = mapListAdapter
                maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.maplistClick = SystemClock.elapsedRealtime()

                    if (maplist.adapter !is MapListAdapter)
                        return@OnItemClickListener

                    val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener
                    if (stm.list.isEmpty) return@OnItemClickListener
                    if (stm.cont.getSave(false)?.nearUnlock(stm) == true) {
                        StaticStore.showShortMessage(this@MapList, getString(R.string.requirement_list).replace("_",stm.cont.getSave(true).requirements(stm).toString()))
                        return@OnItemClickListener
                    }
                    val intent = Intent(this@MapList, StageList::class.java)
                    intent.putExtra("Data", JsonEncoder.encode(stm.id).toString())
                    intent.putExtra("custom", !StaticStore.BCMapCodes.contains(stm.cont.sid))
                    startActivity(intent)
                }
            } else {
                applyFilter()
            }

            val stgfilter = findViewById<FloatingActionButton>(R.id.stgfilter)

            stgfilter.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@MapList,StageSearchFilter::class.java)
                    resultLauncher.launch(intent)
                }
            })

            val back = findViewById<FloatingActionButton>(R.id.stgbck)
            back.setOnClickListener {
                StaticStore.stgFilterReset()
                StaticStore.filterReset()
                StaticStore.entityname = ""
                finish()
            }

            onBackPressedDispatcher.addCallback(this@MapList, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back.performClick()
                }
            })
            StaticStore.setAppear(maplist)
            StaticStore.setDisappear(st, prog)
        }
    }

    private fun applyFilter() {
        val f = filter ?: return

        val stageset = findViewById<Spinner>(R.id.stgspin)
        val maplist = findViewById<ListView>(R.id.maplist)
        val status = findViewById<TextView>(R.id.status)

        if(f.isEmpty()) {
            stageset.visibility = View.GONE
            maplist.visibility = View.GONE

            status.visibility = View.VISIBLE
            status.setText(R.string.filter_nores)
        } else {
            status.visibility = View.GONE
            stageset.visibility = View.VISIBLE
            maplist.visibility = View.VISIBLE

            val mapCollectionResult = ArrayList<String>()
            val collectionName = StaticStore.collectMapCollectionNames(this)

            val keys = f.keys.toMutableList()
            keys.sort()
            for (i in keys) {
                val index = StaticStore.allMCs.indexOf(i)
                if (index != -1)
                    mapCollectionResult.add(collectionName[index])
            }

            var maxWidth = 0
            val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, R.layout.spinneradapter, mapCollectionResult) {
                override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, converView, parent)
                    (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))
                    val eight = StaticStore.dptopx(8f, this@MapList)
                    v.setPadding(eight, eight, eight, eight)

                    if(maxWidth < v.measuredWidth)
                        maxWidth = v.measuredWidth
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent)
                    (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))
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
                        var index = StaticStore.allMCs.indexOf(keys[position])
                        if (index == -1)
                            index = 0

                        val resmapname = ArrayList<Identifier<StageMap>>()
                        val resmaplist = f[keys[position]] ?: return
                        val mc = MapColc.get(StaticStore.allMCs[index]) ?: return

                        for(i in 0 until resmaplist.size())
                            resmapname.add(mc.maps.list[resmaplist.keyAt(i)].id)
                        val mapListAdapter = MapListAdapter(this@MapList, resmapname)
                        maplist.adapter = mapListAdapter
                    } catch (e: Exception) {
                        ErrorLogWriter.writeLog(e)
                    }
                }
            }
            stageset.adapter = adapter

            val index = StaticStore.allMCs.indexOf(keys[stageset.selectedItemPosition])
            if (index == -1)
                return

            val resmapname = ArrayList<Identifier<StageMap>>()
            val resmaplist = f[keys[stageset.selectedItemPosition]] ?: return
            val mc = MapColc.get(keys[stageset.selectedItemPosition]) ?: return

            for(i in 0 until resmaplist.size()) {
                val stm = mc.maps.list[resmaplist.keyAt(i)]
                resmapname.add(stm.id)
            }

            val mapListAdapter = MapListAdapter(this, resmapname)
            maplist.adapter = mapListAdapter

            maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.maplistClick = SystemClock.elapsedRealtime()
                if(maplist.adapter !is MapListAdapter)
                    return@OnItemClickListener

                val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener
                if (stm.list.isEmpty) return@OnItemClickListener
                if (stm.cont.getSave(false)?.nearUnlock(stm) == true) {
                    StaticStore.showShortMessage(this, getString(R.string.requirement_list).replace("_",stm.cont.getSave(true).requirements(stm).toString()))
                    return@OnItemClickListener
                }
                val intent = Intent(this, StageList::class.java)
                intent.putExtra("Data", JsonEncoder.encode(stm.id).toString())
                intent.putExtra("custom", !StaticStore.BCMapCodes.contains(stm.cont.sid))
                startActivity(intent)
            }
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
}