package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.GetStrings
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.util.stage.Limit
import common.util.stage.StageLimit
import kotlinx.coroutines.launch
import kotlin.math.max

class LimitEditor : AppCompatActivity() {

    companion object {
        lateinit var lim : Limit
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
        setContentView(R.layout.activity_pack_limit)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cuslimbck)
            bck.setOnClickListener {
                finish()
            }
            onBackPressedDispatcher.addCallback(this@LimitEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val lname : TextView = findViewById(R.id.cuslimname)
            lname.text = extra.getString("name", lim.toString())

            val lnum : EditText = findViewById(R.id.pklimmax)
            lnum.text = SpannableStringBuilder(lim.num.toString())
            lnum.doAfterTextChanged {
                val num = CommonStatic.parseIntN(lnum.text.toString())
                if (!lnum.hasFocus() || num == lim.num)
                    return@doAfterTextChanged
                lim.num = num
            }

            val lminc : EditText = findViewById(R.id.pklimmincost)
            lminc.text = SpannableStringBuilder(lim.min.toString())
            lminc.doAfterTextChanged {
                val min = CommonStatic.parseIntN(lminc.text.toString())
                if (!lminc.hasFocus() || min == lim.min)
                    return@doAfterTextChanged
                lim.min = min
            }

            val lmaxc : EditText = findViewById(R.id.pklimmaxcost)
            lmaxc.text = SpannableStringBuilder(lim.max.toString())
            lmaxc.doAfterTextChanged {
                val max = CommonStatic.parseIntN(lmaxc.text.toString())
                if (!lmaxc.hasFocus() || max == lim.max)
                    return@doAfterTextChanged
                lim.max = max
            }

            val lrow : Spinner = findViewById(R.id.pklimrow)
            lrow.setPopupBackgroundResource(R.drawable.spinner_popup)
            lrow.adapter = ArrayAdapter(this@LimitEditor, R.layout.spinneradapter, GetStrings(this@LimitEditor).getStrings(R.string.limit_linen, R.string.limit_line2, R.string.limit_line3))
            lrow.setSelection(lim.line)
            lrow.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(par: AdapterView<*>, v: View?, position: Int, id: Long) { lim.line = position }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            val lstar : LinearLayout = findViewById(R.id.pklimstar)
            for (i in 0 until lstar.childCount) {
                val star = lstar.getChildAt(i) as CheckBox
                star.isChecked = lim.star == 0 || ((lim.star shr i) and 1) > 0

                star.setOnClickListener {
                    lim.star = lim.star xor (1 shl i)
                    for (j in 0 until lstar.childCount) {
                        val brar = lstar.getChildAt(j) as CheckBox
                        brar.isChecked = lim.star == 0 || ((lim.star shr j) and 1) > 0
                    }
                }
            }
            val lrars : LinearLayout = findViewById(R.id.pklimrars)
            for (i in 0 until lrars.childCount) {
                val rar = lrars.getChildAt(i) as CheckBox
                rar.isChecked = lim.rare == 0 || ((lim.rare shr i) and 1) > 0

                rar.setOnClickListener {
                    lim.rare = lim.rare xor (1 shl i)
                    for (j in 0 until lrars.childCount) {
                        val brar = lrars.getChildAt(j) as CheckBox
                        brar.isChecked = lim.rare == 0 || ((lim.rare shr j) and 1) > 0
                    }
                }
            }

            val lcgroup : Button = findViewById(R.id.pklimcharagroup)
            val t = "${getString(R.string.limit_chra)}: ${lim.group}"
            lcgroup.text = t
            lcgroup.setOnClickListener {
                //TODO("WIP")
            }

            val llvr : Button = findViewById(R.id.pklimlvrestriction)
            val u = "${getString(R.string.limit_lvres)}: ${lim.lvr}"
            llvr.text = u
            llvr.setOnClickListener {
                //TODO("WIP")
            }

            if (lim.stageLimit == null)
                lim.stageLimit = StageLimit()

            val lmaxu : EditText = findViewById(R.id.pklimbank)
            lmaxu.text = SpannableStringBuilder(lim.stageLimit.maxMoney.toString())
            lmaxu.doAfterTextChanged {
                val num = CommonStatic.parseIntN(lmaxu.text.toString())
                if (!lmaxu.hasFocus() || num == lim.stageLimit.maxMoney)
                    return@doAfterTextChanged
                lim.stageLimit.maxMoney = num
            }
            val lcd : EditText = findViewById(R.id.pklimucd)
            lcd.text = SpannableStringBuilder(lim.stageLimit.globalCooldown.toString())
            lcd.doAfterTextChanged {
                val num = CommonStatic.parseIntN(lcd.text.toString())
                if (!lcd.hasFocus() || num == lim.stageLimit.globalCooldown)
                    return@doAfterTextChanged
                lim.stageLimit.globalCooldown = num
            }
            val lunico : EditText = findViewById(R.id.pklimunico)
            lunico.text = SpannableStringBuilder(lim.stageLimit.globalCost.toString())
            lunico.doAfterTextChanged {
                val num = CommonStatic.parseIntN(lunico.text.toString())
                if (!lunico.hasFocus() || num == lim.stageLimit.globalCost)
                    return@doAfterTextChanged
                lim.stageLimit.globalCost = num
            }
            val lmaxd : EditText = findViewById(R.id.pklimmaxdeploy)
            lmaxd.text = SpannableStringBuilder(lim.stageLimit.maxUnitSpawn.toString())
            lmaxd.doAfterTextChanged {
                val num = CommonStatic.parseIntN(lmaxd.text.toString())
                if (!lmaxd.hasFocus() || num == lim.stageLimit.maxUnitSpawn)
                    return@doAfterTextChanged
                lim.stageLimit.maxUnitSpawn = num
            }
            val lscd : CheckBox = findViewById(R.id.pklimscd)
            lscd.isChecked = lim.stageLimit.coolStart
            lscd.setOnClickListener { lim.stageLimit.coolStart = lscd.isChecked }

            val lcosm : LinearLayout = findViewById(R.id.pklimcosm)
            for (i in 1 until lcosm.childCount) {
                val cosm = lcosm.getChildAt(i) as EditText
                cosm.text = SpannableStringBuilder(lim.stageLimit.costMultiplier[i-1].toString())
                cosm.doAfterTextChanged {
                    val num = CommonStatic.parseIntN(cosm.text.toString())
                    if (!cosm.hasFocus() || num == lim.stageLimit.costMultiplier[i-1])
                        return@doAfterTextChanged
                    lim.stageLimit.costMultiplier[i-1] = num
                }
            }
            val lcdm : LinearLayout = findViewById(R.id.pklimcdm)
            for (i in 1 until lcdm.childCount) {
                val cdm = lcdm.getChildAt(i) as EditText
                cdm.text = SpannableStringBuilder(lim.stageLimit.cooldownMultiplier[i-1].toString())
                cdm.doAfterTextChanged {
                    val num = CommonStatic.parseIntN(cdm.text.toString())
                    if (!cdm.hasFocus() || num == lim.stageLimit.cooldownMultiplier[i-1])
                        return@doAfterTextChanged
                    lim.stageLimit.cooldownMultiplier[i-1] = num
                }
            }
            val lrspwn : LinearLayout = findViewById(R.id.pklimrspwn)
            for (i in 1 until lrspwn.childCount) {
                val rspwn = lrspwn.getChildAt(i) as EditText
                rspwn.text = SpannableStringBuilder(lim.stageLimit.rarityDeployLimit[i-1].toString())
                rspwn.doAfterTextChanged {
                    val num = max(-1, CommonStatic.parseIntN(rspwn.text.toString()))
                    if (!rspwn.hasFocus() || num == lim.stageLimit.rarityDeployLimit[i-1])
                        return@doAfterTextChanged
                    lim.stageLimit.rarityDeployLimit[i-1] = num
                    rspwn.text = SpannableStringBuilder(lim.stageLimit.rarityDeployLimit[i-1].toString())
                }
            }
            val ldpspwn : LinearLayout = findViewById(R.id.pklimdpspwn)
            for (i in 1 until ldpspwn.childCount) {
                val dpspwn = ldpspwn.getChildAt(i) as TableRow

                val dpcount = dpspwn.getChildAt(0) as EditText
                dpcount.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationTimes[i-1].toString())
                val dptime = dpspwn.getChildAt(2) as EditText
                dptime.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationDelay[i-1].toString())

                dpcount.doAfterTextChanged {
                    val num = CommonStatic.parseIntN(dpcount.text.toString())
                    if (!dpcount.hasFocus() || num == lim.stageLimit.deployDuplicationTimes[i-1])
                        return@doAfterTextChanged
                    lim.stageLimit.deployDuplicationTimes[i-1] = num
                    if (num == 0) {
                        lim.stageLimit.deployDuplicationTimes[i-1] = 0
                        dptime.text = SpannableStringBuilder("0")
                    } else if (lim.stageLimit.deployDuplicationDelay[i - 1] == 0) {
                        lim.stageLimit.deployDuplicationTimes[i-1] = 1
                        dptime.text = SpannableStringBuilder("1")
                    }
                }
                dptime.doAfterTextChanged {
                    var num = CommonStatic.parseIntN(dptime.text.toString())
                    if (!dptime.hasFocus() || num == lim.stageLimit.deployDuplicationDelay[i-1])
                        return@doAfterTextChanged
                    num = if (lim.stageLimit.deployDuplicationTimes[i-1] == 0) 0 else max(num, 1)

                    lim.stageLimit.deployDuplicationDelay[i-1] = num
                    dptime.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationDelay[i-1].toString())
                }
            }
            val banl : ListView = findViewById(R.id.pklimcbanlist)
            val adp = ComboBanAdapter(this@LimitEditor, lim.stageLimit)
            banl.adapter = adp
            banl.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, pos: Int, _: Long ->
                if (!lim.stageLimit.bannedCatCombo.remove(pos))
                    lim.stageLimit.bannedCatCombo.add(pos)
                adp.notifyDataSetChanged()
            }

            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)
            StaticStore.setDisappear(st, prog)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    internal class ComboBanAdapter(private val ctx : LimitEditor, private val limt : StageLimit) : ArrayAdapter<String>(ctx, R.layout.list_layout_text, GetStrings(ctx).getAStrings(StaticStore.comnames)) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = super.getView(position, convertView, parent)

            if (limt.bannedCatCombo.contains(position))
                row.setBackgroundColor(StaticStore.getAttributeColor(ctx, R.attr.SemiWarningPrimary))
            else
                row.setBackgroundColor(StaticStore.getAttributeColor(ctx, R.attr.backgroundPrimary))

            return row
        }
    }
}