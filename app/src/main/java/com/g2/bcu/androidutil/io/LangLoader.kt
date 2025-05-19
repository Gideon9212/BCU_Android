package com.g2.bcu.androidutil.io

import android.content.Context
import com.g2.bcu.androidutil.Definer
import com.g2.bcu.androidutil.StaticStore
import common.CommonStatic
import common.CommonStatic.Lang
import common.pack.UserProfile
import common.system.files.VFile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import common.util.stage.info.DefStageInfo
import common.util.unit.Combo
import java.io.File

object LangLoader {
    @Synchronized
    fun readUnitLang(c: Context) {
        val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")

        MultiLangCont.getStatic().FNAME.clear()
        MultiLangCont.getStatic().FEXP.clear()
        MultiLangCont.getStatic().CFEXP.clear()
        MultiLangCont.getStatic().COMNAME.clear()

        for (l in Lang.Locale.values()) {
            for (n in files) {
                val f = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$n")
                if (f.exists()) {
                    val qs = VFile.getFile(f).data.readLine()
                    when (n) {
                        "UnitName.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])] ?: continue
                            for (i in u.forms.indices) {
                                if (i >= strs.size-1) break
                                MultiLangCont.getStatic().FNAME.put(l, u.forms[i], strs[i + 1].trim())
                            }
                        }
                        "UnitExplanation.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])] ?: continue
                            for (i in u.forms.indices) {
                                if (i >= strs.size-1) break
                                MultiLangCont.getStatic().FEXP.put(l, u.forms[i], strs[i + 1].trim().split("<br>").toTypedArray())
                            }
                        }
                        "CatFruitExplanation.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            if (strs.size == 1)
                                continue
                            val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])] ?: continue
                            val lines = strs[1].replace("<br>", "\n")

                            MultiLangCont.getStatic().CFEXP.put(l, u.info, lines)
                            if (strs.size == 3) {
                                val ultraLines = strs[2].replace("<br>", "\n")
                                MultiLangCont.getStatic().UFEXP.put(l, u.info, ultraLines)
                            }
                        }
                        "ComboName.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            if (strs.size <= 1) {
                                continue
                            }
                            val id = strs[0].trim()
                            val combo = getComboViaID(UserProfile.getBCData().combos.list, id) ?: continue

                            val name = strs[1].trim()
                            MultiLangCont.getStatic().COMNAME.put(l, combo, name)
                        }
                    }
                }
            }
        }
        Definer.unitlang = true
    }

    @Synchronized
    fun readEnemyLang(c: Context) {
        val files = arrayOf("EnemyName.txt", "EnemyExplanation.txt")

        MultiLangCont.getStatic().ENAME.clear()
        MultiLangCont.getStatic().EEXP.clear()
        for (l in Lang.Locale.values()) {
            for (n in files) {
                val f = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$n")
                if (f.exists()) {
                    val qs = VFile.getFile(f).data.readLine()

                    when (n) {
                        "EnemyName.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            val em = UserProfile.getBCData().enemies[CommonStatic.parseIntN(strs[0])] ?: continue
                            if (strs.size > 1)
                                MultiLangCont.getStatic().ENAME.put(l, em, if (strs[1].trim().startsWith("【")) strs[1].trim().substring(1, strs[1].trim().length - 1) else strs[1].trim())
                        }
                        "EnemyExplanation.txt" -> for (str in qs) {
                            val strs = str.trim().split("\t").toTypedArray()
                            val em = UserProfile.getBCData().enemies[CommonStatic.parseIntN(strs[0])] ?: continue
                            if (strs.size > 1 && strs[1].trim().replace("<br>","").isNotEmpty())
                                MultiLangCont.getStatic().EEXP.put(l, em, strs[1].trim().split("<br>").toTypedArray())
                        }
                    }
                }
            }
        }
        Definer.enemylang = true
    }

    @Synchronized
    fun readStageLang(c: Context) {
        val file = "StageName.txt"
        val diff = "Difficulty.txt"
        val rewa = "RewardName.txt"

        MultiLangCont.getStatic().SMNAME.clear()
        MultiLangCont.getStatic().STNAME.clear()
        MultiLangCont.getStatic().RWNAME.clear()

        for (l in Lang.Locale.values()) {
            val f = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$file")

            if (f.exists()) {
                val qs = VFile.getFile(f).data.readLine()

                if (qs != null) {
                    for (s in qs) {
                        val strs = s.trim().split("\t").toTypedArray()

                        if (strs.size == 1)
                            continue

                        val id = strs[0].trim()
                        val name = strs[strs.size - 1].trim()

                        if (id.isEmpty() || name.isEmpty())
                            continue

                        val ids = id.split("-").toTypedArray()
                        val id0 = CommonStatic.parseIntN(ids[0].trim())

                        val mc = MapColc.get(Data.hex(id0)) ?: continue

                        if (ids.size == 1) {
                            MultiLangCont.getStatic().MCNAME.put(l, mc, name)
                            continue
                        }

                        val id1 = CommonStatic.parseIntN(ids[1].trim())

                        if (id1 >= mc.maps.list.size || id1 < 0)
                            continue

                        val stm = mc.maps.list[id1] ?: continue

                        if (ids.size == 2) {
                            MultiLangCont.getStatic().SMNAME.put(l, stm, name)

                            continue
                        }

                        val id2 = CommonStatic.parseIntN(ids[2].trim())

                        if (id2 >= stm.list.list.size || id2 < 0)
                            continue

                        val st = stm.list.list[id2]

                        MultiLangCont.getStatic().STNAME.put(l, st, name)
                    }
                }
            }
        }

        for (l in Lang.Locale.values()) {
            val f = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$rewa")

            if (f.exists()) {
                val qs = VFile.getFile(f).data.readLine()

                if (qs != null) {
                    for (s in qs) {
                        val strs = s.trim().split("\t").toTypedArray()

                        if (strs.size <= 1)
                            continue

                        val ids = strs[0].trim().split("|")

                        val name = strs[1].trim()

                        for (id in ids) {
                            when {
                                CommonStatic.isInteger(id) -> MultiLangCont.getStatic().RWNAME.put(l, id.toInt(), name)
                                id.startsWith("S") -> MultiLangCont.getStatic().RWSTNAME.put(l, id.replace("S", "").toInt(), name)
                                id.startsWith("I") -> MultiLangCont.getStatic().RWSVNAME.put(l, id.replace("I", "").toInt(), name)
                            }
                        }
                    }
                }
            }
        }

        val f = File("${StaticStore.getExternalAsset(c)}lang/$diff")

        if (f.exists()) {
            val qs = VFile.getFile(f).data.readLine()

            if (qs != null) {
                for (s in qs) {
                    val strs = s.trim().split("\t").toTypedArray()

                    if (strs.size < 2)
                        continue

                    val num = strs[1].trim()
                    val numbers = strs[0].trim().split("-").toTypedArray()

                    if (numbers.size < 3)
                        continue

                    val id0 = CommonStatic.parseIntN(numbers[0].trim())
                    val id1 = CommonStatic.parseIntN(numbers[1].trim())
                    val id2 = CommonStatic.parseIntN(numbers[2].trim())

                    val mc = MapColc.get(Data.hex(id0)) ?: continue

                    if (id1 >= mc.maps.list.size || id1 < 0)
                        continue

                    val stm = mc.maps.list[id1] ?: continue

                    if (id2 >= stm.list.list.size || id2 < 0)
                        continue

                    val st = stm.list[id2]

                    if(st.info != null) {
                        (st.info as DefStageInfo).diff = num.toInt()
                    }
                }
            }
        }
        Definer.stagelang = true
    }

    @Synchronized
    fun readMedalLang(c: Context) {
        val medalName = "MedalName.txt"
        val medalExp = "MedalExplanation.txt"

        for (l in Lang.Locale.values()) {
            val f = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$medalName")

            if(f.exists()) {
                val qs = VFile.getFile(f).data.readLine()

                if (qs != null) {
                    for (str in qs) {
                        val strs = str.trim().split("\t").toTypedArray()

                        if (strs.size == 1) {
                            continue
                        }

                        val idText = strs[0].trim().replace(Regex("^0+"), "")

                        val id = if (idText.isBlank()) 0 else idText.toInt()
                        val name = strs[1].trim()

                        StaticStore.MEDNAME.put(l, id, name)
                    }
                }
            }

            val g = File("${StaticStore.getExternalAsset(c)}lang/${l.code}/$medalExp")

            if(g.exists()) {
                val qs = VFile.getFile(g).data.readLine()

                if (qs != null) {
                    for (str in qs) {
                        val strs = str.trim().split("\t").toTypedArray()

                        if (strs.size == 1) {
                            continue
                        }

                        val idText = strs[0].trim().replace(Regex("^0+"), "")

                        val id = if (idText.isBlank()) 0 else idText.toInt()
                        val name = strs[1].trim().replace("<br>", "\n")

                        StaticStore.MEDEXP.put(l, id, name)
                    }
                }
            }
        }
        Definer.medallang = true
    }

    private fun getComboViaID(combos: List<Combo>, id: String) : Combo? {
        for(c in combos) {
            if(c.name == id)
                return c
        }

        return null
    }
}