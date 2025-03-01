package com.g2.bcu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.animation.adapter.AnimationListAdapter
import com.g2.bcu.androidutil.fakeandroid.FIBM
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.pack.Source
import common.pack.Source.ResourceLocation
import common.util.AnimGroup
import common.util.anim.AnimCE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException


class AnimationManagement : AppCompatActivity() {

    companion object {
        var tempFunc : ((input: Bitmap) -> Unit)? = null
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val path = result.data?.data ?: return@registerForActivityResult

            Log.i("AnimationManagement", "Got URI : $path")
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            val resolver = applicationContext.contentResolver
            val cursor = try {
                resolver.query(path, projection, null, null, null)
            } catch (_: SecurityException) {
                StaticStore.showShortMessage(this, R.string.pack_import_denied)
                return@registerForActivityResult
            } catch (_: FileNotFoundException) {
                StaticStore.showShortMessage(this, R.string.pack_import_nofile)
                return@registerForActivityResult
            } catch (_: IllegalArgumentException) {
                StaticStore.showShortMessage(this, R.string.pack_import_nofile)
                return@registerForActivityResult
            }

            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    val name = cursor.getString(0) ?: return@registerForActivityResult

                    if(!name.endsWith(".png") && !name.endsWith(".jpg")) {
                        StaticStore.showShortMessage(this, R.string.image_import_invalid)
                        return@registerForActivityResult
                    }

                    val input = this.contentResolver.openInputStream(path)
                    val img = BitmapFactory.decodeStream(input, null, BitmapFactory.Options())
                    input!!.close()
                    if (img == null)
                        return@registerForActivityResult
                    tempFunc?.invoke(img)
                }
                cursor.close()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else if (!shared.getBoolean("theme", false))
            setTheme(R.style.AppTheme_night)
        else
            setTheme(R.style.AppTheme_day)

        LeakCanaryManager.initCanary(shared, application)
        DefineItf.check(this)
        AContext.check()
        (CommonStatic.ctx as AContext).updateActivity(this)
        setContentView(R.layout.activity_anim_management)

        lifecycleScope.launch {
            //Prepare
            val swipe = findViewById<SwipeRefreshLayout>(R.id.animmanrefresh)
            val list = findViewById<ListView>(R.id.animmanlist)
            val more = findViewById<FloatingActionButton>(R.id.animmanoption)
            val bck = findViewById<FloatingActionButton>(R.id.animmanbck)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(list, swipe, more)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(
                    this@AnimationManagement,
                    { _ -> },
                    { t -> runOnUiThread { st.text = t } })
            }

            //Load UI
            more.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    getImage(fun(img : Bitmap) {
                        val dialog = Dialog(this@AnimationManagement)
                        dialog.setContentView(R.layout.animation_type_selection)
                        dialog.setCancelable(true)

                        val exGroup = dialog.findViewById<RadioGroup>(R.id.antypegroup)
                        val cont = dialog.findViewById<Button>(R.id.antypecontinue)
                        val cancel = dialog.findViewById<Button>(R.id.antypecancel)

                        val opts = intArrayOf(R.string.anim_type_default, R.string.anim_type_soul, R.string.anim_type_bgeff)
                        for(o in opts.indices) {
                            val radioButton = RadioButton(this@AnimationManagement)
                            radioButton.id = opts[o]
                            radioButton.setTextColor(StaticStore.getAttributeColor(this@AnimationManagement, R.attr.TextPrimary))
                            radioButton.text = getString(opts[o])

                            exGroup.addView(radioButton)
                            radioButton.isChecked = o == 0
                        }

                        cont.setOnClickListener(object : SingleClick() {
                            override fun onSingleClick(v: View?) {
                                val ind = exGroup.indexOfChild(exGroup.findViewById(exGroup.checkedRadioButtonId))
                                if(ind >= 0 && ind < opts.size) {
                                    addAnimation(img, ind)
                                    dialog.dismiss()
                                } else {
                                    dialog.dismiss()
                                }
                            }
                        })
                        cancel.setOnClickListener(object : SingleClick() {
                            override fun onSingleClick(v: View?) {
                                dialog.dismiss()
                            }
                        })
                        if (!this@AnimationManagement.isDestroyed && !this@AnimationManagement.isFinishing) {
                            dialog.show()
                        }
                    })
                }
            })

            val aList = ArrayList<AnimCE>(AnimCE.map().values)
            list.adapter = AnimationListAdapter(this@AnimationManagement, aList)

            bck.setOnClickListener {
                val intent = Intent(this@AnimationManagement, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            onBackPressedDispatcher.addCallback(this@AnimationManagement, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            StaticStore.setDisappear(st, prog)
            StaticStore.setAppear(list, more, swipe)
        }
    }

    fun addAnimation(img: Bitmap, selection : Int) {
        val rl: ResourceLocation = when (selection) {
            2 -> ResourceLocation(ResourceLocation.LOCAL, "new bgeffect anim", Source.BasePath.BGEffect)
            1 -> ResourceLocation(ResourceLocation.LOCAL, "new soul anim", Source.BasePath.SOUL)
            else -> ResourceLocation(ResourceLocation.LOCAL, "new anim", Source.BasePath.ANIM)
        }
        Source.Workspace.validate(rl)
        Source.warn = false
        val ac = AnimCE(rl)
        Source.warn = true
        ac.num = FIBM(img)
        ac.saveImg()
        ac.createNew()
        AnimCE.map()[rl.id] = ac
        AnimGroup.workspaceGroup.renewGroup()
        (findViewById<ListView>(R.id.animmanlist).adapter as AnimationListAdapter).notifyDataSetChanged()
    }

    fun getImage(func: (f: Bitmap) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "*/*"

        tempFunc = func
        resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
    }
}