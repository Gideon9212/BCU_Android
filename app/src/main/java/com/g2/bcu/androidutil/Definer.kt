package com.g2.bcu.androidutil

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.g2.bcu.R
import com.g2.bcu.androidutil.battle.sound.SoundHandler
import com.g2.bcu.androidutil.fakeandroid.BMBuilder
import com.g2.bcu.androidutil.io.AContext
import com.g2.bcu.androidutil.io.AssetException
import com.g2.bcu.androidutil.io.DefineItf
import com.g2.bcu.androidutil.io.ErrorLogWriter
import com.g2.bcu.androidutil.io.LangLoader
import com.g2.bcu.androidutil.io.LanguageException
import com.g2.bcu.androidutil.pack.PackConflict
import common.CommonStatic
import common.io.assets.AssetLoader
import common.pack.PackData
import common.pack.Source.Workspace
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import common.system.files.VFile
import common.util.Data
import common.util.anim.ImgCut
import common.util.lang.ProcLang
import common.util.stage.Replay
import java.io.File
import java.io.IOException
import java.util.function.Consumer

object Definer {
    /** Value which tells if file paths are added to memory  */
    private var init : Boolean = false
    private var animRead : Boolean = false
    /** Value which tells if app already read BC data or not */
    private var bcRead = false
    /** Value which tells if app already read pack or not */
    var packRead : Boolean = false

    /** Value which tells if Unit language data is loaded  */
    var unitlang = false
    /** Value which tells if Enemy language data is loaded  */
    var enemylang = false
    /** Value which tells if Stage language data is loaded  */
    var stagelang = false
    /** Value which tells if Medal language data is loaded  */
    var medallang = false

    @Synchronized
    fun init(context: Context, text: Consumer<String>) {
        if (init)
            return
        StaticStore.clear()
        val shared2 = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        StaticStore.setLang(shared2.getInt("Language", 0))
        ImageBuilder.builder = BMBuilder()
        DefineItf().init(context)
        AContext.check()
        CommonStatic.ctx.initProfile()

        text.accept(context.getString(R.string.main_file_read))
        init = true
    }
    @Synchronized
    fun define(context: Context, prog: Consumer<Double>, text: Consumer<String>) {
        try {
            init(context, text)
            if (!bcRead) {
                try {
                    AssetLoader.load(prog)
                    println("Read asset loader properly\n${VFile.getBCFileTree().list()}")

                    UserProfile.getBCData().load({ t -> text.accept(StaticStore.getLoadingText(context, t)) }, prog)
                    bcRead = true
                } catch (e: Exception) {
                    throw AssetException(e, "E/Definer::define - Failed to read asset")
                }
            }

            if(!packRead) {
                text.accept(context.getString(R.string.main_pack))
                (CommonStatic.ctx as AContext).prog = prog
                (CommonStatic.ctx as AContext).sprg = text
                UserProfile.loadPacks(true)
                PackConflict.filterConflict()

                packRead = true

                val f = File(StaticStore.getExternalPath(context)+"pack/")

                if(f.exists()) {
                    text.accept(context.getString(R.string.main_rev_reformat))

                    StaticStore.deleteFile(f, true)
                }

                val g = File(StaticStore.getExternalRes(context))

                if(g.exists()) {
                    text.accept(context.getString(R.string.main_rev_reformat))

                    StaticStore.deleteFile(g, true)
                }

                handlePacks(context)
                Replay.getMap().clear()
                Replay.read()
            }

            text.accept(context.getString(R.string.load_add))
            prog.accept(-1.0/10000.0)

            if (StaticStore.treasure == null)
                StaticStore.readTreasureIcon()

            try {
                if (!unitlang)
                    LangLoader.readUnitLang(context)
                if(!enemylang)
                    LangLoader.readEnemyLang(context)
                if(!stagelang)
                    LangLoader.readStageLang(context)
            } catch (e: NumberFormatException) {
                throw LanguageException(e, "E/Definer::define - failed to read language")
            }

            if (StaticStore.fruit == null) {
                val path1 = "./org/page/catfruit/"

                val names = arrayOf(
                    "gatyaitemD_30_f.png", "gatyaitemD_31_f.png", "gatyaitemD_32_f.png",
                    "gatyaitemD_33_f.png", "gatyaitemD_34_f.png", "gatyaitemD_35_f.png",
                    "gatyaitemD_36_f.png", "gatyaitemD_37_f.png", "gatyaitemD_38_f.png",
                    "gatyaitemD_39_f.png", "gatyaitemD_40_f.png", "gatyaitemD_41_f.png",
                    "gatyaitemD_42_f.png", "gatyaitemD_43_f.png", "gatyaitemD_44_f.png",
                    "gatyaitemD_160_f.png", "gatyaitemD_161_f.png", "gatyaitemD_164_f.png",
                    "gatyaitemD_167_f.png", "gatyaitemD_168_f.png", "gatyaitemD_169_f.png",
                    "gatyaitemD_170_f.png", "gatyaitemD_171_f.png", "gatyaitemD_179_f.png",
                    "gatyaitemD_180_f.png", "gatyaitemD_181_f.png", "gatyaitemD_182_f.png",
                    "gatyaitemD_183_f.png", "gatyaitemD_184_f.png", "xp.png"
                )

                StaticStore.fruit = Array(names.size) {i ->
                    val vf = VFile.get(path1+names[i]) ?: return@Array StaticStore.empty()

                    val icon = vf.data?.img?.bimg() ?: return@Array StaticStore.empty()

                    icon as Bitmap
                }
            }

            if(StaticStore.starDifficulty == null) {
                val starPath = "./org/page/img102"

                val starVF = VFile.get("$starPath.png")

                if(starVF != null) {
                    val starImg = ImgCut.newIns("$starPath.imgcut")

                    val starPng = starVF.data.img

                    val images = starImg.cut(starPng)

                    StaticStore.starDifficulty = arrayOf(images[33].bimg() as Bitmap, images[34].bimg() as Bitmap)
                }
            }
            if (StaticStore.img15 == null)
                StaticStore.readImg()

            if (StaticStore.sicons == null) {
                StaticStore.sicons = Array(StaticStore.siinds.size) { i ->
                    (StaticStore.img15?.get(StaticStore.siinds[i])?.bimg() ?: StaticStore.empty()) as Bitmap
                }
            }

            StaticStore.allMCs.addAll(StaticStore.BCMapCodes)
            val packs = UserProfile.getUserPacks()
            for(p in packs)
                if(p.mc.maps.list.isNotEmpty())
                    StaticStore.allMCs.add(p.mc.sid)

            if(StaticStore.medalnumber == 0) {
                val vf = VFile.get("./org/page/medal")

                val lit = vf.list()

                if(lit != null)
                    StaticStore.medalnumber = lit.size
            }
            if(SoundHandler.play.isEmpty())
                SoundHandler.play = BooleanArray(UserProfile.getBCData().musics.list.size)

            text.accept(context.getString(R.string.load_process))
        } catch (e: IOException) {
            e.printStackTrace()

            ErrorLogWriter.writeLog(e)
        }
    }
    fun defineAnimations(context: Context, prog: Consumer<Double>, text: Consumer<String>) {
        init(context, text)
        if (animRead)
            return
        Workspace.loadAnimations(null)
        animRead = true
    }
    fun defineMedals(context: Context, text: Consumer<String>) {
        init(context, text)
        try {
            if(StaticStore.medalnumber != 0 && !medallang) {
                LangLoader.readMedalLang(context)
            }
        } catch (e: NumberFormatException) {
            throw LanguageException(e, "E/Definer::define - failed to read language")
        }
    }

    fun redefine(context: Context) {
        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        StaticStore.setLang(shared.getInt("Language", 0))

        ProcLang.clear()
    }

    fun handlePacks(c: Context) {
        val shared = c.getSharedPreferences(StaticStore.PACK, Context.MODE_PRIVATE)
        val editor = shared.edit()
        val checked = ArrayList<String>()

        for(p in UserProfile.getUserPacks()) {
            p ?: continue
            if(!shared.contains(p.sid)) {
                //New Pack detected
                editor.putString(p.sid, "${p.desc.names} - ${p.desc.version}")
                editor.apply()
                extractMusic(p, shared)
            } else {
                val info = shared.getString(p.sid, "")
                val newInfo = "${p.desc.names} - ${p.desc.version}"

                if(info != newInfo) {
                    //Update detected
                    editor.putString(p.sid, "${p.desc.names} - ${p.desc.version}")
                    editor.apply()
                    extractMusic(p, shared)
                }
            }
            checked.add(p.sid)
        }

        //Check if unchecked pack ID in shared preferences

        val notExisting = ArrayList<String>()
        for(any in shared.all) {
            if(any.value !is String)
                continue
            val value = any.key

            if(realNotExisting(notExisting, checked, value)) {
                if(value.contains("-")) {
                    val info = value.split("-")

                    if(info.size != 2) {
                        Log.w("Definer::extractMusic", "Invalid music file format : $info")

                        continue
                    } else
                        CommonStatic.parseIntN(info[1])
                } else
                    notExisting.add(value)
            }
        }

        Log.i("Definer::handlePacks", "Nonexistent pack list : $notExisting")
        //Then perform removing music files
        for(p in notExisting)
            for(key in shared.all.keys)
                if(key.startsWith(p)) {
                    if(key.contains("-")) {
                        val f = File(StaticStore.dataPath+"music/", key)

                        if(f.exists())
                            f.delete()
                        else
                            Log.i("Definer::handlePacks", "??? File doesn't exist : ${f.absolutePath}")
                    }
                    editor.remove(key)
                    editor.apply()
                }
    }

    fun initializeConfiguration(shared: SharedPreferences, c: Context) {
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            initializeAsset(c)
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", false)
            ed.putBoolean("frame", true)
            ed.putBoolean("apktest", false)
            ed.putInt("default_level", 50)
            ed.putInt("Language", 0)
            ed.putInt("Orientation", 0)
            ed.putBoolean("Lay_Port", true)
            ed.putBoolean("Lay_Land", true)
            ed.apply()
        }
        if (!shared.contains("apktest")) {
            ed.putBoolean("apktest", true)
            ed.apply()
        }

        if (!shared.contains("default_level")) {
            ed.putInt("default_level", 50)
            ed.apply()
        }

        if (!shared.contains("apktest")) {
            ed.putBoolean("apktest", false)
            ed.apply()
        }

        if (!shared.contains("Language")) {
            ed.putInt("Language", 0)
            ed.apply()
        }

        if (!shared.contains("frame")) {
            ed.putBoolean("frame", true)
            ed.apply()
        }

        if (shared.contains("Orientation")) {
            ed.remove("Orientation")
            ed.apply()
        }

        if (!shared.contains("Lay_Port")) {
            ed.putBoolean("Lay_Port", true)
            ed.apply()
        }

        if (!shared.contains("Lay_Land")) {
            ed.putBoolean("Lay_Land", true)
            ed.apply()
        }

        if (shared.contains("Skip_Text")) {
            ed.remove("Skip_Text")
            ed.apply()
        }

        if (!shared.contains("music")) {
            ed.putBoolean("music", true)
            ed.apply()
        }

        if (!shared.contains("mus_vol")) {
            ed.putInt("mus_vol", 99)
            ed.apply()
        }

        if (!shared.contains("SE")) {
            ed.putBoolean("SE", true)
            ed.apply()
        }

        if (!shared.contains("se_vol")) {
            ed.putInt("se_vol", 99)
            ed.apply()
        }

        if (!shared.contains("DEV_MODE")) {
            ed.putBoolean("DEV_MODE", false)
            ed.apply()
        }

        if (!shared.contains("Announce_0.13.0")) {
            ed.putBoolean("Announce_0.13.0", false)
            ed.apply()
        }

        if (!shared.contains("PackReset0137")) {
            ed.putBoolean("PackReset0137", false)
            ed.apply()
        }

        if(!shared.contains("Reformat0150")) {
            ed.putBoolean("Reformat0150", false)
            ed.apply()
        }

        if(!shared.contains("UI")) {
            ed.putBoolean("UI", true)
            ed.apply()
        }

        if(!shared.contains("ui_vol")) {
            ed.putInt("ui_vol", 99)
            ed.apply()
        }

        if(!shared.contains("gif")) {
            ed.putInt("gif", 100)
            ed.apply()
        }

        if(!shared.contains("rowlayout")) {
            ed.putBoolean("rowlayout", true)
            ed.apply()
        }

        if(!shared.contains("levelLimit")) {
            ed.putInt("levelLimit", 0)
            ed.apply()
        }

        if(!shared.contains("unlockPlus")) {
            ed.putBoolean("unlockPlus", true)
            ed.apply()
        }

        if(!shared.getBoolean("PackReset0137", false)) {
            ed.putBoolean("PackReset0137", true)
            ed.apply()

            deleter(File(StaticStore.getExternalRes(c)))
        }

        if(!shared.contains("bgeff")) {
            ed.putBoolean("bgeff", true)
            ed.apply()
        }

        if(!shared.contains("unitDelay")) {
            ed.putBoolean("unitDelay", true)
            ed.apply()
        }

        if(!shared.contains("viewerColor")) {
            ed.putInt("viewerColor", -1)
            ed.apply()
        }

        if(!shared.contains("exContinue")) {
            ed.putBoolean("exContinue", true)
            ed.apply()
        }

        if(!shared.contains("realEx")) {
            ed.putBoolean("realEx", false)
            ed.apply()
        }

        if(!shared.contains("shake")) {
            ed.putBoolean("shake", true)
            ed.apply()
        }

        if(!shared.contains("showst")) {
            ed.putBoolean("showst", true)
            ed.apply()
        }

        if(!shared.contains("showres")) {
            ed.putBoolean("showres", true)
            ed.apply()
        }

        if(!shared.contains("reallv")) {
            ed.putBoolean("reallv", false)
            ed.apply()
        }

        if(!shared.contains("lazylineup")) {
            ed.putBoolean("lazylineup", false)
            ed.apply()
        }

        SoundHandler.musicPlay = shared.getBoolean("music", true)
        SoundHandler.mu_vol = if(shared.getBoolean("music", true)) {
            0.01f + shared.getInt("mus_vol", 99) / 100f
        } else {
            0f
        }
        SoundHandler.sePlay = shared.getBoolean("SE", true)
        SoundHandler.se_vol = if(shared.getBoolean("SE", true)) {
            (0.01f + shared.getInt("se_vol", 99) / 100f) * 0.85f
        } else {
            0f
        }
        SoundHandler.uiPlay = shared.getBoolean("UI", true)
        SoundHandler.ui_vol = if(SoundHandler.uiPlay)
            (0.01f + shared.getInt("ui_vol", 99) / 100f) * 0.85f
        else
            0f

        StaticStore.showResult = shared.getBoolean("showres", true)

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
        CommonStatic.getConfig().realLevel = shared.getBoolean("reallv", false)
        CommonStatic.getConfig().deadOpa = 0
        CommonStatic.getConfig().fullOpa = 100
        CommonStatic.getConfig().fps60 = shared.getBoolean("fps60", false)
        CommonStatic.getConfig().prog = shared.getBoolean("prog", false)
    }

    private fun extractMusic(p: PackData.UserPack, shared: SharedPreferences) {
        if(p.musics.list.isEmpty())
            return

        val editor = shared.edit()

        if(CommonStatic.ctx == null || CommonStatic.ctx !is AContext)
            return

        for(m in p.musics.list) {
            val f = (CommonStatic.ctx as AContext).getMusicFile(m)

            if(!f.exists()) {
                val result = StaticStore.extractMusic(m, f)

                Log.i("Definer::extractMusic", "Created new music file : ${result.absolutePath} | ${result.exists()}")

                editor.putString(result.name, StaticStore.fileToMD5(result))
            } else {
                val md5 = shared.getString(f.name, "")

                val newMD5 = StaticStore.streamToMD5(m.data.stream)

                if(md5 != newMD5) {
                    Log.i("Deinfer::extractMusic","New MD5 detected : ID = ${m.id}, MD5 = $md5, newMD5 = $newMD5")

                    val result = StaticStore.extractMusic(m, f)

                    editor.putString(result.name, newMD5)
                }
            }
        }

        editor.apply()

        //Handle deleted music
        //Get music file list of specified pack first

        val mList = ArrayList<File>()

        val fList = File(StaticStore.dataPath+"music/").listFiles() ?: return

        for(f in fList) {
            if(f.name.startsWith("${p.sid}-"))
                mList.add(f)
        }

        //Then compare if specified music file is existing in pack too

        for(m in mList) {
            val info = m.name.split("-")

            if(info.size != 2) {
                Log.w("Definer::extractMusic", "Invalid music file format : $info")

                continue
            }

            val music = p.musics.getRaw(CommonStatic.parseIntN(info[1]))

            if(music == null) {
                Log.i("Definer::extractMusic", "Deleted music : ${m.absolutePath}")

                m.delete()

                editor.remove(m.name)
            }
        }

        editor.apply()
    }

    private fun realNotExisting(notExisting: ArrayList<String>, checked: ArrayList<String>, target: String) : Boolean {
        for(p in checked) {
            if(target.startsWith(p))
                return false
        }

        for(u in notExisting) {
            if(target.startsWith(u))
                return false
        }

        return true
    }

    private fun initializeAsset(c: Context) {
        var f = File(StaticStore.getExternalAsset(c))

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(c)+"assets/")

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(c)+"lang/")

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(c)+"music/")

        if(!f.exists())
            f.mkdirs()
    }

    private fun deleter(f: File) {
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return

            for (g in lit)
                deleter(g)

            f.delete()
        } else
            f.delete()
    }
}