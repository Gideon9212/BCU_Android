package com.g2.bcu.androidutil

import android.content.Context
import android.util.Log
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore.isEnglish
import common.battle.data.MaskEntity
import common.pack.SortedPackSet
import common.util.Data
import common.util.lang.Formatter
import common.util.lang.ProcLang
import common.util.unit.Trait
import java.util.Locale

object Interpret : Data() {
    const val EN = "en"
    const val ZH = "zh"
    const val JA = "ja"
    const val KO = "ko"
    const val RU = "ru"
    const val FR = "fr"

    /**
     * enemy traits
     */
    val TRAIT = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_de, R.string.sch_re, R.string.sch_wh, R.string.esch_eva, R.string.esch_witch, R.string.sch_bar, R.string.sch_bst, R.string.sch_ssg, R.string.sch_ba)

    /**
     * star names
     */
    val STAR = intArrayOf(R.string.unit_info_starred, R.string.unit_info_god1, R.string.unit_info_god2, R.string.unit_info_god3)

    /**
     * ability name
     */
    val ABIS = intArrayOf(R.string.sch_abi_ao, R.string.sch_abi_me, R.string.abi_isnk, R.string.abi_istt, R.string.abi_gh, R.string.sch_abi_zk, R.string.sch_abi_wk, R.string.abi_sui, R.string.abi_ithch, R.string.sch_abi_eva, R.string.abi_iboswv, R.string.sch_abi_bk, R.string.sch_abi_ck, R.string.sch_abi_sh)

    /**
     * Additional ability description
     */
    val ADD = intArrayOf(R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_colo)

    /**
     * Converts Data Proc index to BCU Android Proc Index
     */
    private val P_INDEX = byteArrayOf(P_DMGINC, P_DEFINC, P_WEAK, P_STOP, P_SLOW, P_KB, P_WARP, P_CURSE, P_IMUATK, P_STRONG, P_LETHAL, P_LETHARGY, P_RAGE, P_HYPNO,
            P_ATKBASE, P_CRIT, P_METALKILL, P_BREAK, P_SHIELDBREAK, P_SATK, P_BOUNTY, P_MINIWAVE, P_WAVE, P_MINIVOLC, P_VOLC, P_DEMONVOLC, P_BLAST, P_SPIRIT, P_BSTHUNT,
            P_IMUWEAK, P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUVOLC, P_IMUBLAST, P_IMUWARP, P_IMUCURSE, P_IMUPOIATK, P_IMULETHARGY, P_IMURAGE, P_IMUHYPNO,
            P_POIATK, P_BARRIER, P_DEMONSHIELD, P_REMOTESHIELD, P_RANGESHIELD, P_DEATHSURGE, P_BURROW, P_REVIVE, P_SNIPER, P_SEAL, P_BLESS, P_TIME, P_SUMMON,
            P_MOVEWAVE, P_THEME, P_POISON, P_BOSS, P_ARMOR, P_SPEED, P_COUNTER, P_DMGCUT, P_DMGCAP, P_CRITI, P_IMUPOI, P_IMUSEAL, P_IMUMOVING, P_IMUSUMMON,
            P_IMUARMOR, P_IMUSPEED, P_WORKERLV, P_CDSETTER, P_WEAKAURA, P_STRONGAURA, P_DRAIN, P_IMUCANNON, P_AI)

    /**
     * treasure max
     */
    private val TMAX = intArrayOf(30, 30, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 1500, 100,
            100, 100, 30, 30, 30, 30, 30, 10, 300, 300, 600, 600, 600, 20, 30, 30, 20, 30, 30, 30)

    private val PROCIND = arrayOf("DMGINC", "DEFINC", "WEAK", "STOP", "SLOW", "KB", "WARP", "CURSE", "IMUATK", "STRONG", "LETHAL", "LETHARGY", "RAGE", "HYPNO",
            "ATKBASE", "CRIT", "METALKILL", "BREAK", "SHIELDBREAK", "SATK", "BOUNTY", "MINIWAVE", "WAVE", "MINIVOLC", "VOLC", "DEMONVOLC", "BLAST", "SPIRIT",
            "BSTHUNT", "IMUWEAK", "IMUSTOP", "IMUSLOW", "IMUKB", "IMUWAVE", "IMUVOLC", "IMUBLAST", "IMUWARP", "IMUCURSE", "IMUPOIATK", "IMULETHARGY", "IMURAGE",
            "IMUHYPNO", "POIATK", "BARRIER", "DEMONSHIELD", "REMOTESHIELD", "RANGESHIELD", "DEATHSURGE", "BURROW", "REVIVE", "SNIPER", "SEAL", "BLESSING", "TIME", "SUMMON",
            "MOVEWAVE", "THEME", "POISON", "BOSS", "ARMOR", "SPEED", "COUNTER", "DMGCUT", "DMGCAP", "CRITI", "IMUPOI", "IMUSEAL", "IMUMOVING", "IMUSUMMON",
            "IMUARMOR", "IMUSPEED", "WORKERLV", "CDSETTER", "WEAKAURA", "STRONGAURA", "DRAIN", "IMUCANNON", "AI")

    private val immune = listOf(P_IMUWEAK, P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUWARP, P_IMUCURSE, P_IMUPOIATK, P_IMUVOLC)

    fun getTrait(traits: SortedPackSet<Trait>, star: Int, c: Context): String {
        if (traits.isEmpty()) return ""
        val ans = StringBuilder()
        for(trait in traits) {
            if(trait.BCTrait()) {
                if(trait.id.id == 6 && star == 1)
                    ans.append(c.getString(TRAIT[6])).append(" (").append(c.getString(STAR[1])).append("), ")
                else ans.append(c.getString(TRAIT[trait.id.id])).append(", ")
            } else ans.append(trait.name).append(", ")
        }
        return ans.substring(0, ans.length - 2)
    }

    fun getProc(du: MaskEntity, useSecond: Boolean, isEnemy: Boolean, magnif: DoubleArray, context: Context, atk : Int = 0): List<String> {
        val lang = Locale.getDefault().language
        val common = du.isCommon

        val l: MutableList<String> = ArrayList()
        val c = Formatter.Context(isEnemy, useSecond, magnif, du.traits)

        for (i in PROCIND.indices) {
            if (isValidProc(i, du.proc) && (common || du.proc.sharable(P_INDEX[i].toInt()))) {
                val f = ProcLang.get().get(PROCIND[i]).format
                val item = du.proc.get(PROCIND[i])
                val ans = if ((P_INDEX[i] == P_DMGINC || P_INDEX[i] == P_DEFINC)) {
                    "${-(StaticStore.dmgType(P_INDEX[i] == P_DMGINC, item[0])+1)}\\" + Formatter.format(f, item, c)
                } else if (immune.contains(P_INDEX[i]) && item[0] != 100 && (P_INDEX[i] == P_IMUWAVE || item[1] < 100)) {//item[0] will always be mult, which calculates resistances
                    if (P_INDEX[i] == P_IMUWAVE && item[1] != 0) "-6\\" + Formatter.format(f, item, c)
                    else "${-(StaticStore.siinds.size - immune.size + immune.indexOf(P_INDEX[i])+1)}\\" + Formatter.format(f, item, c)
                } else
                    "${P_INDEX[i]}\\" + Formatter.format(f, item, c)

                l.add(ans)
            }
        }
        if (!common) {
            val atkMap = HashMap<String, ArrayList<Int>>()
            for (k in 0 until StaticJava.getAtkModel(du, atk).size) {
                val ma = StaticJava.getAtkModel(du, atk)[k]
                for (i in PROCIND.indices) {
                    if (isValidProc(i, ma.proc) && !ma.proc.sharable(P_INDEX[i].toInt())) {
                        val mf = ProcLang.get().get(PROCIND[i]).format
                        val item = ma.proc.get(PROCIND[i])
                        val ans = if ((P_INDEX[i] == P_DMGINC || P_INDEX[i] == P_DEFINC)) {
                            "${-(StaticStore.dmgType(P_INDEX[i] == P_DMGINC, item[0])+1)}\\" + Formatter.format(mf, item, c)
                        } else if (immune.contains(P_INDEX[i]) && item[0] != 100) {//item[0] will always be mult, which calculates resistances
                            if (P_INDEX[i] == P_IMUWAVE && item[1] != 0) "-6\\" + Formatter.format(mf, item, c)
                            else "${-(StaticStore.siinds.size - immune.size + immune.indexOf(P_INDEX[i])+1)}\\" + Formatter.format(mf, item, c)
                        } else
                            "${P_INDEX[i]}\\" + Formatter.format(mf, item, c)

                        val inds = atkMap[ans] ?: ArrayList()
                        inds.add(k + 1)
                        atkMap[ans] = inds
                    }
                }
            }
            for(key in atkMap.keys) {
                val inds = atkMap[key] ?: ArrayList()
                when {
                    inds.isEmpty() || inds.size == StaticJava.getAtkModel(du, atk).size -> l.add(key)
                    else -> l.add(getFullExplanationWithAtk(key, inds, lang, context))
                }
            }
        }
        return l
    }

    private fun isValidProc(ind: Int, proc: Proc): Boolean {
        return when (ind) {
            in P_INDEX.indices ->
                proc.getArr(P_INDEX[ind].toInt()).exists()
            else -> {
                Log.e("Interpret", "Invalid index : $ind")
                false
            }
        }
    }

    private fun getFullExplanationWithAtk(explanation: String, inds: ArrayList<Int>, lang: String, c: Context) : String {
        if(isEnglish) {
            val builder = StringBuilder("[")
            for(i in inds.indices) {
                builder.append(numberWithExtension(inds[i], lang))

                if(i < inds.size - 1)
                    builder.append(", ")
            }
            return explanation + " " + builder.append(getNumberAttack(lang)).append("]").toString()
        } else {
            val builder = StringBuilder()

            for(i in inds.indices) {
                builder.append(inds[i])

                if(i < inds.size - 1)
                    builder.append(", ")
            }
            return explanation + " " + c.getString(R.string.unit_info_atks).replace("_", builder.toString())
        }
    }

    fun getAbiid(me: MaskEntity): List<Int> {
        val l: MutableList<Int> = ArrayList()
        for (i in ABIS.indices)
            if (me.abi shr i and 1 > 0)
                l.add(i)
        return l
    }

    fun getAbi(me: MaskEntity, frag: Array<Array<String>>, lang: Int, c: Context): List<String> {
        val l: MutableList<String> = ArrayList()
        for (i in ABIS.indices) {
            val imu = StringBuilder(frag[lang][0])
            val abilityName = c.getString(ABIS[i])

            if (me.abi shr i and 1 > 0)
                if (abilityName.startsWith("Imu."))
                    imu.append(abilityName.substring(4))
                else {
                    when (i) {
                        ABI_WKILL.toInt() -> l.add(abilityName + c.getString(ADD[0]))
                        ABI_EKILL.toInt() -> l.add(abilityName + c.getString(ADD[1]))
                        ABI_BAKILL.toInt() -> l.add(abilityName + c.getString(ADD[2]))
                        else -> l.add(abilityName)
                    }
                }
            if (imu.toString().isNotEmpty() && imu.toString() != frag[lang][0])
                l.add(imu.toString())
        }
        return l
    }

    fun isType(de: MaskEntity, type: Int, index : Int): Boolean {
        val raw = StaticJava.getAtkModel(de, index)

        return when (type) {
            0 -> !de.isRange(index)
            1 -> de.isRange(index)
            2 -> de.isLD
            3 -> raw.size > 1
            4 -> de.isOmni
            5 -> de.tba + raw[0].pre < de.getItv(0) / 2
            else -> false
        }
    }

    fun numberWithExtension(n: Int, lang: String): String {
        val f = n % 10
        return when (lang) {
            EN -> when (f) {
                1 -> n.toString() + if(n != 11) "st" else "th"
                2 -> n.toString() + if(n != 12) "nd" else "th"
                3 -> n.toString() + if(n != 13) "rd" else "th"
                else -> n.toString() + "th"
            }
            RU -> {
                if (f == 3) {
                    n.toString() + "ья"
                } else n.toString() + "ая"
            }
            FR -> {
                if (f == 1) {
                    n.toString() + "ière"
                } else n.toString() + "ième"
            }
            else -> " $n"
        }
    }

    private fun getNumberAttack(lang: String): String {
        return when (lang) {
            EN -> "Attack"
            RU -> "Аттака"
            FR -> "Attaque"
            else -> ""
        }
    }
}