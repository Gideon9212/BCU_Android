package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.battle.sound.SoundHandler
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmField
        var isRunning = false

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
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

        deleter(File(Environment.getDataDirectory().absolutePath+"/data/com.g2.bcu/temp/"))
        deleter(File(StaticStore.getExternalTemp(this)))

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter("${StaticStore.getPublicDirectory()}logs"))
        setContentView(R.layout.activity_main)

        SoundHandler.musicPlay = shared.getBoolean("music", true)
        SoundHandler.mu_vol = if(shared.getBoolean("music", true)) {
            0.01f + shared.getInt("mus_vol", 99) / 100f
        } else {
            0.5f
        }
        SoundHandler.sePlay = shared.getBoolean("SE", true)
        SoundHandler.se_vol = if(shared.getBoolean("SE", true)) {
            (0.01f + shared.getInt("se_vol", 99) / 100f) * 0.85f
        } else {
            0.5f
        }
        SoundHandler.uiPlay = shared.getBoolean("UI", true)
        SoundHandler.ui_vol = if(SoundHandler.uiPlay)
            (0.01f + shared.getInt("ui_vol", 99)) * 0.85f
        else
            0.5f
        StaticStore.upload = shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)
        CommonStatic.getConfig().twoRow = shared.getBoolean("rowlayout", true)
        CommonStatic.getConfig().levelLimit = shared.getInt("levelLimit", 0)
        CommonStatic.getConfig().plus = shared.getBoolean("unlockPlus", true)
        CommonStatic.getConfig().drawBGEffect = shared.getBoolean("bgeff", true)
        CommonStatic.getConfig().buttonDelay = shared.getBoolean("unitDelay", true)
        CommonStatic.getConfig().viewerColor = shared.getInt("viewerColor", -1)
        CommonStatic.getConfig().exContinuation = shared.getBoolean("exContinue", true)
        CommonStatic.getConfig().realEx = shared.getBoolean("realEx", false)
        CommonStatic.getConfig().shake = shared.getBoolean("shake", true)
        CommonStatic.getConfig().stageName = shared.getBoolean("showst", true)
        StaticStore.showResult = shared.getBoolean("showres", true)
        CommonStatic.getConfig().realLevel = shared.getBoolean("reallv", false)
        CommonStatic.getConfig().deadOpa = 0
        CommonStatic.getConfig().fullOpa = 100
        CommonStatic.getConfig().fps60 = shared.getBoolean("fps60", false)
        CommonStatic.getConfig().prog = shared.getBoolean("prog", false)

        isRunning = true

        val grid = findViewById<GridLayout>(R.id.maingrid)

        val drawables = intArrayOf(R.drawable.ic_kasa_jizo, R.drawable.ic_enemy, R.drawable.ic_castle,
                R.drawable.ic_medal, R.drawable.ic_basis, R.drawable.ic_bg, R.drawable.ic_castles,
                R.drawable.ic_music, R.drawable.ic_effect, R.drawable.ic_pack, R.drawable.ic_baseline_folder_24,
                R.drawable.ic_kasa_jizo, R.drawable.ic_pack)//TODO - Anim Page icon

        val classes = arrayOf(AnimationViewer::class.java, EnemyList::class.java, MapList::class.java,
                MedalList::class.java, LineUpScreen::class.java, BackgroundList::class.java, CastleList::class.java,
                MusicList::class.java, EffectList::class.java, PackManagement::class.java, AssetBrowser::class.java,
                AnimationManagement::class.java, PackCreation::class.java)

        val texts = intArrayOf(R.string.main_unitinfo,R.string.main_enemy_info, R.string.stg_inf,
                R.string.main_medal, R.string.main_equip, R.string.main_bg, R.string.main_castle,
                R.string.main_music, R.string.main_effect, R.string.main_packs, R.string.main_asset,
                R.string.main_animation, R.string.main_cus_pack)

        val row = 8
        val col = 2 // unit/enemy | stage,medal | basis | bg,castles | music,effect | pack | asset | custom anim/custom pack

        val gap = StaticStore.dptopx(4f, this)

        val w = StaticStore.getScreenWidth(this, false) - StaticStore.dptopx(32f, this) - gap * 4

        grid.rowCount = row
        grid.columnCount = col

        for(i in 0 until row) {
            if(i == 2 || i == 5 || i == 6) {
                var index = i * 2
                if(i > 2)
                    index--
                if(i > 5)
                    index--

                val card = CardView(this)

                val r = GridLayout.spec(i, 1)
                val c = GridLayout.spec(0 ,2)

                val gParam = GridLayout.LayoutParams(r, c)

                gParam.setMargins(gap, gap, gap, gap)

                card.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val v = LayoutInflater.from(this).inflate(R.layout.main_card_layout, card, false)

                val layout = v.findViewById<ConstraintLayout>(R.id.cardlayout)

                val lParam = layout.layoutParams

                lParam.width = w + gap * 2
                lParam.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                layout.layoutParams = lParam

                val text = v.findViewById<TextView>(R.id.cardname)
                val img = v.findViewById<ImageView>(R.id.cardimg)

                text.setText(texts[index])
                img.setImageDrawable(ContextCompat.getDrawable(this, drawables[index]))

                card.addView(v)

                card.isClickable = true

                card.radius = StaticStore.dptopx(8f, this).toFloat()

                card.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(this@MainActivity, classes[index])

                        startActivity(intent)

                        if(classes[index] == PackManagement::class.java) {
                            finish()
                        }
                    }
                })

                grid.addView(card, gParam)
            } else {
                for(j in 0 until col) {
                    var index = i * 2 + j
                    if(i > 2)
                        index--
                    if(i > 6)
                        index -= 2

                    val card = CardView(this)

                    val r = GridLayout.spec(i, 1)
                    val c = GridLayout.spec(j ,1)

                    val gParam = GridLayout.LayoutParams(r, c)

                    gParam.setMargins(gap, gap, gap, gap)

                    card.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val v = LayoutInflater.from(this).inflate(R.layout.main_card_layout, card, false)

                    val layout = v.findViewById<ConstraintLayout>(R.id.cardlayout)

                    val lParam = layout.layoutParams

                    lParam.width = w / 2
                    lParam.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                    layout.layoutParams = lParam

                    val text = v.findViewById<TextView>(R.id.cardname)
                    val img = v.findViewById<ImageView>(R.id.cardimg)

                    text.setText(texts[index])
                    img.setImageDrawable(ContextCompat.getDrawable(this, drawables[index]))

                    card.addView(v)

                    card.isClickable = true

                    card.radius = StaticStore.dptopx(8f, this).toFloat()

                    card.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            val intent = Intent(this@MainActivity, classes[index])

                            startActivity(intent)

                            if(classes[index] == PackManagement::class.java) {
                                finish()
                            }
                        }
                    })

                    grid.addView(card, gParam)
                }
            }
        }

        val config = findViewById<FloatingActionButton>(R.id.mainconfig)

        config.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, ConfigScreen::class.java)

                startActivity(intent)
                finish()
            }
        })

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isRunning = false

                StaticStore.dialogisShowed = false

                StaticStore.clear()
                finish()
            }
        })
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
        isRunning = false

        StaticStore.dialogisShowed = false
        StaticStore.toast = null

        super.onDestroy()
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun deleter(f: File) {
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return
            for (g in lit)
                deleter(g)
        } else
            f.delete()
    }

    private fun safeCheck(f: File, suffix: String = "", maxSize: Int = -1): Boolean {
        if (suffix.isNotBlank()) {
            val name = f.name
            if (!name.endsWith(suffix))
                return false
        }
        if (maxSize == -1)
            return true

        val size = f.length()
        val mb = size / 1024 / 1024
        return mb <= maxSize
    }
}