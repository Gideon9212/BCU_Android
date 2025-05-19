package com.g2.bcu.androidutil.filter

import android.util.SparseArray
import androidx.core.util.isNotEmpty
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.io.ErrorLogWriter
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import common.util.stage.MapColc.PackMapColc
import common.util.stage.Stage
import common.util.unit.AbEnemy

object FilterStage {
    fun setFilter(name: String, stmname: String, enemies: ArrayList<Identifier<AbEnemy>>, enemOrOp: Boolean,
        music: String, bg: String, star: Int, bh: Int, bhop: Int, contin: Int, boss: Int) : Map<String, SparseArray<ArrayList<Int>>> {
        val result = HashMap<String, SparseArray<ArrayList<Int>>>()

        println("Filtered enemy : $enemies")

        for(n in 0 until StaticStore.allMCs.size) {
            val i = StaticStore.allMCs[n]
            val m = MapColc.get(i) ?: continue
            val stresult = SparseArray<ArrayList<Int>>()

            if (noPointSearching(m, enemies, enemOrOp, music, bg))
                continue

            for(j in m.maps.list.indices) {
                val stm = m.maps.list[j] ?: continue
                if (m.getSave(false)?.unlocked(stm) == false && m.getSave(false)?.nearUnlock(stm) == false)
                    continue
                val sresult = ArrayList<Int>()
                for(k in 0 until stm.list.list.size) {
                    val s = stm.list.list[k] ?: continue
                    val nam = if(stmname != "") {
                        if(name != "") {
                            val stmnam = (MultiLangCont.get(stm) ?: stm.names.toString()).lowercase().contains(stmname.lowercase())
                            val stnam = (MultiLangCont.get(s) ?: s.names.toString()).lowercase().contains(name.lowercase())

                            stmnam && stnam
                        } else
                            (MultiLangCont.get(stm) ?: stm.names.toString()).lowercase().contains(stmname.lowercase())
                    } else
                        (MultiLangCont.get(s) ?: s.names.toString()).lowercase().contains(name.lowercase())
                    if (!nam) continue

                    val es = ArrayList<Identifier<AbEnemy>>()
                    for(d in s.data.datas) {
                        val e = d.enemy ?: continue

                        if(!es.contains(e))
                            es.add(e)
                    }
                    if (!containEnemy(enemies, es, enemOrOp))
                        continue

                    var mus = music.isEmpty()
                    if(!mus && s.mus0 != null && s.mus0.id != -1) {
                        val m0 = s.mus0.pack + " - " + Data.trio(s.mus0.id)
                        mus = m0 == music
                    }
                    if(!mus && s.mus1 != null && s.mus1.id != -1) {
                        val m1 = s.mus1.pack + " - " + Data.trio(s.mus1.id)
                        mus = m1 == music
                    }
                    if (!mus) continue

                    var backg = bg.isEmpty()
                    if(!backg && s.bg != null && s.bg.id != -1) {
                        val b = s.bg.pack + " - " + Data.trio(s.bg.id)
                        backg = bg == b
                    }
                    if (!backg) continue
                    if (stm.stars.size <= star) continue
                    val baseh = if(bh != -1) {
                        when(bhop) {
                            -1 -> true
                            0 -> s.health < bh
                            1 -> s.health == bh
                            2 -> s.health > bh
                            else -> false
                        }
                    } else
                        true
                    if (!baseh) continue
                    val cont = when(contin) {
                        -1 -> true
                        0 -> !s.non_con
                        1 -> s.non_con
                        else -> false
                    }
                    if (!cont) continue
                    val bos = when(boss) {
                        -1 -> true
                        0 -> hasBoss(s)
                        1 -> !hasBoss(s)
                        else -> false
                    }
                    if(bos)
                        sresult.add(k)
                }
                if(sresult.isNotEmpty())
                    stresult.put(j,sresult)
            }
            if(stresult.isNotEmpty())
                result[i] = stresult
        }

        return result
    }

    private fun noPointSearching(m : MapColc, enemies: ArrayList<Identifier<AbEnemy>>, enemOrOp: Boolean, music: String, bg: String) : Boolean {
        val id = if (m is MapColc.DefMapColc) Identifier.DEF else m.sid
        var cont = enemies.isNotEmpty() && enemOrOp
        for (e in enemies) {
            if (enemOrOp) {
                if (e.pack == id || (m is PackMapColc && (id == e.pack || m.pack.desc.dependency.contains(e.pack)))) {
                    cont = false
                    break
                }
            } else if (id != e.pack && (m !is PackMapColc || m.pack.desc.dependency.contains(e.pack)))
                return true
        }
        if (cont)//It'll be false for all so who care
            return true

        if (music.isNotEmpty()) {
            val m2 = music.substring(0, music.indexOf(" - "))
            if (m2 != id && (m !is PackMapColc || m.pack.desc.dependency.contains(id)))
                return true
        }
        if (bg.isNotEmpty()) {
            val b2 = bg.substring(0, bg.indexOf(" - "))
            if (b2 != id && (m !is PackMapColc || m.pack.desc.dependency.contains(id)))
                return true
        }
        return false
    }

    private fun containEnemy(src: ArrayList<Identifier<AbEnemy>>, target: List<Identifier<AbEnemy>>, orOp: Boolean) : Boolean {
        if(src.isEmpty()) return true

        if(target.isEmpty()) return false

        val targetID = ArrayList<Identifier<AbEnemy>>()

        for(ten in target) {
            if (!contains(ten, targetID)) {
                targetID.add(ten)
            }
        }

        //True for Or search

        if(orOp) {
            for(i in src)
                if(contains(i, targetID))
                    return true
            return false
        } else {
            return containsAll(src, targetID)
        }
    }

    private fun hasBoss(st: Stage) : Boolean {
        try {
            val def = st.data ?: return false

            for (i in def.datas) {
                if (i.boss >= 1)
                    return true
            }
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e)
            return false
        }

        return false
    }

    private fun contains(src: Identifier<AbEnemy>, target: ArrayList<Identifier<AbEnemy>>) : Boolean {
        src.pack ?: return false

        for(id in target) {
            id.pack ?: continue

            if(id.equals(src)) {
                return true
            }
        }

        return false
    }

    private fun containsAll(src: ArrayList<Identifier<AbEnemy>>, target: ArrayList<Identifier<AbEnemy>>) : Boolean {
        for(id in src) {
            if(!contains(id, target)) {
                return false
            }
        }

        return true
    }
}