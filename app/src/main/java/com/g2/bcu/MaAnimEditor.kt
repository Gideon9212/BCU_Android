package com.g2.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.JsonParser
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.animation.adapter.MaAnimListAdapter
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.supports.DynamicListView
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Source
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.system.files.FDFile
import common.system.files.VFile
import common.util.anim.AnimCE
import common.util.anim.MaAnim
import common.util.anim.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.system.measureTimeMillis

@SuppressLint("ClickableViewAccessibility")
class MaAnimEditor : AppCompatActivity() {

    companion object {
        private var tempFunc : ((input: MaAnim, n : String) -> Unit)? = null
        private var tempFile : VFile? = null
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

                    if(name.endsWith(".txt") || name.endsWith(".mamodel")) {
                        val fl = File(StaticStore.getExternalPack(this), name)
                        if (fl.exists()) {
                            val imc = MaAnim.newIns(FDFile(fl), false)
                            tempFunc?.invoke(imc, name.substring(0, name.indexOf('.')))
                        }
                    }
                }
                cursor.close()
            }
        }
    }
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val data = result.data

            if(data != null) {
                val file = tempFile
                val uri = data.data

                if(uri == null || file == null) {
                    StaticStore.showShortMessage(this, getString(R.string.file_extract_cant))
                } else {
                    val pfd = contentResolver.openFileDescriptor(uri, "w")

                    if(pfd != null) {
                        val fos = FileOutputStream(pfd.fileDescriptor)
                        val ins = file.data.stream

                        val b = ByteArray(65536)
                        var len: Int
                        while(ins.read(b).also { len = it } != -1)
                            fos.write(b, 0, len)

                        ins.close()
                        fos.close()

                        val path = uri.path
                        if(path == null) {
                            StaticStore.showShortMessage(this, getString(R.string.file_extract_semi).replace("_",file.name))
                            return@registerForActivityResult
                        }

                        val f = File(path)
                        if(f.absolutePath.contains(":")) {
                            val p = f.absolutePath.split(":")[1]
                            StaticStore.showShortMessage(this,
                                getString(R.string.file_extract_success).replace("_", file.name)
                                    .replace("-", p))
                        } else
                            StaticStore.showShortMessage(this, getString(R.string.file_extract_semi).replace("_",file.name))
                    } else
                        StaticStore.showShortMessage(this, getString(R.string.file_extract_cant))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        setContentView(R.layout.activity_maanim_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val root = findViewById<ConstraintLayout>(R.id.maanimroot)
            val layout = findViewById<LinearLayout>(R.id.maanimlayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.maanimCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.maanimMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.maanimCfgHide)

            StaticStore.setDisappear(cfgBtn, layout)

            val res = JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), ResourceLocation::class.java) ?: return@launch
            val anim = if (res.pack == ResourceLocation.LOCAL)
                AnimCE.map()[res.id]
            else
                UserProfile.getUserPack(res.pack)?.source?.loadAnimation(res.id, res.base) as AnimCE?
            if (anim == null)
                return@launch
            anim.load()

            val cfgShowT = MaterialContainerTransform().apply {
                startView = cfgBtn
                endView = cfgMenu

                addTarget(endView!!)
                setPathMotion(MaterialArcMotion())
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            }
            val cfgHideT = MaterialContainerTransform().apply {
                startView = cfgMenu
                endView = cfgBtn

                addTarget(endView!!)
                createAnimator(root, TransitionValues(cfgMenu), TransitionValues(cfgBtn))
                setPathMotion(MaterialArcMotion())
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            }

            cfgBtn.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    TransitionManager.beginDelayedTransition(root, cfgShowT)
                    cfgBtn.visibility = View.GONE
                    cfgMenu.visibility = View.VISIBLE
                }
            })
            cfgHideBtn.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    TransitionManager.beginDelayedTransition(root, cfgHideT)
                    cfgBtn.visibility = View.VISIBLE
                    cfgMenu.visibility = View.GONE
                }
            })

            val viewer = AnimationEditView(this@MaAnimEditor, anim, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true)).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            viewer.id = R.id.animationView
            val scaleListener = ScaleListener(viewer)
            val detector = ScaleGestureDetector(this@MaAnimEditor, scaleListener)
            viewer.setOnTouchListener(object : OnTouchListener {
                var preid = -1
                var preX = 0f
                var preY = 0f

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    detector.onTouchEvent(event)
                    if (preid == -1)
                        preid = event.getPointerId(0)

                    val id = event.getPointerId(0)

                    val x = event.x
                    val y = event.y

                    if (event.action == MotionEvent.ACTION_DOWN) {
                        scaleListener.updateScale = true
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        if (event.pointerCount == 1 && id == preid) {
                            val dx = x - preX
                            val dy = y - preY

                            viewer.pos.x += dx
                            viewer.pos.y += dy
                            if (dx != 0f || dy != 0f)
                                viewer.invalidate()
                        }
                    }

                    preX = x
                    preY = y

                    preid = id

                    return true
                }
            })
            layout.addView(viewer)
            val list = findViewById<DynamicListView>(R.id.maanimvalList)
            val adp = MaAnimListAdapter(this@MaAnimEditor, anim)
            list.adapter = adp
            list.setSwapListener { from, to ->
                val p = getAnim(anim).parts
                val temp = p[from]
                p[from] = p[to]
                p[to] = temp
                anim.unSave("maanim sort")
            }

            val addl = findViewById<Button>(R.id.maanimpadd)
            addl.setOnClickListener {
                val ma = getAnim(anim)
                val ind : Int = ma.n
                val data: Array<Part> = ma.parts
                ma.parts = arrayOfNulls<Part>(++ma.n)
                if (ind >= 0) System.arraycopy(data, 0, ma.parts, 0, ind)
                if (data.size - ind >= 0)
                    System.arraycopy(data, ind, ma.parts, ind + 1, data.size - ind)

                val np = Part()
                np.validate()
                ma.parts[ind] = np
                ma.validate()
                unSave(anim,"maanim add line")
                adp.insert(np, ind)
                viewer.animationChanged()
            }
            val impr = findViewById<Button>(R.id.maanimimport)
            impr.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"

                tempFunc = fun(a : MaAnim, n : String) {
                    for (i in anim.types.indices)
                        if (anim.types[i].toString() == n) {
                            anim.anims[i] = a
                            anim.unSave("Import maanim")
                            if (anim.types[i] == viewer.getType()) {
                                adp.setTo(*a.parts)
                                viewer.animationChanged()
                            }
                            break
                        }
                }
                resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
            }
            val expr = findViewById<Button>(R.id.maanimexport)
            expr.setOnClickListener {
                anim.save()
                tempFile = VFile.getFile(CommonStatic.ctx.getWorkspaceFile(anim.id.path.substring(1) + "/maanim_${viewer.getType()}.txt"))
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*")
                intent.putExtra(Intent.EXTRA_TITLE, tempFile?.name ?: "")
                exportLauncher.launch(intent)
            }

            val anims = findViewById<Spinner>(R.id.maanimselect)
            val controller = findViewById<SeekBar>(R.id.maanimframeseek)
            val frame = findViewById<TextView>(R.id.maanimframe)

            StaticStore.frame = 0f
            frame.text = getString(R.string.anim_frame).replace("-", "" + StaticStore.frame)
            controller.progress = CommonStatic.fltFpsMul(StaticStore.frame).toInt()
            controller.max = CommonStatic.fltFpsMul(viewer.anim.len().toFloat()).toInt()
            if (anim.id.base == Source.BasePath.SOUL)
                anims.visibility = View.GONE
            else {
                anims.adapter = ArrayAdapter(this@MaAnimEditor, R.layout.spinneradapter, anim.rawNames())
                anims.setSelection(viewer.aind)
                anims.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (viewer.aind != position) {
                            viewer.aind = position
                            viewer.animationChanged()
                            adp.setTo(*getAnim(anim).parts)
                            controller.max = CommonStatic.fltFpsMul(viewer.anim.len().toFloat()).toInt()

                            controller.progress = 0
                            StaticStore.frame = 0f
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            controller.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(controller: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        StaticStore.frame = CommonStatic.fltFpsDiv(progress.toFloat())
                        viewer.anim.setTime(StaticStore.frame)
                    }
                }
                override fun onStartTrackingTouch(controller: SeekBar) {}
                override fun onStopTrackingTouch(controller: SeekBar) {}
            })
            val buttons = arrayOf<FloatingActionButton>(findViewById(R.id.animbackward), findViewById(R.id.animplay), findViewById(R.id.animforward))
            buttons[1].setOnClickListener {
                frame.setTextColor(StaticStore.getAttributeColor(this@MaAnimEditor, R.attr.TextPrimary))

                if (StaticStore.play) {
                    buttons[1].setImageDrawable(ContextCompat.getDrawable(this@MaAnimEditor, R.drawable.ic_play_arrow_black_24dp))
                    buttons[0].show()
                    buttons[2].show()
                    controller.isEnabled = true
                } else {
                    buttons[1].setImageDrawable(ContextCompat.getDrawable(this@MaAnimEditor, R.drawable.ic_pause_black_24dp))
                    buttons[0].hide()
                    buttons[2].hide()
                    controller.isEnabled = false
                }
                StaticStore.play = !StaticStore.play
            }
            buttons[0].setOnClickListener {
                buttons[2].isEnabled = false
                if (StaticStore.frame > 0) {
                    val f = CommonStatic.fltFpsDiv(1f)
                    StaticStore.frame -= f
                    viewer.anim.setTime(StaticStore.frame)
                } else {
                    frame.setTextColor(Color.rgb(227, 66, 66))
                    StaticStore.showShortMessage(this@MaAnimEditor, R.string.anim_warn_frame)
                }

                buttons[2].isEnabled = true
            }
            buttons[2].setOnClickListener {
                buttons[0].isEnabled = false
                val f = CommonStatic.fltFpsDiv(1f)
                StaticStore.frame += f
                viewer.anim.setTime(StaticStore.frame)
                frame.setTextColor(StaticStore.getAttributeColor(this@MaAnimEditor, R.attr.TextPrimary))
                buttons[0].isEnabled = true
            }

            val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
            val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
            undo.setOnClickListener {
                anim.undo()
                undo.visibility = if (anim.undo == "initial")
                    View.GONE
                else
                    View.VISIBLE
                redo.visibility = View.VISIBLE
                adp.setTo(*getAnim(anim).parts)
                viewer.animationChanged()
            }
            redo.setOnClickListener {
                anim.redo()
                redo.visibility = if (anim.getRedo() == "nothing")
                    View.GONE
                else
                    View.VISIBLE
                undo.visibility = View.VISIBLE
                adp.setTo(*getAnim(anim).parts)
                viewer.animationChanged()
            }
            undo.visibility = if (anim.undo == "initial")
                View.GONE
            else
                View.VISIBLE
            redo.visibility = if (anim.getRedo() == "nothing")
                View.GONE
            else
                View.VISIBLE

            val bck = findViewById<Button>(R.id.maanimexit)
            bck.setOnClickListener {
                anim.save()
                finish()
            }
            onBackPressedDispatcher.addCallback(this@MaAnimEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val imgcutBtn = findViewById<Button>(R.id.maan_imgcut_btn)
            imgcutBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, ImgCutEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.maan_mamodel_btn)
            maanimBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, MaModelEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val viewBtn = findViewById<Button>(R.id.maan_view_anim)
            viewBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, ImageViewer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())
                intent.putExtra("Img", ImageViewer.ViewerType.CUSTOM.name)

                startActivity(intent)
            }

            StaticStore.setAppear(cfgBtn, layout)
            activateAnims()
        }
    }

    private fun activateAnims() {
        StaticStore.play = true
        lifecycleScope.launch {
            val viewer = findViewById<AnimationEditView>(R.id.animationView)
            val controller = findViewById<SeekBar>(R.id.maanimframeseek)
            val targetFPS = 1000L / CommonStatic.fltFpsMul(30f).toLong()
            val frame = findViewById<TextView>(R.id.maanimframe)

            withContext(Dispatchers.IO) {
                while (true) {
                    if (!viewer.started)
                        continue

                    val time = measureTimeMillis {
                        viewer.postInvalidate()

                        withContext(Dispatchers.Main) {
                            frame.text = getText(R.string.anim_frame).toString().replace("-", "" + StaticStore.frame)
                        }

                        val maxValue = CommonStatic.fltFpsDiv(controller.max.toFloat())
                        controller.progress =
                            if (StaticStore.frame >= maxValue && StaticStore.play) {
                                StaticStore.frame = 0f
                                0
                            } else {
                                CommonStatic.fltFpsMul(StaticStore.frame).toInt()
                            }
                    }
                    delay(max(0, targetFPS - time))
                }
            }
        }
    }

    fun getAnim(a : AnimCE) : MaAnim {
        val viewer = findViewById<AnimationEditView>(R.id.animationView)
        return a.getMaAnim(viewer.getType())
    }

    fun unSave(a : AnimCE, str : String) {
        a.unSave(str)

        val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
        val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
        undo.visibility = View.VISIBLE
        redo.visibility = View.GONE
    }

    inner class ScaleListener(private val cView : AnimationEditView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.size *= detector.scaleFactor

            val diffX = (realFX - previousX) * (cView.size / previousScale - 1)
            val diffY = (realFY - previousY) * (cView.size / previousScale - 1)
            cView.pos.x = previousX - diffX
            cView.pos.y = previousY - diffY
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                realFX = detector.focusX - cView.width / 2f
                previousX = cView.pos.x

                realFY = detector.focusY - cView.height * 2f / 3f
                previousY = cView.pos.y

                previousScale = cView.size
                updateScale = false
            }
            return super.onScaleBegin(detector)
        }
    }
}