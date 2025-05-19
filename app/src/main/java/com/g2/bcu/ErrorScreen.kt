package com.g2.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.DataResetHandler
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic

class ErrorScreen : AppCompatActivity() {
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

        (CommonStatic.ctx as AContext).updateActivity(this)
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_error_screen)

        val bundle = intent.extras ?: return

        val reasonPhrase = bundle.getString("reasonPhrase", "")

        val solution = bundle.getString("solution", "")

        val errorCode = bundle.getString("errorCode", "")

        val reasonPhraseText = findViewById<TextView>(R.id.errphrase)
        val solutionText = findViewById<TextView>(R.id.errsuggestion)

        reasonPhraseText.text = reasonPhrase
        solutionText.text = solution

        val exit = findViewById<FloatingActionButton>(R.id.errexit)

        exit.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                finish()
            }
        })

        val solve = findViewById<Button>(R.id.errsolve)

        if (errorCode.isBlank())
            solve.visibility = View.GONE
        else
            solve.setOnClickListener(object: SingleClick() {
                override fun onSingleClick(v: View?) {
                    when (errorCode) {
                        StaticStore.ERR_ASSET -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)

                            intent.setData(uri)

                            startActivity(intent)
                            finish()
                        }
                        StaticStore.ERR_LANG -> {
                            val handler = DataResetHandler(getString(R.string.datareset_lang), getString(R.string.datareset_langreset), DataResetHandler.TYPE.LANG)

                            handler.performReset(this@ErrorScreen)

                            StaticStore.showShortMessage(this@ErrorScreen, R.string.datareset_restart)

                            finish()
                        }
                    }
                }
            })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exit.performClick()
            }
        })
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