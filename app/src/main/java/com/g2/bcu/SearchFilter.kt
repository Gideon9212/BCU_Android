package com.g2.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import com.g2.bcu.androidutil.supports.adapter.SearchAbilityAdapter
import com.g2.bcu.androidutil.supports.adapter.SearchTraitAdapter
import common.CommonStatic
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.unit.Trait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchFilter : AppCompatActivity() {
    private val rareid = intArrayOf(R.id.schchba, R.id.schchex, R.id.schchr, R.id.schchsr, R.id.schchur, R.id.schchlr)
    private val rarity = arrayOf("0", "1", "2", "3", "4", "5")

    private val atkid = intArrayOf(R.id.schchld, R.id.schchom, R.id.schchmu)
    private val atks = arrayOf("2", "4", "3")

    private val tgToolID = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_de, R.string.sch_re, R.string.sch_wh)

    private val abils = arrayOf(intArrayOf(R.string.sch_abi_we, 1, Data.P_WEAK.toInt()), intArrayOf(R.string.sch_abi_fr, 1, Data.P_STOP.toInt()),
        intArrayOf(R.string.sch_abi_sl, 1, Data.P_SLOW.toInt()), intArrayOf(R.string.sch_abi_ao, 0, Data.AB_ONLY.toInt()), intArrayOf(R.string.sch_abi_st, 1, Data.P_DMGINC.toInt(), 0, 100, 300),
        intArrayOf(R.string.sch_abi_re,1, Data.P_DEFINC.toInt(), 0, 400, 600), intArrayOf(R.string.sch_abi_it, 1, Data.P_DEFINC.toInt(), 0, 600), intArrayOf(R.string.sch_abi_md, 1, Data.P_DMGINC.toInt(), 0, 300, 500),
        intArrayOf(R.string.sch_abi_id,  1, Data.P_DMGINC.toInt(), 0, 500), intArrayOf(R.string.sch_abi_kb, 1, Data.P_KB.toInt()), intArrayOf(R.string.sch_abi_wa, 1, Data.P_WARP.toInt()),
        intArrayOf(R.string.sch_abi_cu, 1, Data.P_CURSE.toInt()), intArrayOf(R.string.sch_abi_iv, 1, Data.P_IMUATK.toInt()), intArrayOf(R.string.sch_abi_str, 1, Data.P_STRONG.toInt()),
        intArrayOf(R.string.sch_abi_su, 1, Data.P_LETHAL.toInt()), intArrayOf(R.string.sch_abi_bd, 1, Data.P_ATKBASE.toInt()), intArrayOf(R.string.sch_abi_cr, 1, Data.P_CRIT.toInt()), intArrayOf(R.string.sch_abi_mk, 1, Data.P_METALKILL.toInt()),
        intArrayOf(R.string.sch_abi_zk, 0, Data.AB_ZKILL.toInt()), intArrayOf(R.string.sch_abi_ck, 0, Data.AB_CKILL.toInt()), intArrayOf(R.string.sch_abi_bb, 1, Data.P_BREAK.toInt()),
        intArrayOf(R.string.sch_abi_shb, 1, Data.P_SHIELDBREAK.toInt()), intArrayOf(R.string.sch_abi_sb, 1, Data.P_SATK.toInt()), intArrayOf(R.string.sch_abi_em, 1, Data.P_BOUNTY.toInt()),
        intArrayOf(R.string.sch_abi_me, 0, Data.AB_METALIC.toInt()), intArrayOf(R.string.sch_abi_mw, 1, Data.P_MINIWAVE.toInt()), intArrayOf(R.string.sch_abi_wv, 1, Data.P_WAVE.toInt()),
        intArrayOf(R.string.sch_abi_ms, 1, Data.P_MINIVOLC.toInt()), intArrayOf(R.string.sch_abi_surge, 1, Data.P_VOLC.toInt()), intArrayOf(R.string.sch_abi_cs, 1, Data.P_DEMONVOLC.toInt()), intArrayOf(R.string.sch_abi_expl, 1, Data.P_BLAST.toInt()),
        intArrayOf(R.string.sch_abi_ws, 1, Data.P_IMUWAVE.toInt(), 1), intArrayOf(R.string.sch_abi_ss, 1, Data.P_SPIRIT.toInt()), intArrayOf(R.string.sch_abi_bk, 0, Data.AB_BAKILL.toInt()), intArrayOf(R.string.sch_abi_bh, 1, Data.P_BSTHUNT.toInt()),
        intArrayOf(R.string.sch_abi_sh, 0, Data.AB_SKILL), intArrayOf(R.string.sch_abi_iw, 1, Data.P_IMUWEAK.toInt()), intArrayOf(R.string.sch_abi_if, 1, Data.P_IMUSTOP.toInt()), intArrayOf(R.string.sch_abi_is, 1, Data.P_IMUSLOW.toInt()), intArrayOf(R.string.sch_abi_ik, 1, Data.P_IMUKB.toInt()),
        intArrayOf(R.string.sch_abi_iwv, 1, Data.P_IMUWAVE.toInt(), 1, -1, 100), intArrayOf(R.string.sch_abi_imsu, 1, Data.P_IMUVOLC.toInt()), intArrayOf(R.string.sch_abi_iexp, 1, Data.P_IMUBLAST.toInt()), intArrayOf(R.string.sch_abi_iwa, 1, Data.P_IMUWARP.toInt()),
        intArrayOf(R.string.sch_abi_ic, 1, Data.P_IMUCURSE.toInt()), intArrayOf(R.string.sch_abi_impoi, 1, Data.P_IMUPOIATK.toInt()), intArrayOf(R.string.sch_abi_wk, 0, Data.AB_WKILL.toInt()),
        intArrayOf(R.string.sch_abi_eva, 0, Data.AB_EKILL.toInt()), intArrayOf(R.string.sch_abi_poi, 1, Data.P_POIATK.toInt()), intArrayOf(R.string.enem_info_barrier, 1, Data.P_BARRIER.toInt()),
        intArrayOf(R.string.sch_abi_ds, 1, Data.P_DEMONSHIELD.toInt()), intArrayOf(R.string.sch_abi_sd,  1, Data.P_DEATHSURGE.toInt()), intArrayOf(R.string.sch_abi_rms,  1, Data.P_REMOTESHIELD.toInt()), intArrayOf(R.string.sch_abi_tps,  1, Data.P_RANGESHIELD.toInt()),
        intArrayOf(R.string.abi_sui, 0, Data.AB_GLASS.toInt()), intArrayOf(R.string.abi_bu, 1, Data.P_BURROW.toInt()), intArrayOf(R.string.abi_rev, 1, Data.P_REVIVE.toInt()),
        intArrayOf(R.string.abi_gh, 0, Data.AB_GHOST.toInt()), intArrayOf(R.string.abi_snk, 1, Data.P_SNIPER.toInt()), intArrayOf(R.string.abi_seal, 1, Data.P_SEAL.toInt()),
        intArrayOf(R.string.abi_stt, 1, Data.P_TIME.toInt()), intArrayOf(R.string.abi_sum, 1, Data.P_SUMMON.toInt()), intArrayOf(R.string.abi_mvatk, 1, Data.P_MOVEWAVE.toInt()),
        intArrayOf(R.string.abi_thch, 1, Data.P_THEME.toInt()), intArrayOf(R.string.abi_poi, 1, Data.P_POISON.toInt()), intArrayOf(R.string.abi_boswv, 1, Data.P_BOSS.toInt()),
        intArrayOf(R.string.abi_armbr, 1, Data.P_ARMOR.toInt()), intArrayOf(R.string.abi_hast, 1, Data.P_SPEED.toInt()), intArrayOf(R.string.sch_abi_ltg, 1, Data.P_LETHARGY.toInt()),
        intArrayOf(R.string.sch_abi_rg, 1, Data.P_RAGE.toInt()),intArrayOf(R.string.sch_abi_hy, 1, Data.P_HYPNO.toInt()), intArrayOf(R.string.sch_abi_cou, 1, Data.P_COUNTER.toInt()),
        intArrayOf(R.string.sch_abi_cap, 1, Data.P_DMGCAP.toInt()), intArrayOf(R.string.sch_abi_cut, 1, Data.P_DMGCUT.toInt()), intArrayOf(R.string.abi_imvatk, 1, Data.P_IMUMOVING.toInt()),
        intArrayOf(R.string.abi_isnk, 0, Data.AB_SNIPERI.toInt()), intArrayOf(R.string.abi_istt, 0, Data.AB_TIMEI.toInt()), intArrayOf(R.string.abi_ipoi, 1, Data.P_IMUPOI.toInt()),
        intArrayOf(R.string.abi_ithch, 0, Data.AB_THEMEI.toInt()), intArrayOf(R.string.abi_iseal, 1, Data.P_IMUSEAL.toInt()), intArrayOf(R.string.abi_iboswv, 0, Data.AB_IMUSW.toInt()),
        intArrayOf(R.string.abi_imcri, 1, Data.P_CRITI.toInt()), intArrayOf(R.string.sch_abi_imusm, 1, Data.P_IMUSUMMON.toInt()), intArrayOf(R.string.sch_abi_imar, 1, Data.P_IMUARMOR.toInt()),
        intArrayOf(R.string.sch_abi_imlt, 1, Data.P_IMULETHARGY.toInt()), intArrayOf(R.string.sch_abi_imr, 1, Data.P_IMURAGE.toInt()), intArrayOf(R.string.sch_abi_imh, 1, Data.P_IMUHYPNO.toInt()),
        intArrayOf(R.string.sch_abi_imsp, 1, Data.P_IMUSPEED.toInt()), intArrayOf(R.string.sch_abi_wlv, 1, Data.P_WORKERLV.toInt()), intArrayOf(R.string.sch_abi_cdc, 1, Data.P_CDSETTER.toInt()),
        intArrayOf(R.string.sch_abi_waur, 1, Data.P_WEAKAURA.toInt()), intArrayOf(R.string.sch_abi_saur, 1, Data.P_STRONGAURA.toInt()), intArrayOf(R.string.sch_abi_stt, 1, Data.P_AI.toInt())) //2nd field is 0 if ability, 1 if proc. For the ones with higher length, the format is: [field index, >= min-amount, < max-amount] (-1 or not making the array as long if none for the amounts)

    private val rarities = arrayOfNulls<CheckBox>(rareid.size)
    private val attacks = arrayOfNulls<CheckBox>(atkid.size)

    private val atkdraw = intArrayOf(212, 112)

    private lateinit var abilAdapter: SearchAbilityAdapter
    private lateinit var traitAdapter: SearchTraitAdapter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (StaticStore.img15 == null)
            StaticStore.readImg()

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

        setContentView(R.layout.activity_search_filter)

        lifecycleScope.launch {
            val scrollLayout = findViewById<NestedScrollView>(R.id.animsc)
            val title = findViewById<TextView>(R.id.schnm)
            val traitOr = findViewById<RadioButton>(R.id.schrdtgor)
            val attackTypeOr = findViewById<RadioButton>(R.id.schrdatkor)
            val abilityOr = findViewById<RadioButton>(R.id.schrdabor)
            val multipleTarget = findViewById<RadioButton>(R.id.schrdatkmu)
            val singleTarget = findViewById<RadioButton>(R.id.schrdatksi)
            val abilityList = findViewById<RecyclerView>(R.id.schchabrec)
            val traitList = findViewById<RecyclerView>(R.id.schchtgrec)
            val stat = findViewById<FloatingActionButton>(R.id.eschstat)
            val reset = findViewById<FloatingActionButton>(R.id.schreset)
            val status = findViewById<TextView>(R.id.status)
            val progress = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(scrollLayout, title, stat, reset)

            withContext(Dispatchers.IO) {
                Definer.define(this@SearchFilter, { _ -> }, { t -> runOnUiThread { status.text = t }})
            }

            multipleTarget.compoundDrawablePadding = StaticStore.dptopx(16f, this@SearchFilter)

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                multipleTarget.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 40f), null)
                singleTarget.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 40f), null)
            } else {
                multipleTarget.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 32f), null)
                singleTarget.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 32f), null)
            }

            singleTarget.compoundDrawablePadding = StaticStore.dptopx(16f, this@SearchFilter)

            for (i in rareid.indices)
                rarities[i] = findViewById(rareid[i])

            for (i in atkid.indices) {
                attacks[i] = findViewById(atkid[i])

                if (i < atkid.size - 1) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                        attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 40f), null)
                    else
                        attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 32f), null)

                    attacks[i]?.compoundDrawablePadding = StaticStore.dptopx(8f, this@SearchFilter)
                }
            }

            abilityList.isNestedScrollingEnabled = false

            abilAdapter = SearchAbilityAdapter(this@SearchFilter, abils)
            abilAdapter.setHasStableIds(true)

            traitAdapter = SearchTraitAdapter(this@SearchFilter, generateTraitToolTip(), generateTraitArray())
            traitAdapter.setHasStableIds(true)

            abilityList.layoutManager = LinearLayoutManager(this@SearchFilter)
            abilityList.adapter = abilAdapter

            traitList.layoutManager = LinearLayoutManager(this@SearchFilter)
            traitList.adapter = traitAdapter

            traitOr.isChecked = true
            attackTypeOr.isChecked = true
            abilityOr.isChecked = true

            checker()

            listeners()

            onBackPressedDispatcher.addCallback(this@SearchFilter, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val back = findViewById<FloatingActionButton>(R.id.schbck)

                    back.performClick()
                }
            })

            StaticStore.setAppear(scrollLayout, title, stat, reset)
            StaticStore.setDisappear(status, progress)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    private fun listeners() {
        val back = findViewById<FloatingActionButton>(R.id.schbck)
        val reset = findViewById<FloatingActionButton>(R.id.schreset)
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val tgor = findViewById<RadioButton>(R.id.schrdtgor)
        val atkor = findViewById<RadioButton>(R.id.schrdatkor)
        val abor = findViewById<RadioButton>(R.id.schrdabor)
        val chnp = findViewById<CheckBox>(R.id.schnp)
        val tggroup = findViewById<RadioGroup>(R.id.schrgtg)
        val atkgroupor = findViewById<RadioGroup>(R.id.schrgatkor)
        val abgroup = findViewById<RadioGroup>(R.id.schrgab)
        val atkmu = findViewById<RadioButton>(R.id.schrdatkmu)
        val stat = findViewById<FloatingActionButton>(R.id.eschstat)

        back.setOnClickListener { returner() }

        reset.setOnClickListener {
            StaticStore.filterReset()
            atkgroup!!.clearCheck()
            tgor!!.isChecked = true
            atkor!!.isChecked = true
            abor!!.isChecked = true
            chnp!!.isChecked = false

            for (rarity1 in rarities) {
                if (rarity1!!.isChecked)
                    rarity1.isChecked = false
            }

            for (attack1 in attacks) {
                if (attack1!!.isChecked)
                    attack1.isChecked = false
            }

            traitAdapter.updateList()
            traitAdapter.notifyDataSetChanged()

            abilAdapter.updateList()
            abilAdapter.notifyDataSetChanged()
        }

        tggroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.tgorand = checkedId == tgor!!.id }
        atkgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.atksimu = checkedId == atkmu!!.id }
        atkgroupor.setOnCheckedChangeListener { _, checkedId -> StaticStore.atkorand = checkedId == atkor!!.id }
        abgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.aborand = checkedId == abor!!.id }

        for (i in rarities.indices) {
            rarities[i]!!.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.rare.add(rarity[i])
                else
                    StaticStore.rare.remove(rarity[i])
            }
        }

        for (i in attacks.indices) {
            attacks[i]!!.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.attack.add(atks[i])
                else
                    StaticStore.attack.remove(atks[i])
            }
        }

        chnp.setOnCheckedChangeListener { _, isChecked ->
            StaticStore.talents = isChecked
        }

        stat.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@SearchFilter, StatSearchFilter::class.java)
                intent.putExtra("unit", true)

                startActivity(intent)
            }
        })
    }

    private fun returner() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val result = Intent()

        StaticStore.empty = atkgroup!!.checkedRadioButtonId == -1

        setResult(Activity.RESULT_OK, result)

        finish()
    }

    private fun checker() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val atkgroupor = findViewById<RadioGroup>(R.id.schrgatkor)
        val tggroup = findViewById<RadioGroup>(R.id.schrgtg)
        val abgroup = findViewById<RadioGroup>(R.id.schrgab)
        val chnp = findViewById<CheckBox>(R.id.schnp)

        if (!StaticStore.empty)
            atkgroup.check(R.id.schrdatkmu)

        if (!StaticStore.atksimu && !StaticStore.empty)
            atkgroup.check(R.id.schrdatksi)

        if (!StaticStore.atkorand)
            atkgroupor.check(R.id.schrdatkand)

        if (!StaticStore.tgorand)
            tggroup.check(R.id.schrdtgand)

        if (!StaticStore.aborand)
            abgroup.check(R.id.schrdaband)

        for (i in rarity.indices)
            if (StaticStore.rare.contains(rarity[i]))
                rarities[i]?.isChecked = true

        for (i in atks.indices)
            if (StaticStore.attack.contains(atks[i]))
                attacks[i]?.isChecked = true

        if (StaticStore.talents)
            chnp?.isChecked = true
    }

    private fun getResizeDraw(id: Int, dp: Float): BitmapDrawable {
        val icon = StaticStore.img15?.get(id)?.bimg() ?: StaticStore.empty(this, dp, dp)
        val bd = BitmapDrawable(resources, StaticStore.getResizeb(icon as Bitmap, this, dp))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
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

    private fun generateTraitArray() : Array<Identifier<Trait>> {
        val traits = ArrayList<Identifier<Trait>>()

        for(i in 0 until 10) {
            traits.add(UserProfile.getBCData().traits.list[i].id)
        }

        for(userPack in UserProfile.getUserPacks()) {
            for(tr in userPack.traits.list) {
                tr ?: continue

                traits.add(tr.id)
            }
        }

        return traits.toTypedArray()
    }

    private fun generateTraitToolTip() : Array<String> {
        val tool = ArrayList<String>()

        for(i in tgToolID) {
            tool.add(getText(i).toString())
        }

        for(userPack in UserProfile.getUserPacks()) {
            for(tr in userPack.traits.list) {
                tr ?: continue

                tool.add(tr.name)
            }
        }

        return tool.toTypedArray()
    }
}