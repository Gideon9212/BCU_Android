package com.g2.bcu.androidutil.io

import android.app.Activity
import android.util.Log
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.pack.PackConflict
import common.CommonStatic
import common.io.Backup
import common.io.PackLoader
import common.io.assets.Admin
import common.io.assets.UpdateCheck
import common.pack.Context
import common.pack.Identifier
import common.util.Data
import common.util.stage.Music
import java.io.File
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.function.Consumer

class AContext : Context {
    companion object {
        fun check() {
            if(CommonStatic.ctx == null)
                CommonStatic.ctx = AContext()
        }
    }

    private val stopper = Object()

    var c: WeakReference<Activity?>? = null

    fun updateActivity(a: Activity) {
        c = WeakReference(a)

        synchronized(stopper) {
            stopper.notifyAll()
        }
    }

    override fun noticeErr(e: Exception, t: Context.ErrType, str: String) {
        Log.e("AContext", str)
        e.printStackTrace()

        if(str.contains("err during formatting"))
            return

        if(str.contains("failed to load external pack")) {
            val path = str.split("/")

            val list = ArrayList<String>()

            list.add(path[path.size-1])

            PackConflict(PackConflict.ID_CORRUPTED, list, true)
        } else if(str.contains("failed to load pack ")) {
            val packDescription = str.split(" - ")

            val list = ArrayList<String>()

            list.add(packDescription[packDescription.size - 1])

            PackConflict(PackConflict.ID_CORRUPTED, list, true)
        }

        val wac = c

        if(wac == null) {
            ErrorLogWriter.writeDriveLog(e)
            return
        }

        val a = wac.get()

        if(a == null) {
            if(t == Context.ErrType.FATAL || t == Context.ErrType.ERROR)
                ErrorLogWriter.writeDriveLog(e)

            return
        }

        if(t == Context.ErrType.FATAL || t == Context.ErrType.ERROR)
            ErrorLogWriter.writeLog(e)


    }

    override fun getWorkspaceFile(relativePath: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalWorkspace(a)+relativePath)
    }

    override fun getBackupFile(string: String?): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalBackup(a) + string)
    }

    override fun getBCUFolder(): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalPath(a))
    }

    override fun newFile(path: String?): File {
        if (path == null)//This will never be true but Kotlin won't shut up about it
            return File("")
        return File(bcuFolder.name + path)
    }

    override fun getAuthor(): String {
        return ""
    }

    override fun getAssetFile(string: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalAsset(a)+string)
    }

    override fun getAuxFile(string: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalPath(a)+string)
    }

    override fun initProfile() {
        UpdateCheck.addRequiredAssets("090901")
    }

    override fun getUserFile(string: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(StaticStore.getExternalUser(a)+string)
    }

    override fun confirm(str: String?): Boolean {
        return false //Only used for autosave
    }

    override fun confirmDelete(f: File?): Boolean {
        return true
    }

    override fun printErr(t: Context.ErrType?, str: String?) {
        if(t != null) {
            if(t == Context.ErrType.DEBUG || t == Context.ErrType.INFO || t == Context.ErrType.NEW) {
                Log.i("AContext", str ?: "")
            } else if(t == Context.ErrType.CORRUPT || t == Context.ErrType.WARN) {
                Log.w("AContext", str ?: "")

                val msg = str ?: return

                if(msg.contains(" has same ID with ")) {
                    val info = msg.split(" has same ID with")

                    if(info.size == 2) {
                        val list = ArrayList<String>()

                        list.add(info[0])
                        list.add(info[1])

                        PackConflict(PackConflict.ID_SAME_ID, list, true)
                    }
                } else if(msg.contains(" core version ")) {
                    val info = msg.replace("Pack ","").replace(" core version (", "\\").replace(") is higher than BCU", "").replace(")", "").split("\\")

                    val list = ArrayList<String>()

                    list.add(info[0])
                    list.add(info[1])

                    PackConflict(PackConflict.ID_UNSUPPORTED_CORE_VERSION, list, true)
                } else if(msg.contains("parent packs ")) {
                    val p = msg.split(") requires")[0]

                    var op = ""

                    for(i in p.length -1 downTo 0) {
                        if(p[i] == '(')
                            break
                        else
                            op += p[i]
                    }

                    op = op.reversed() + ".pack.bcuzip"

                    val info = msg.split(": ")[1]

                    val list = ArrayList<String>()

                    list.add(op)
                    list.add(info)

                    PackConflict(PackConflict.ID_PARENT, list, msg.contains("parent packs "))
                }

            } else if(t == Context.ErrType.ERROR || t == Context.ErrType.FATAL) {
                Log.e("AContext", str ?: "")
            }
        }
    }

    lateinit var prog : Consumer<Double>
    lateinit var sprg : Consumer<String>
    override fun loadProg(d: Double, str: String?) {
        prog.accept(d)
        if (str != null)
            sprg.accept(str)
    }

    override fun restore(b: Backup?, prog: Consumer<Double>?): Boolean {
        return false
    }

    override fun getLangFile(file: String): InputStream? {
        val wac = c ?: return null

        val a = wac.get() ?: return null

        if(file.startsWith("animation_type") && file.endsWith(".json"))
            return a.resources.openRawResource(R.raw.animation_type)
        return a.resources.openRawResource(R.raw.proc)
    }

    override fun preload(desc: PackLoader.ZipDesc.FileDesc): Boolean {
        if(desc.path.contains(".ogg"))
            return false

        return Admin.preload(desc)
    }

    fun getMusicFile(m: Music) : File {
        synchronized(stopper) {
            while(c == null) {
                stopper.wait()
            }
        }
        val wac = c ?: return File("")
        val a = wac.get() ?: return File("")

        return if(m.id.pack == Identifier.DEF) {
            File(StaticStore.getExternalAsset(a)+"music/"+ Data.trio(m.id.id)+".ogg")
        } else {
            File(StaticStore.dataPath+"music/"+m.id.pack+"-"+Data.trio(m.id.id)+".ogg")
        }
    }
}