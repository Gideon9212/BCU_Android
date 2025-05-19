package com.g2.bcu.androidutil.io

import android.content.Context
import android.media.MediaMetadataRetriever
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.battle.sound.SoundHandler
import com.g2.bcu.androidutil.music.OggDataSource
import common.CommonStatic
import common.CommonStatic.Itf
import common.pack.Identifier
import common.util.stage.Music
import common.util.unit.AbForm
import common.util.unit.Level
import java.io.File

class DefineItf : Itf {
    companion object {
        var dir: String = ""

        fun check(c: Context) {
            if(dir == "") {
                dir = StaticStore.getExternalPath(c)
            }
        }
    }

    override fun save(save: Boolean, genBackup: Boolean, exit: Boolean) {}

    override fun getMusicLength(f: Music?): Long {
        f ?: return -1

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(OggDataSource(f.data))

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: -1
    }

    @Deprecated("Deprecated in Java")
    override fun route(path: String?): File {
        val realPath = path?.replace("./",dir) ?: ""

        return File(realPath)
    }

    override fun setSE(ind: Int) {
        SoundHandler.setSE(ind)
    }

    override fun setSE(mus: Identifier<Music>?) {
        mus ?: return

        SoundHandler.setSE(mus)
    }

    override fun setBGM(mus: Identifier<Music>?) {
        mus ?: return

        SoundHandler.setBGM(mus)
    }

    override fun getUILang(m: Int, s: String?): String {
        return "${m} / ${s}"
    }

    override fun lvText(f: AbForm?, lv: Level?): Array<String> {
        TODO("Not yet implemented")
    }

    fun init(c: Context) {
        dir = StaticStore.getExternalPath(c)

        CommonStatic.def = this
    }
}