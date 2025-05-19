package com.g2.bcu.androidutil

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import common.CommonStatic
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskEntity
import common.battle.data.MaskUnit
import common.pack.Identifier
import common.pack.SortedPackSet
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.Limit
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.stage.info.DefStageInfo
import common.util.unit.Character
import common.util.unit.Enemy
import common.util.unit.Form
import common.util.unit.Level
import common.util.unit.Trait
import java.text.DecimalFormat
import kotlin.math.roundToInt

class GetStrings(private val c: Context) {
    companion object {
        private val talData = intArrayOf(
            -1, //0: ??
            R.string.sch_abi_we, //1: Weaken
            R.string.sch_abi_fr, //2: Freeze
            R.string.sch_abi_sl, //3: Slow
            R.string.sch_abi_ao, //4: Attacks Only
            R.string.sch_abi_st, //5: Strong
            R.string.sch_abi_re, //6: Resistant
            R.string.sch_abi_md, //7: Massive Damage
            R.string.sch_abi_kb, //8: Knockback
            R.string.sch_abi_wa, //9: Warp
            R.string.sch_abi_str, //10: Strengthen
            R.string.sch_abi_su, //11: Survive
            R.string.sch_abi_bd, //12: Base Destroyer
            R.string.sch_abi_cr, //13: Critical
            R.string.sch_abi_zk, //14: Zombie Killer
            R.string.sch_abi_bb, //15: Barrier Breaker
            R.string.sch_abi_em, //16: Extra Money
            R.string.sch_abi_wv, //17: Wave
            R.string.talen_we, //18: Res. Weaken
            R.string.talen_fr, //19: Res. Freeze
            R.string.talen_sl, //20: Res. Slow
            R.string.talen_kb, //21: Res. Knockback
            R.string.talen_wv, //22: Res. Wave
            R.string.sch_abi_ws, //23: Wave Shield
            R.string.talen_warp, //24: Res. Warp
            R.string.unit_info_cost, //25: Cost
            R.string.unit_info_cd, //26: Cooldown
            R.string.unit_info_spd, //27: Speed
            R.string.hb, //28: ??
            R.string.sch_abi_ic, //29: Imu. Curse
            R.string.talen_cu, //30: Res. Curse
            R.string.unit_info_atk, //31: Attack Damage
            R.string.unit_info_hp, //32: HP
            R.string.sch_red, //33: Red Trait
            R.string.sch_fl, //34: Float Trait
            R.string.sch_bla, //35: Black Trait
            R.string.sch_me, //36: Metal Trait
            R.string.sch_an, //37: Angel Trait
            R.string.sch_al, //38: Alien Trait
            R.string.sch_zo, //39: Zombie Trait
            R.string.sch_re, //40: Relic Trait
            R.string.sch_wh, //41: White Trait
            R.string.esch_witch, //42: ??
            R.string.esch_eva, //43: ??
            R.string.sch_abi_iw, //44: Imu. Weaken
            R.string.sch_abi_if, //45: Imu. Freeze
            R.string.sch_abi_is, //46: Imu. Slow
            R.string.sch_abi_ik, //47: Imu. Knockback
            R.string.sch_abi_iwv, //48: Imu. Wave
            R.string.sch_abi_iwa, //49: Imu. Warp
            R.string.sch_abi_sb, //50: Savage Blow
            R.string.sch_abi_iv, //51: Invincibility
            R.string.talen_poi, //52: Res. Poison
            R.string.abi_ipoi, //53: Imu. Poison
            R.string.talen_sur, //54: Res. Surge
            R.string.sch_abi_imsu, //55: Imu. Surge
            R.string.sch_abi_surge, //56: Surge Attack,
            R.string.sch_de, //57: Aku
            R.string.sch_abi_shb, //58: Shield breaker
            R.string.sch_abi_ck, //59: Corpse killer
            R.string.sch_abi_cu, //60: Curse
            R.string.unit_info_tba, //61: TBA
            R.string.sch_abi_mw, //62: Mini Wave
            R.string.sch_abi_bk, //63: Colossus Slayer
            R.string.sch_abi_bh, //64: Behemoth Hunter
            R.string.sch_abi_ms, //65: Mini Surge
            R.string.sch_abi_sh, //66: Super Sage Slayer
            R.string.sch_abi_expl //67: BAJA BLAST
        )
        private val cusTalent = intArrayOf(
            -1,
            R.string.abi_bu,
            R.string.abi_rev,
            R.string.enem_info_barrier,
            R.string.sch_abi_ds,
            R.string.sch_abi_sd,
            R.string.sch_abi_cs,
            R.string.abi_seal,
            R.string.sch_abi_cou,
            R.string.sch_abi_cut,
            R.string.sch_abi_cap,
            R.string.sch_abi_rms,
            R.string.abi_armbr,
            R.string.abi_hast,
            R.string.sch_abi_rg,
            R.string.sch_abi_hy,
            R.string.abi_imcri,
            R.string.sch_abi_imusm,
            R.string.abi_iseal,
            R.string.sch_abi_imar,
            R.string.sch_abi_imsp,
            R.string.sch_abi_imlt,
            R.string.sch_abi_imr,
            R.string.sch_abi_imh,
            R.string.unit_info_rang,
            R.string.sch_abi_poi,
            R.string.abi_ipoi,
            R.string.sch_abi_imr,
            R.string.sch_abi_imh,
            R.string.abi_isnk,
            R.string.abi_istt,
            R.string.abi_ithch,
            R.string.abi_iboswv,
            R.string.sch_abi_ltg,
            R.string.abi_snk,
            R.string.abi_boswv,
            R.string.abi_stt,
            R.string.abi_imvatk,
            R.string.sch_abi_waur,
            R.string.sch_abi_saur,
            R.string.sch_abi_st,
            R.string.sch_abi_re,
            R.string.sch_abi_tps,
            R.string.sch_abi_ss,
            R.string.abi_drn,
            R.string.sch_abi_speedup,
            R.string.sch_abi_rfn,
            R.string.sch_abi_msd
        )
        private lateinit var talTool: Array<String>
        private val mapcolcid = arrayOf("N", "S", "C", "CH", "E", "T", "V", "R", "M", "A", "B", "RA", "H", "CA", "Q", "L", "ND", "SR", "G")
    }

    init {
        talTool = Array(talData.size) { i ->
            if(talData[i] == -1)
                return@Array "Invalid"
            c.getString(talData[i])
        }
    }

    fun getTitle(f: Form?): String {
        if (f == null)
            return ""

        val result = StringBuilder()

        val name = MultiLangCont.get(f) ?: f.names.toString()

        val rarity: String = when (f.unit.rarity) {
            0 -> c.getString(R.string.sch_rare_ba)
            1 -> c.getString(R.string.sch_rare_ex)
            2 -> c.getString(R.string.sch_rare_ra)
            3 -> c.getString(R.string.sch_rare_sr)
            4 -> c.getString(R.string.sch_rare_ur)
            5 -> c.getString(R.string.sch_rare_lr)
            else -> "Unknown"
        }
        return if (name == "") rarity else result.append(rarity).append(" - ").append(name).toString()
    }

    fun getAtkTime(f: Form?, talent: Boolean, frame: Boolean, lvs: Level, index : Int): String {
        if (f == null || index >= f.du.atkTypeCount) return ""
        val du = if(f.du.pCoin != null && talent) f.du.pCoin.improve(lvs.talents) else f.du
        return if (frame) du.getItv(index).toString() + "f" else DecimalFormat("#.##").format(du.getItv(0).toDouble() / 30) + "s"
    }
    fun getAtkTime(em: Enemy?, frame: Boolean, index : Int): String {
        if (em == null || index >= em.de.atkTypeCount) return ""
        return if (frame) em.de.getItv(index).toString() + "f" else DecimalFormat("#.##").format(em.de.getItv(index).toDouble() / 30) + "s"
    }

    fun getAbilT(ch: Character?, index: Int): String {
        if (ch == null) return ""
        val atks = StaticJava.getAtkModel(ch.mask, index)
        val result = StringBuilder()
        for (i in atks.indices) {
            result.append(getBoolean(atks[i].canProc()))
            if (i < atks.size - 1)
                result.append(" / ")
        }//TODO: Remove this
        return result.toString()
    }

    fun getPost(c: Character?, frame: Boolean, index : Int): String {
        if (c == null || index >= c.mask.atkTypeCount) return ""
        return if (frame) c.mask.getPost(false, index).toString() + "f" else DecimalFormat("#.##").format(c.mask.getPost(false, index).toDouble() / 30) + "s"
    }

    fun getTBA(f: Form?, talent: Boolean, frame: Boolean, lvs: Level): String {
        if (f == null) return ""
        val du = if(f.du.pCoin != null && talent) f.du.pCoin.improve(lvs.talents) else f.du
        return getTBA(du, frame)
    }
    fun getTBA(em: Enemy?, frame: Boolean): String {
        return if (em == null) "" else getTBA(em.de, frame)
    }
    private fun getTBA(ma : MaskEntity, frame : Boolean) : String {
        return if (frame) ma.tba.toString() + "f" else DecimalFormat("#.##").format(ma.tba.toDouble() / 30) + "s"
    }

    fun getPre(c: Character?, frame: Boolean, index : Int): String {
        if (c == null)
            return ""
        val atkdat = StaticJava.getAtkModel(c.mask, index)
        return if (frame) {
            if (atkdat.size > 1) {
                val result = StringBuilder()

                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(atkdat[i].pre).append("f / ")
                    else result.append(atkdat[i].pre).append("f")
                }
                result.toString()
            } else atkdat[0].pre.toString() + "f"
        } else {
            val df = DecimalFormat("#.##")
            if (atkdat.size > 1) {
                val result = StringBuilder()
                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(df.format(atkdat[i].pre.toDouble() / 30)).append("s / ")
                    else result.append(df.format(atkdat[i].pre.toDouble() / 30)).append("s")
                }
                result.toString()
            } else df.format(atkdat[0].pre.toDouble() / 30) + "s"
        }
    }

    fun getPackName(id: Identifier<*>, isRaw: Boolean) : String {
        return if (isRaw) id.pack
        else if (id.pack == Identifier.DEF) c.getString(R.string.pack_default)
        else StaticStore.getPackName(id.pack)
    }
    fun getPackName(pack: String, isRaw: Boolean) : String {
        return if(isRaw) pack
        else if(StaticStore.BCMapCodes.contains(pack)) c.getString(R.string.pack_default) + "(" + c.getString(StaticStore.bcMapNames[StaticStore.BCMapCodes.indexOf(pack)]) + ")"
        else StaticStore.getPackName(pack)
    }

    fun getID(viewHolder: RecyclerView.ViewHolder?, id: String): String {
        return if (viewHolder == null) ""
        else id + "-" + viewHolder.bindingAdapterPosition
    }
    fun getID(form: Int, id: String): String {
        return "$id-$form"
    }

    fun getRange(f: Form?, index : Int, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""
        val du = if(f.du.pCoin != null && talent) f.du.pCoin.improve(lvs.talents) else f.du//Custom range talents exist in this fork
        return getRange(f, index, du.range)
    }
    fun getRange(e: Enemy?, index : Int): String {
        if (e == null)
            return ""
        return getRange(e, index, e.de.range)
    }
    private fun getRange(c: Character, index : Int, tb : Int): String {
        if(!c.mask.isLD && !c.mask.isOmni)
            return tb.toString()

        val model = StaticJava.getAtkModel(c.mask, index)
        if(model.isEmpty() || allRangeSame(c.mask, index)) {
            val ma = StaticJava.getAtkModel(c.mask, index)[0]
            val lds = ma.shortPoint
            val ldr = ma.longPoint - ma.shortPoint

            val start = lds.coerceAtMost(lds + ldr)
            val end = lds.coerceAtLeast(lds + ldr)
            return "$tb | $start ~ $end"
        } else {
            val builder = StringBuilder("$tb | ")
            for(i in model.indices) {
                val ma = model[i]

                val lds = ma.shortPoint
                val ldr = ma.longPoint - ma.shortPoint

                val start = lds.coerceAtMost(lds + ldr)
                val end = lds.coerceAtLeast(lds + ldr)

                builder.append("$start ~ $end")
                if(i < model.size-1)
                    builder.append(" / ")
            }
            return builder.toString()
        }
    }

    private fun allRangeSame(de: MaskEntity, index : Int) : Boolean {
        val near = ArrayList<Int>()
        val far = ArrayList<Int>()

        for(atk in StaticJava.getAtkModel(de, index)) {
            near.add(atk.shortPoint)
            far.add(atk.longPoint)
        }
        if(near.isEmpty() && far.isEmpty())
            return true

        for(n in near)
            if(n != near[0])
                return false
        for(f in far)
            if(f != far[0])
                return false
        return true
    }

    fun getCD(f: Form?, t: Treasure?, frame: Boolean, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du

        return if (frame) t.getFinRes(du.respawn, 0).toString() + "f"
        else DecimalFormat("#.##").format(t.getFinRes(du.respawn, 0).toDouble() / 30) + "s"
    }

    fun getAtk(f: Form?, t: Treasure?, talent: Boolean, lvs: Level, index : Int): String {
        if (f == null || t == null)
            return ""
        return if (StaticJava.getAtkModel(f.du, index).size > 1) getTotAtk(f, t, talent, lvs, index) + " " + getAtks(f, t, talent, lvs, index)
        else getTotAtk(f, t, talent, lvs, index)
    }
    fun getAtk(em: Enemy?, multi: Int, index: Int): String {
        if (em == null)
            return ""
        return if (StaticJava.getAtkModel(em.de, index).size > 1) getTotAtk(em, multi, index) + " " + getAtks(em, multi, index)
        else getTotAtk(em, multi, index)
    }

    fun getSpd(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du

        return du.speed.toString()
    }
    fun getSpd(em: Enemy?): String {
        return em?.de?.speed?.toString() ?: ""
    }

    fun getBarrier(em: Enemy?): String {
        if (em == null)
            return ""

        return if (em.de.proc.BARRIER.health == 0)
            c.getString(R.string.unit_info_t_none)
        else
            em.de.proc.BARRIER.health.toString()
    }

    fun getHB(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du

        return du.hb.toString()
    }

    fun getHB(em: Enemy?): String {
        return em?.de?.hb?.toString() ?: ""
    }

    fun getHP(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du

        val result = if(f.du.pCoin != null && talent) {
            (((du.hp * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.defMulti).toInt() * (f.du.pCoin.getStatMultiplication(Data.PC2_HP, lvs.talents))).toInt()
        } else
            ((du.hp * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.defMulti).toInt()

        return result.toString()
    }

    fun getHP(em: Enemy?, multi: Int): String {
        if (em == null)
            return ""

        return (em.de.multi(BasisSet.current()) * em.de.hp * multi / 100).toInt().toString()
    }

    fun getTotAtk(f: Form?, t: Treasure?, talent: Boolean, lvs: Level, index : Int): String {
        if (f == null || t == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du

        val result: Int = if(f.du.pCoin != null && talent) {
            (((StaticJava.allAtk(du, index) * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt() * f.du.pCoin.getStatMultiplication(Data.PC2_ATK, lvs.talents)).toInt()
        } else ((StaticJava.allAtk(du, index) * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt()

        return result.toString()
    }
    private fun getTotAtk(em: Enemy?, multi: Int, index : Int): String {
        if (em == null)
            return ""
        return (em.de.multi(BasisSet.current()) * StaticJava.allAtk(em.de, index) * multi / 100).toInt().toString()
    }

    fun getDPS(f: Form?, t: Treasure?, talent: Boolean, lvs: Level, index : Int): String {
        return if (f == null || t == null || index >= f.du.atkTypeCount) ""
        else {
            val du = if(talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du
            DecimalFormat("#.##").format(getTotAtk(f, t, talent, lvs, index).toDouble() / (du.getItv(index) / 30.0)).toString()
        }
    }
    fun getDPS(em: Enemy?, multi: Int, index : Int): String {
        return if (em == null || index >= em.de.atkTypeCount) ""
        else DecimalFormat("#.##").format(getTotAtk(em, multi, index).toDouble() / (em.de.getItv(index).toDouble() / 30)).toString()
    }

    fun getTrait(ef: Form?, talent: Boolean, lvs: Level): Array<Bitmap> {
        if (ef == null) return arrayOf()
        val du: MaskUnit = if (ef.du.pCoin != null && talent) ef.du.pCoin.improve(lvs.talents) else ef.du
        return traitIcons(du.traits)
    }
    fun getTrait(em: Enemy): Array<Bitmap> {
        return traitIcons(em.de.traits)
    }
    fun traitIcons(traits : SortedPackSet<Trait>) : Array<Bitmap> {
        return Array(traits.size) { i ->
            if(traits[i].id.pack == Identifier.DEF) CommonStatic.getBCAssets().icon[3][traits[i].id.id].img.bimg() as Bitmap
            else if (traits[i].icon != null && traits[i].icon.img != null) traits[i].icon.img.bimg() as Bitmap
            else CommonStatic.getBCAssets().dummyTrait.img.bimg() as Bitmap
        }
    }

    fun getCost(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""
        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du
        return (du.price * 1.5).toInt().toString()
    }
    fun getDrop(em: Enemy?, t: Treasure): String {
        if (em == null)
            return ""
        return (em.de.drop * t.getDropMulti(0) / 100).toInt().toString()
    }

    private fun getAtks(f: Form?, t: Treasure?, talent: Boolean, lvs: Level, index : Int): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (talent && f.du.pCoin != null) f.du.pCoin.improve(lvs.talents) else f.du
        val atks = StaticJava.getAtkModel(du, index)
        val damges = ArrayList<Int>()

        for (atk in atks) {
            val result: Int = if(f.du.pCoin != null && talent) {
                (((atk.atk * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt() * f.du.pCoin.getStatMultiplication(Data.PC2_ATK, lvs.talents)).toInt()
            } else
                ((atk.atk * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt()
            damges.add(result)
        }
        val result = StringBuilder("(")
        for (i in damges.indices) {
            if (i < damges.size - 1) result.append(damges[i]).append(", ")
            else result.append(damges[i]).append(")")
        }
        return result.toString()
    }
    private fun getAtks(em: Enemy?, multi: Int, index : Int): String {
        if (em == null)
            return ""

        val atks = StaticJava.getAtkModel(em.de, index)
        val damages = ArrayList<Int>()

        for (atk in atks)
            damages.add((atk.atk * em.de.multi(BasisSet.current()) * multi / 100).toInt())

        val result = StringBuilder("(")
        for (i in damages.indices) {
            if (i < damages.size - 1) result.append("").append(damages[i]).append(", ")
            else result.append("").append(damages[i]).append(")")
        }
        return result.toString()
    }

    fun getSimus(ch: Character?, index: Int) : ArrayList<Bitmap> {
        if (ch == null) return ArrayList(0)

        val single = CommonStatic.getBCAssets().icon[2][Data.ATK_SINGLE.toInt()].img.bimg() as Bitmap
        val area = CommonStatic.getBCAssets().icon[2][Data.ATK_AREA.toInt()].img.bimg() as Bitmap

        val lis = ArrayList<Bitmap>()
        val atks = StaticJava.getAtkModel(ch.mask, index)

        var allSame = true
        val defult = atks[0].isRange
        for (atk in atks)
            if (atk.isRange != defult) {
                allSame = false
                break
            }
        if (allSame) {
            lis.add(if (defult) area else single)
        } else
            for (atk in atks)
                lis.add(if (atk.isRange) area else single)
        return lis
    }

    fun getTalentName(index: Int, f: Form, c: Context): String {
        val ans: String?
        val info = f.du.pCoin.info
        if (info[index][0] < 0)
            return c.getString(cusTalent[-info[index][0]])
        if(talData[info[index][0]] == -1)
            return "Invalid Data"

        val trait = listOf(37, 38, 39, 40)
        val basic = listOf(25, 26, 27, 31, 32)

        ans = when {
            trait.contains(info[index][0]) -> c.getString(R.string.talen_trait) + talTool[info[index][0]]
            basic.contains(info[index][0]) -> talTool[info[index][0]]
            f.du.pCoin.trait.isNotEmpty() && index == 0 -> {
                val tr = Interpret.getTrait(f.du.pCoin.trait, 0, c)
                if(tr.endsWith(", "))
                    c.getString(R.string.talen_abil) + tr.substring(0, tr.length - 2) + " " + talTool[info[index][0]]
                else c.getString(R.string.talen_abil) + tr + " " + talTool[info[index][0]]
            }
            else -> c.getString(R.string.talen_abil) + " " + talTool[info[index][0]]
        }
        return ans
    }

    fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

    fun getID(mapcode: String, stid: Int, posit: Int): String {
        return if(mapcode.length == 6) {
            val index = StaticStore.BCMapCodes.indexOf(mapcode)

            if(index != -1) {
                "${mapcolcid[index]}-$stid-$posit"
            } else {
                "$mapcode-$stid-$posit"
            }
        } else {
            "$stid=$posit"
        }
    }

    fun getDifficulty(diff: Int, ac: Activity): String {
        return if(diff < 0)
            ac.getString(R.string.unit_info_t_none)
        else
            "★$diff"
    }

    fun getEnergy(stage: Stage, c: Context) : String {
        val info = stage.info ?: return "0"

        return if (info is DefStageInfo) {
            if (stage.cont.cont.sid == "000014") {
                if (info.energy < 1000) {
                    c.getString(R.string.stg_info_catamina, info.energy)
                } else if(info.energy < 2000) {
                    c.getString(R.string.stg_info_cataminb, info.energy - 1000)
                } else {
                    c.getString(R.string.stg_info_cataminc, info.energy - 2000)
                }
            } else {
                info.energy.toString()
            }
        } else {
            "0"
        }
    }

    fun getLayer(data: SCDef.Line): String {
        return if (data.layer_0 == data.layer_1)
            "" + data.layer_0
        else
            data.layer_0.toString() + " ~ " + data.layer_1
    }

    fun getRespawn(data: SCDef.Line, frame: Boolean): String {
        return if (data.respawn_0 == data.respawn_1)
            if (frame) data.respawn_0.toString() + "f"
            else DecimalFormat("#.##").format(data.respawn_0.toFloat() / 30.toDouble()) + "s"
        else if (frame)
            data.respawn_0.toString() + "f ~ " + data.respawn_1 + "f"
        else DecimalFormat("#.##").format(data.respawn_0.toFloat() / 30.toDouble()) + "s ~ " + DecimalFormat("#.##").format(data.respawn_1.toFloat() / 30.toDouble()) + "s"
    }

    fun getBaseHealth(data: SCDef.Line): String {
        return if(data.castle_0 == data.castle_1 || data.castle_1 == 0) {
            "${data.castle_0}%"
        } else
            "${data.castle_0}% / ${data.castle_1}%"
    }

    fun getMultiply(data: SCDef.Line, multi: Int): String {
        return if(data.multiple == data.mult_atk) {
            (data.multiple.toFloat() * multi.toFloat() / 100f).toInt().toString() + "%"
        } else {
            (data.multiple.toFloat() * multi.toFloat() / 100f).toInt().toString() + " / " + (data.mult_atk.toFloat() * multi.toFloat() / 100.toFloat()).toInt().toString() + "%"
        }
    }

    fun getNumber(data: SCDef.Line): String {
        return if (data.number == 0)
            c.getString(R.string.infinity)
        else
            data.number.toString()
    }

    fun getStart(data: SCDef.Line, frse: Boolean): String {
        return if (data.spawn_0 == data.spawn_1 || data.spawn_1 == 0) {
            if (frse) data.spawn_0.toString() + "f"
            else DecimalFormat("#.##").format(data.spawn_0.toFloat() / 30.toDouble()) + "s"
        } else if (frse)
            data.spawn_0.toString() + "f ~ " + data.spawn_1 + "f"
        else DecimalFormat("#.##").format(data.spawn_0.toFloat() / 30.toDouble()) + "s ~ " + DecimalFormat("#.##").format(data.spawn_1.toFloat() / 30.toDouble()) + "s"
    }

    fun getLimit(l: Limit?): LinkedHashMap<String, String> {
        if (l == null)
            return LinkedHashMap()

        val limits: LinkedHashMap<String, String> = LinkedHashMap()
        if (l.line != 0) {
            val result = c.getString(R.string.limit_line) + " : " + c.getString(if (l.line == 2) R.string.limit_line3 else R.string.limit_line2)
            limits[c.getString(R.string.limit_line)] = result
        }
        if (l.max != 0) {
            val result = c.getString(R.string.limit_max) + " : " + c.getString(R.string.limit_max2).replace("_", l.max.toString())
            limits[c.getString(R.string.limit_max)] = result
        }
        if (l.min != 0) {
            val result = c.getString(R.string.limit_min) + " : " + c.getString(R.string.limit_min2).replace("_", l.min.toString())
            limits[c.getString(R.string.limit_min)] = result
        }
        if (l.rare != 0) {
            val rid = intArrayOf(R.string.sch_rare_ba, R.string.sch_rare_ex, R.string.sch_rare_ra, R.string.sch_rare_sr, R.string.sch_rare_ur, R.string.sch_rare_lr)
            val rare = StringBuilder()

            for (i in rid.indices) {
                if (l.rare shr i and 1 == 1)
                    rare.append(c.getString(rid[i])).append(", ")
            }

            val result = c.getString(R.string.limit_rare) + " : " + rare.toString().substring(0, rare.length - 2)
            limits[c.getString(R.string.limit_rare)] = result
        }
        if (l.num != 0) {
            val result = c.getString(R.string.limit_deploy) + " : " + l.num
            limits[c.getString(R.string.limit_deploy)] = result
        }
        if (l.group != null && l.group.fset.size != 0) {
            val units = StringBuilder()
            val u: List<Form> = ArrayList(l.group.fset)

            for (i in u.indices) {
                if (i == l.group.fset.size - 1) {
                    val f = MultiLangCont.get(u[i]) ?: u[i].names.toString()

                    units.append(f)
                } else {
                    val f = MultiLangCont.get(u[i]) ?: u[i].names.toString()

                    units.append(f).append(", ")
                }
            }

            val result: String = if (l.group.type == 0)
                c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra1).replace("_", units.toString())
            else
                c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra2).replace("_", units.toString())
            val key = c.getString(R.string.limit_chra) + " : " + l.group.toString()
            limits[key] = result
        }
        if (l.stageLimit?.isBlank == false) {
            if (l.stageLimit.maxMoney != 0)
                limits[c.getString(R.string.limit_bank)] = c.getString(R.string.limit_bank) + " : " + l.stageLimit.maxMoney
            if (l.stageLimit.globalCooldown != 0)
                limits[c.getString(R.string.limit_uvcd)] = c.getString(R.string.limit_uvcd) + " : " + l.stageLimit.globalCooldown
            if (l.stageLimit.globalCost != 0)
                limits[c.getString(R.string.limit_unico)] = c.getString(R.string.limit_unico) + " : " + l.stageLimit.globalCost
            if (l.stageLimit.coolStart)
                limits[c.getString(R.string.limit_inicd)] = c.getString(R.string.limit_inicd)//lol, lmao even
            if (l.stageLimit.maxUnitSpawn != 0)
                limits[c.getString(R.string.limit_spawn)] = c.getString(R.string.limit_spawn) + " : " + l.stageLimit.maxUnitSpawn
            if (l.stageLimit.bannedCatCombo.isNotEmpty()) {
                val str = StringBuilder(c.getString(R.string.limit_banco)).append(" : ")
                for (i in l.stageLimit.bannedCatCombo.indices) {
                    str.append(c.getString(StaticStore.comnames[i]))
                    if (i < l.stageLimit.bannedCatCombo.size - 1)
                        str.append(", ")
                }
                limits[c.getString(R.string.limit_banco)] = str.toString()
            }
            if (!l.stageLimit.defCD() || !l.stageLimit.defMoney() || !l.stageLimit.defDeploy()) {
                val rid = intArrayOf(R.string.sch_rare_ba, R.string.sch_rare_ex, R.string.sch_rare_ra, R.string.sch_rare_sr, R.string.sch_rare_ur, R.string.sch_rare_lr)
                if (!l.stageLimit.defMoney()) {
                    val str = StringBuilder(c.getString(R.string.limit_rcos)).append(": [")
                    for (i in rid.indices)
                        if (l.stageLimit.costMultiplier[i] != 100)
                            str.append(c.getString(rid[i])).append(" : ").append(l.stageLimit.costMultiplier[i]).append("%, ")
                    limits[c.getString(R.string.limit_rcos)] = str.substring(0, str.length - 2) + "]"
                }
                if (!l.stageLimit.defCD()) {
                    val str = StringBuilder(c.getString(R.string.limit_rcoo)).append(" : [")
                    for (i in rid.indices)
                        if (l.stageLimit.cooldownMultiplier[i] != 100)
                            str.append(c.getString(rid[i])).append(": ").append(l.stageLimit.cooldownMultiplier[i]).append("%, ")
                    limits[c.getString(R.string.limit_rcoo)] = str.substring(0, str.length - 2) + "]"
                }
                if (!l.stageLimit.defDeploy()) {
                    val str = StringBuilder(c.getString(R.string.limit_rspn)).append(" : [")
                    for (i in rid.indices)
                        if (l.stageLimit.rarityDeployLimit[i] != -1)
                            str.append(c.getString(rid[i])).append(": ").append(l.stageLimit.rarityDeployLimit[i]).append(", ")
                    limits[c.getString(R.string.limit_rspn)] = str.substring(0, str.length - 2) + "]"
                }
                if (!l.stageLimit.defDupe()) {
                    val str = StringBuilder(c.getString(R.string.limit_dpspwn)).append(" : [")
                    for (i in rid.indices)
                        if (l.stageLimit.deployDuplicationTimes[i] != 0)
                            str.append(c.getString(rid[i])).append(": ").append(l.stageLimit.deployDuplicationTimes[i]).append(" / ").append(l.stageLimit.deployDuplicationDelay[i]).append("f, ")
                    limits[c.getString(R.string.limit_dpspwn)] = str.substring(0, str.length - 2) + "]"
                }
            }
        }
        return limits
    }

    fun getXP(xp: Int, t: Treasure?, legend: Boolean): String {
        if (t == null)
            return ""

        return if (legend)
            "" + (xp * t.xpMult * 9).toInt()
        else
            "" + (xp * t.xpMult).toInt()
    }

    fun getMiscellaneous(st: Stage) : ArrayList<String> {
        val res = ArrayList<String>()

        st.info ?: return res
        if(st.info !is DefStageInfo)
            return res

        if(st.cont.info.hiddenUponClear)
            res.add(c.getString(R.string.stg_info_hidden))

        if(st.cont.info.resetMode != -1) {
            when(st.cont.info.resetMode) {
                1 -> res.add(c.getString(R.string.stg_info_reset1))
                2 -> res.add(c.getString(R.string.stg_info_reset2))
                3 -> res.add(c.getString(R.string.stg_info_reset3))
                else -> res.add(c.getString(R.string.stg_info_reset).replace("_", st.cont.info.resetMode.toString()))
            }
        }

        if(st.cont.info.waitTime != -1)
            res.add(c.getString(R.string.stg_info_wait).replace("_", st.cont.info.waitTime.toString()))
        if(st.cont.info.clearLimit != -1)
            res.add(c.getString(R.string.stg_info_numbplay).replace("_", st.cont.info.clearLimit.toString()))
        if (st.cont.info.unskippable)
            res.add(c.getString(R.string.stg_info_nocpu))

        return res
    }

    fun getStrings(vararg ids : Int) : Array<String> {
        return Array(ids.size) {c.getString(ids[it])}
    }

    fun getAStrings(ids : IntArray) : Array<String> {
        return Array(ids.size) {c.getString(ids[it])}
    }

    fun getBoolean(b : Boolean) : String {
        return c.getString(if (b) R.string.unit_info_true else R.string.unit_info_false)
    }
}