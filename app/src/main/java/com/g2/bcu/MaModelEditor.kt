package com.g2.bcu

import android.annotation.SuppressLint
import android.app.Dialog
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
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.JsonParser
import com.g2.bcu.androidutil.LocaleManager
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.animation.AnimationEditView
import com.g2.bcu.androidutil.animation.adapter.MaModelListAdapter
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.supports.LeakCanaryManager
import com.g2.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.system.P
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.MaModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin


@SuppressLint("ClickableViewAccessibility")
class MaModelEditor : AppCompatActivity() {

    companion object {
        private var tempFunc : ((input: MaModel) -> Unit)? = null
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
                            val imc = MaModel.newIns(FDFile(fl))
                            tempFunc?.invoke(imc)
                        }
                    }
                }
                cursor.close()
            }
        }
    }
    enum class MOVEMODE {
        NONE,
        POSITION,
        PIVOT,
        ANGLE
    }
    var move = MOVEMODE.POSITION

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
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_mamodel_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val root = findViewById<ConstraintLayout>(R.id.mamodelroot)
            val layout = findViewById<LinearLayout>(R.id.mamodellayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.mamodelCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.mamodelMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.mamodelCfgHide)
            val modeSel = findViewById<Spinner>(R.id.mamodelmode)
            modeSel.adapter = ArrayAdapter(this@MaModelEditor, R.layout.spinneradapter, MOVEMODE.values())
            modeSel.setSelection(1)
            modeSel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    move = modeSel.adapter.getItem(position) as MOVEMODE
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            StaticStore.setDisappear(cfgBtn, layout)

            val res = JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), ResourceLocation::class.java) ?: return@launch
            val anim = if (res.pack == ResourceLocation.LOCAL)
                AnimCE.map()[res.id]
            else
                UserProfile.getUserPack(res.pack)?.source?.loadAnimation(res.id, res.base) as AnimCE?
            if (anim == null)
                return@launch
            anim.check()

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
            val list = findViewById<RecyclerView>(R.id.mamodelvalList)
            list.layoutManager = LinearLayoutManager(this@MaModelEditor)
            val adp = MaModelListAdapter(this@MaModelEditor, anim)
            list.adapter = adp

            val viewer = AnimationEditView(this@MaModelEditor, anim, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true)).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            viewer.id = R.id.animationView
            val touch = ItemTouchHelper(object: ItemTouchHelper.Callback() {
                var moved : Boolean = false

                override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
                }

                override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return current.itemViewType == target.itemViewType
                }

                override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                    val from = src.bindingAdapterPosition
                    val to = dest.bindingAdapterPosition
                    val s = anim.mamodel.strs0
                    val tempe = s[from]
                    s[from] = s[to]
                    s[to] = tempe

                    val p = anim.mamodel.parts
                    val temp = p[from]
                    p[from] = p[to]
                    p[to] = temp
                    for (i in p.indices)
                        if (p[i][0] == from || p[i][0] == to) {
                            p[i][0] = if (p[i][0] == from) to else from
                            adp.notifyItemChanged(i)
                        }
                    for (ma in anim.anims)
                        for (pt in ma.parts) {
                            if (pt.ints[0] == from)
                                pt.ints[0] = to
                            else if (pt.ints[0] == to)
                                pt.ints[0] = from
                        }
                    moved = true
                    viewer.animationChanged()
                    adp.notifyItemMoved(from, to)
                    (src as MaModelListAdapter.ViewHolder).setID(to, viewer)
                    (dest as MaModelListAdapter.ViewHolder).setID(from, viewer)
                    return false
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    // Action finished
                    if (moved)
                        unSave(anim,"mamodel sort")
                    moved = false
                }

                override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                    val pos = holder.bindingAdapterPosition
                    val parts = StringBuilder()
                    for (i in anim.mamodel.parts.indices)
                        if (anim.mamodel.parts[i][0] == pos)
                            parts.append("MaModel ").append(getString(R.string.def_part)).append(" $i (").append(anim.mamodel.strs0[i]).append(")\n")
                    for (i in anim.anims.indices)
                        for (part in anim.anims[i].parts)
                            if (part.ints[0] == pos) {
                                parts.append("MaAnim: ${anim.types[i]}").append("\n")
                                break
                            }
                    if (parts.isBlank())
                        adp.removePart(viewer, pos)
                    else {
                        val delPop = Dialog(this@MaModelEditor)
                        delPop.setContentView(R.layout.animation_part_delete_confirm)
                        delPop.setCancelable(true)

                        val parList = delPop.findViewById<TextView>(R.id.usedPartList)
                        parList.text = parts.toString()
                        val del = delPop.findViewById<Button>(R.id.part_delete_tree)
                        del.setOnClickListener {
                            adp.removePart(viewer, pos)
                            adp.notifyDataSetChanged()
                            delPop.dismiss()
                        }
                        val cancel = delPop.findViewById<Button>(R.id.part_nodelete)
                        cancel.setOnClickListener {
                            delPop.dismiss()
                            adp.notifyItemChanged(pos)
                        }
                        if (!isDestroyed && !isFinishing)
                            delPop.show()
                    }
                }
            })
            touch.attachToRecyclerView(list)

            val scaleListener = ScaleListener(viewer, anim)
            val detector = ScaleGestureDetector(this@MaModelEditor, scaleListener)
            viewer.setOnTouchListener(object : OnTouchListener {
                var preid = -1
                var spriteSelected = false
                var partMoved = false
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
                        spriteSelected = false
                        if (move != MOVEMODE.NONE) {
                            if (viewer.anim.sele != -1) {
                                val r = viewer.getPartRect(viewer.anim.sele)
                                if (!r.inBox(x, y)) {
                                    viewer.anim.sele = -1
                                    viewer.invalidate()
                                }
                            } else {
                                val mo = anim.mamodel
                                for (i in 0 until mo.n) {
                                    val r = viewer.getPartRect(i)
                                    if (r.inBox(x, y)) {
                                        viewer.anim.sele = i
                                        scaleListener.setModel(mo.parts[i])
                                        list.smoothScrollToPosition(i)
                                        spriteSelected = true
                                        viewer.invalidate()
                                        break
                                    }
                                }
                            }
                        }
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        if (event.pointerCount == 1 && id == preid) {
                            var dx = x - preX
                            var dy = y - preY

                            if (move == MOVEMODE.NONE || viewer.anim.sele == -1) {
                                viewer.pos.x += dx
                                viewer.pos.y += dy
                                if (dx != 0f || dy != 0f)
                                    viewer.invalidate()
                            } else if (move == MOVEMODE.ANGLE) {
                                val part = anim.mamodel.parts[viewer.anim.sele]
                                val ps = viewer.getPartPos(viewer.anim.sele)
                                val sB = atan2(preY - ps.y, preX - ps.x)
                                val sA = atan2(y - ps.y, x - ps.x)
                                part[10] += ((sA - sB) * 1800 / Math.PI).toInt()
                                part[10] %= 3600
                                viewer.animationChanged()
                                adp.notifyItemChanged(viewer.anim.sele)
                            } else {
                                val mo = anim.mamodel
                                val movePivot = move == MOVEMODE.PIVOT
                                val scale: P = viewer.realScale(mo.parts, mo.parts[viewer.anim.sele], !movePivot)
                                dx /= viewer.size / scale.x
                                dy /= viewer.size / scale.y

                                val angle: Double = viewer.getAngle(mo.parts, mo.parts[viewer.anim.sele], !movePivot) / 1800.0 * Math.PI
                                val sin = sin(angle)
                                val cos = cos(angle)
                                if (!movePivot || viewer.anim.sele == 0) {
                                    dx *= -1
                                    dy *= -1
                                }
                                partMoved = partMoved || dx.toInt() != 0 || dy.toInt() != 0
                                anim.mamodel.parts[viewer.anim.sele][if (movePivot || viewer.anim.sele == 0) 6 else 4] -= ((dx * cos) + (dy * sin)).toInt()
                                anim.mamodel.parts[viewer.anim.sele][if (movePivot || viewer.anim.sele == 0) 7 else 5] -= ((dy * cos) + (dx * sin)).toInt()
                                if (dx != 0f || dy != 0f) {
                                    viewer.animationChanged()
                                    adp.notifyItemChanged(viewer.anim.sele)
                                }
                            }
                        }
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        if (partMoved) {
                            partMoved = false
                            unSave(anim, "mamodel move part ${viewer.anim.sele}")
                        } else if (move == MOVEMODE.ANGLE) {
                            unSave(anim, "mamodel rotate part ${viewer.anim.sele}")
                        } else if (!spriteSelected) {
                            var selected = -1
                            val mo = anim.mamodel
                            for (i in 0 until mo.n) {
                                if (viewer.anim.sele == i)
                                    continue
                                val r = viewer.getPartRect(i)
                                if (r.inBox(x, y)) {
                                    selected = i
                                    break
                                }
                            }
                            viewer.anim.sele = selected
                            if (selected != -1)
                                list.smoothScrollToPosition(selected)
                            viewer.invalidate()
                        } else if (move != MOVEMODE.NONE && viewer.anim.sele != -1 && scaleListener.scaled())
                            unSave(anim,"mamodel scale part ${viewer.anim.sele}")
                    }

                    preX = x
                    preY = y

                    preid = id

                    return true
                }
            })
            layout.addView(viewer)

            val addl = findViewById<Button>(R.id.mamodelpadd)
            addl.setOnClickListener {
                val ind = max(1, if (viewer.anim.sele == -1) anim.mamodel.n else viewer.anim.sele)
                anim.addMMline(ind, 0)
                unSave(anim,"initial")
                viewer.animationChanged()
                adp.notifyItemInserted(ind)
                adp.notifyDataSetChanged()
                //for (i in man.findFirstVisibleItemPosition()..man.findLastVisibleItemPosition())
                //    adp.notifyItemChanged(i)
                //for (i in max(ind, man.findFirstVisibleItemPosition())..man.findLastVisibleItemPosition())
                //    (list.getChildViewHolder(list.getChildAt(i)) as MaModelListAdapter.ViewHolder).iid.text = (i+1).toString()
            }
            val impr = findViewById<Button>(R.id.mamodelimport)
            impr.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"

                tempFunc = fun(f : MaModel) {
                    anim.mamodel = f
                    unSave(anim,"Import mamodel")
                    viewer.animationChanged()
                    adp.notifyDataSetChanged()
                }
                resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
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
                viewer.animationChanged()
                adp.notifyDataSetChanged()
            }
            undo.setOnLongClickListener {
                StaticStore.showShortMessage(this@MaModelEditor, anim.undo)
                false
            }
            undo.visibility = if (anim.undo == "initial")
                View.GONE
            else
                View.VISIBLE
            redo.setOnClickListener {
                anim.redo()
                redo.visibility = if (anim.getRedo() == "nothing")
                    View.GONE
                else
                    View.VISIBLE
                undo.visibility = View.VISIBLE
                viewer.animationChanged()
                adp.notifyDataSetChanged()
            }
            redo.setOnLongClickListener {
                StaticStore.showShortMessage(this@MaModelEditor, anim.getRedo())
                false
            }
            redo.visibility = if (anim.getRedo() == "nothing")
                View.GONE
            else
                View.VISIBLE

            val bck = findViewById<Button>(R.id.mamodelexit)
            bck.setOnClickListener {
                anim.save()
                anim.unload()
                finish()
            }
            onBackPressedDispatcher.addCallback(this@MaModelEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val imgcutBtn = findViewById<Button>(R.id.mamo_imgcut_btn)
            imgcutBtn.setOnClickListener {
                anim.check()
                anim.save()
                val intent = Intent(this@MaModelEditor, ImgCutEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.mamo_maanim_btn)
            maanimBtn.setOnClickListener {
                anim.check()
                anim.save()
                val intent = Intent(this@MaModelEditor, MaAnimEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val viewBtn = findViewById<Button>(R.id.mamo_view_anim)
            viewBtn.setOnClickListener {
                anim.check()
                anim.save()
                val intent = Intent(this@MaModelEditor, ImageViewer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())
                intent.putExtra("Img", ImageViewer.ViewerType.CUSTOM.name)

                startActivity(intent)
            }

            StaticStore.setAppear(cfgBtn, layout)
        }
    }

    fun unSave(a : AnimCE, str : String) {
        a.unSave(str)

        val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
        val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
        undo.visibility = View.VISIBLE
        redo.visibility = View.GONE
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    inner class ScaleListener(private val cView : AnimationEditView, private val anim : AnimCE) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (move != MOVEMODE.NONE && cView.anim.sele != -1) {
                val mo = anim.mamodel
                mo.parts[cView.anim.sele][9] = (mo.parts[cView.anim.sele][9] * detector.scaleFactor).toInt()
                mo.parts[cView.anim.sele][10] = (mo.parts[cView.anim.sele][10] * detector.scaleFactor).toInt()
            } else {
                cView.size *= detector.scaleFactor

                val diffX = (realFX - previousX) * (cView.size / previousScale - 1)
                val diffY = (realFY - previousY) * (cView.size / previousScale - 1)
                cView.pos.x = previousX - diffX
                cView.pos.y = previousY - diffY
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                if (move != MOVEMODE.NONE && cView.anim.sele != -1) {
                    setModel(anim.mamodel.parts[cView.anim.sele])
                } else {
                    realFX = detector.focusX - cView.width / 2f
                    previousX = cView.pos.x

                    realFY = detector.focusY - cView.height * 2f / 3f
                    previousY = cView.pos.y
                }
                previousScale = cView.size
                updateScale = false
            }
            return super.onScaleBegin(detector)
        }

        fun setModel(model : IntArray) {
            realFX = model[9].toFloat()
            realFY = model[10].toFloat()
        }

        fun scaled() : Boolean {
            val model = anim.mamodel.parts[cView.anim.sele]
            return model[9] != realFX.toInt() || model[10] != realFY.toInt()
        }
    }
}