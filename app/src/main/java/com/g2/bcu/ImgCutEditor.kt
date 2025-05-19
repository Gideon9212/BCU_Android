package com.g2.bcu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.g2.bcu.androidutil.animation.SpriteView
import com.g2.bcu.androidutil.animation.adapter.ImgcutListAdapter
import com.g2.bcu.androidutil.fakeandroid.FIBM
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
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.ImgCut
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class ImgCutEditor : AppCompatActivity() {

    companion object {
        private var tempFunc : ((input: Any) -> Unit)? = null
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

                    if(name.endsWith(".png") || name.endsWith(".jpg")) {
                        val input = this.contentResolver.openInputStream(path)
                        val img = BitmapFactory.decodeStream(input, null, BitmapFactory.Options())
                        input!!.close()
                        if (img == null)
                            return@registerForActivityResult
                        tempFunc?.invoke(img)
                    } else if(name.endsWith(".txt") || name.endsWith(".imgcut")) {
                        val fl = File(StaticStore.getExternalPack(this), name)
                        if (fl.exists()) {
                            val imc = ImgCut.newIns(FDFile(fl))
                            tempFunc?.invoke(imc)
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
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
        setContentView(R.layout.activity_imgcut_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val root = findViewById<ConstraintLayout>(R.id.imgcutroot)
            val layout = findViewById<LinearLayout>(R.id.imgcutlayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.imgCutMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgHide)

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
            val list = findViewById<RecyclerView>(R.id.imgcutvalList)
            list.layoutManager = LinearLayoutManager(this@ImgCutEditor)
            val adp = ImgcutListAdapter(this@ImgCutEditor, anim)
            list.adapter = adp
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
                    val s = anim.imgcut.strs
                    val tempe = s[from]
                    s[from] = s[to]
                    s[to] = tempe

                    val c = anim.imgcut.cuts
                    val temp = c[from]
                    c[from] = c[to]
                    c[to] = temp
                    for (p in anim.mamodel.parts) {
                        if (p[2] == from)
                            p[2] = to
                        else if (p[2] == to)
                            p[2] = from
                    }
                    for (ma in anim.anims)
                        for (pt in ma.parts)
                            if (pt.ints[1] == 2)
                                for (mov in pt.moves) {
                                    if (mov[1] == from)
                                        mov[1] = to
                                    else if (mov[1] == to)
                                        mov[1] = from
                                }
                    moved = true
                    val voo = findViewById<SpriteView>(R.id.spriteView)
                    if (voo.sele == from)
                        voo.sele = to
                    else if (voo.sele == to)
                        voo.sele = from
                    adp.notifyItemMoved(from, to)
                    (src as ImgcutListAdapter.ViewHolder).setID(to, voo)
                    (dest as ImgcutListAdapter.ViewHolder).setID(from, voo)
                    return false
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    // Action finished
                    if (moved)
                        unSave(anim,"imgcut sort")
                    moved = false
                }

                override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                    val pos = holder.bindingAdapterPosition
                    val parts = StringBuilder()
                    for (i in anim.mamodel.parts.indices)
                        if (anim.mamodel.parts[i][2] == pos)
                            parts.append("MaModel ").append(getString(R.string.def_part)).append(" $i (").append(anim.mamodel.strs0[i]).append(")\n")

                    for (i in anim.anims.indices)
                        for (part in anim.anims[i].parts) {
                            var animd = false
                            if (part.ints[1] == 2)
                                for (mov in part.moves)
                                    if (mov[1] == pos) {
                                        parts.append("MaAnim: ${anim.types[i]}").append("\n")
                                        animd = true
                                        break
                                    }
                            if (animd)
                                break
                        }
                    if (parts.isBlank())
                        removePart(findViewById(R.id.spriteView), anim, adp, pos)
                    else {
                        val delPop = Dialog(this@ImgCutEditor)
                        delPop.setContentView(R.layout.animation_part_delete_confirm)
                        delPop.setCancelable(true)

                        val parList = delPop.findViewById<TextView>(R.id.usedPartList)
                        parList.text = parts.toString()
                        val del = delPop.findViewById<Button>(R.id.part_delete_tree)
                        del.setOnClickListener {
                            removePart(findViewById(R.id.spriteView), anim, adp, pos)
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

            val viewer = SpriteView(this@ImgCutEditor, anim).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                postMove = fun(sav : String) {
                    if (sav.isBlank()) {
                        adp.notifyItemChanged(sele) //if (list.size > sele) //    (list.getChildViewHolder(list[sele]) as ImgcutListAdapter.ViewHolder).setData(anim.imgcut.cuts[sele])
                    } else
                        unSave(anim, sav)
                }
                selectionChanged = fun() {
                    if (sele != -1)
                        list.smoothScrollToPosition(sele)
                }
            }
            viewer.id = R.id.spriteView
            layout.addView(viewer)

            val addl = findViewById<Button>(R.id.imgcutpadd)
            val impr = findViewById<Button>(R.id.imgcutimport)
            val spri = findViewById<Button>(R.id.imgcutsprimp)
            val resi = findViewById<Button>(R.id.imgcutresize)
            addl.setOnClickListener {
                anim.imgcut.addLine(viewer.sele)
                adp.notifyItemInserted(anim.imgcut.n - 1)
                viewer.sele = anim.imgcut.n - 1
                unSave(anim, "imgcut add line")
                viewer.invalidate()
                adp.notifyDataSetChanged()
            }
            impr.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f !is ImgCut)
                        return
                    anim.imgcut = f
                    adp.notifyDataSetChanged()
                    unSave(anim,"Import imgcut")
                    viewer.invalidate()
                })
            }
            spri.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f !is Bitmap)
                        return
                    anim.num = FIBM(f)
                    anim.saveImg()
                    anim.reloImg()
                    viewer.calculateSize(true)
                })
            }

            resi.setOnClickListener {
                val dialog = Dialog(this@ImgCutEditor)
                dialog.setContentView(R.layout.create_setlu_dialog)
                val edit = dialog.findViewById<EditText>(R.id.setluedit)
                val done = dialog.findViewById<Button>(R.id.setludone)
                val cancel = dialog.findViewById<Button>(R.id.setlucancel)
                val tbar = dialog.findViewById<TextView>(R.id.setluname)
                tbar.setText(R.string.anim_resizep)
                edit.hint = "100.0%"
                val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(this@ImgCutEditor, R.attr.TextPrimary))
                edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))
                edit.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                done.setOnClickListener {
                    val siz = CommonStatic.parseDoubleN(edit.text.ifBlank { edit.hint }.toString())
                    if (siz > 0) {
                        anim.resize(siz / 100)
                        unSave(anim, "initial")
                        viewer.invalidate()
                    }
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
                if (!isDestroyed && !isFinishing) {
                    dialog.show()
                }
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
                adp.notifyDataSetChanged()
                viewer.invalidate()
            }
            undo.setOnLongClickListener {
                StaticStore.showShortMessage(this@ImgCutEditor, anim.undo)
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
                adp.notifyDataSetChanged()
                viewer.invalidate()
            }
            redo.setOnLongClickListener {
                StaticStore.showShortMessage(this@ImgCutEditor, anim.getRedo())
                false
            }
            redo.visibility = if (anim.getRedo() == "nothing")
                View.GONE
            else
                View.VISIBLE

            val bck = findViewById<Button>(R.id.imgcutexit)
            bck.setOnClickListener {
                anim.save()
                anim.unload()
                finish()
            }
            onBackPressedDispatcher.addCallback(this@ImgCutEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val mamodelBtn = findViewById<Button>(R.id.imgc_mamodel_btn)
            mamodelBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, MaModelEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.imgc_maanim_btn)
            maanimBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, MaAnimEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val viewBtn = findViewById<Button>(R.id.imgc_view_anim)
            viewBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, ImageViewer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())
                intent.putExtra("Img", ImageViewer.ViewerType.CUSTOM.name)

                startActivity(intent)
            }

            StaticStore.setAppear(cfgBtn, layout)
        }
    }

    private fun getImport(func: (f: Any) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "*/*"

        tempFunc = func
        resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
    }

    fun unSave(a : AnimCE, str : String) {
        a.unSave(str)

        val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
        val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
        undo.visibility = View.VISIBLE
        redo.visibility = View.GONE
    }

    private fun removePart(voo : SpriteView, a : AnimCE, adp : ImgcutListAdapter, pos : Int) {
        a.removeICline(pos)
        adp.notifyItemRemoved(pos)
        unSave(a, "imgcut remove part")
        voo.invalidate()
        adp.notifyDataSetChanged()
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.attachBaseContext(this, newBase)

        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }
}