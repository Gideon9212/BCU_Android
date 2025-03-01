package com.g2.bcu.androidutil.filter

import com.g2.bcu.androidutil.Interpret
import com.g2.bcu.androidutil.StatFilterElement
import com.g2.bcu.androidutil.StaticStore
import common.CommonStatic
import common.battle.data.MaskEntity
import common.pack.Identifier
import common.pack.SaveData
import common.pack.SortedPackSet
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.AbUnit
import common.util.unit.Character
import common.util.unit.Trait
import common.util.unit.Unit
import java.util.Locale

object FilterEntity {
    @Synchronized
    fun setUnitFilter(pid: String): ArrayList<Identifier<AbUnit>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val result = ArrayList<Identifier<AbUnit>>()
        for (u in p.units.list) {
            if ((StaticStore.rare.isNotEmpty() && !StaticStore.rare.contains(u.rarity.toString())) || (StaticStore.entityname.isNotEmpty() && !validateName(u.forms[0])))
                continue

            for (f in u.forms) {
                val du = if (StaticStore.talents) f.maxu() else f.du
                if (validate(du) && (StatFilterElement.statFilter.isEmpty() || StatFilterElement.performFilter(f, StatFilterElement.orand))) {
                    result.add(u.id)
                    break
                }
            }
        }
        return result
    }

    @Synchronized
    fun setEnemyFilter(pid: String): ArrayList<Identifier<AbEnemy>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val result = ArrayList<Identifier<AbEnemy>>()
        for (e in p.getEnemies(false)) {
            if ((StaticStore.starred && e.de.star != 1) || !validate(e.de) || !validateName(e))
                continue
            if (StatFilterElement.statFilter.isEmpty() || StatFilterElement.performFilter(e, StatFilterElement.orand))
                result.add(e.id)
        }
        return result
    }

    @Synchronized
    fun setLuFilter(save : SaveData?) : ArrayList<Identifier<AbUnit>> {
        val result = ArrayList<Identifier<AbUnit>>()
        for(info in StaticStore.ludata) {
            val u = try { Identifier.get(info)
            } catch (_: Exception) { continue }
            if(u !is Unit || (StaticStore.rare.isNotEmpty() && !StaticStore.rare.contains(u.rarity.toString())) || (StaticStore.entityname.isNotEmpty() && !validateName(u.forms[0])))
                continue
            for(f in u.forms) {
                if (save?.locked(f) == true) break
                val du = if(StaticStore.talents) f.maxu() else f.du
                if (validate(du) && (StatFilterElement.statFilter.isEmpty() || StatFilterElement.performFilter(f, StatFilterElement.orand))) {
                    result.add(u.id)
                    break
                }
            }
        }
        return result
    }

    fun validate(de: MaskEntity) : Boolean {
        val t = de.traits
        val a = de.abi

        var check = StaticStore.empty
        if (!check)
            if (StaticStore.atksimu) check = Interpret.isType(de, 1, de.firstAtk())
            else check = Interpret.isType(de, 0, de.firstAtk())
        if (!check)
            return false

        check = StaticStore.attack.isEmpty() || !StaticStore.atkorand
        for (k in StaticStore.attack.indices) {
            check = if (StaticStore.atkorand) check or Interpret.isType(de, StaticStore.attack[k].toInt(), de.firstAtk())
            else check and Interpret.isType(de, StaticStore.attack[k].toInt(), de.firstAtk())
        }
        if (!check)
            return false

        check = StaticStore.tg.isEmpty() || !StaticStore.tgorand
        for (k in StaticStore.tg.indices) {
            check = if (StaticStore.tgorand) check or hasTrait(t, StaticStore.tg[k])
            else check and hasTrait(t, StaticStore.tg[k])
        }
        if (!check)
            return false

        check = StaticStore.ability.isEmpty() || !StaticStore.aborand
        for (k in StaticStore.ability.indices) {
            val vect = StaticStore.ability[k]

            if (vect[StaticStore.SF_TYPE] == 0) {
                val bind = a and vect[StaticStore.SF_PROC] != 0
                check = if (StaticStore.aborand) check or bind else check and bind
            } else if (vect[StaticStore.SF_TYPE] == 1) {
                check = if (StaticStore.aborand) check or getChance(vect, de)
                else check and getChance(vect, de)
            }
        }
        return check
    }
    private fun validateName(c : Character) : Boolean {
        var name = MultiLangCont.get(c) ?: c.names.toString()
        name = Data.trio(c.id.id) + " - " + name.lowercase()

        val lang = Locale.getDefault().language
        return if(CommonStatic.getConfig().langs[0].equals(CommonStatic.Lang.Locale.KR) || lang == Interpret.KO) {
            KoreanFilter.filter(name, StaticStore.entityname)
        } else {
            name.contains(StaticStore.entityname.lowercase())
        }
    }

    private fun hasTrait(traits: SortedPackSet<Trait>, t: Identifier<Trait>) : Boolean {
        for(tr in traits)
            if(tr.id.equals(t))
                return true
        return false
    }

    private fun getChance(data: ArrayList<Int>, du: MaskEntity) : Boolean {
        val item = du.proc.getArr(data[StaticStore.SF_PROC])
        return when(data[StaticStore.SF_PROC]) {
            in 0 until Data.PROC_TOT -> when (data.size) {
                StaticStore.SF_PROC+1 -> item.exists()
                StaticStore.SF_PROC+2 -> item[data[StaticStore.SF_PROC+1]] > 0
                StaticStore.SF_PROC+3 -> item.exists() && item[data[StaticStore.SF_PROC+1]] >= data[StaticStore.SF_PROC+2]
                else -> item.exists() && item[data[StaticStore.SF_PROC+1]] >= data[StaticStore.SF_PROC+2] && item[data[StaticStore.SF_PROC+1]] < data[StaticStore.SF_PROC+3]
            }
            else -> false
        }
    }
}