package com.g2.bcu.androidutil

import android.util.Log
import common.battle.BasisSet
import common.battle.data.MaskEntity
import common.util.Data
import common.util.unit.Enemy
import common.util.unit.Form
import kotlin.math.roundToInt

class StatFilterElement(val type: Int, val option: Int, val lev: Int) {
    companion object {
        const val HP = 0
        const val ATK = 1
        const val DPS = 2
        const val HB = 3
        const val RANGE = 4
        const val COSTDROP = 5
        const val SPEED = 6
        const val CD = 7
        const val BARRIER = 8
        const val ATKTIME = 9
        const val PREATK = 10
        const val POSTATK = 11
        const val TBA = 12
        const val ATKCOUNT = 13

        const val OPTION_GREAT = 0
        const val OPTION_EQUAL = 1
        const val OPTION_LESS = 2

        val statFilter = ArrayList<StatFilterElement>()
        var orand = true
        var started = false
        var show = false
        var talent = false
        var curAtk = 0

        fun performFilter(entity: Form, orand: Boolean) : Boolean {
            var result = !orand
            for(stat in statFilter)
                result = stat.setFilter(entity, result, orand)

            return result
        }

        fun performFilter(entity: Enemy, orand: Boolean) : Boolean {
            var result = !orand
            for(stat in statFilter)
                result = stat.setFilter(entity, result, orand)

            return result
        }

        fun canBeAdded(type: Int, option: Int, lev: Int) : Boolean {
            for(element in statFilter)
                if(element.type == type && element.option == option && element.lev == lev)
                    return false
            return true
        }

        fun canBeAdded(type: Int) : Boolean {
            val less = ArrayList<Int>()
            val equal = ArrayList<Int>()
            val great = ArrayList<Int>()

            for(element in statFilter) {
                if(element.type == type) {
                    when(element.option) {
                        OPTION_LESS -> {
                            if(!less.contains(element.lev))
                                less.add(element.lev)
                        }
                        OPTION_EQUAL -> {
                            if(!equal.contains(element.lev))
                                equal.add(element.lev)
                        }
                        OPTION_GREAT -> {
                            if(!great.contains(element.lev))
                                great.add(element.lev)
                        }
                    }
                }
            }
            return !(less.size == 60 && equal.size == 60 && great.size == 60)
        }

        fun canBeAdded(type: Int, option: Int) : Boolean {
            val res = ArrayList<Int>()

            for(element in statFilter) {
                if(element.type == type && element.option == option) {
                    if(!res.contains(element.lev))
                        res.add(element.lev)
                }
            }

            return res.size != 50
        }
    }

    init {
        Log.i("STAT","TYPE : $type | OPT : $option | LEV : $lev")
        statFilter.add(this)
    }

    var data: Int = 0
    var delete: Boolean = false

    fun setFilter(entity: Form, result: Boolean, orand: Boolean) : Boolean {
        val t = BasisSet.current().t()
        val du = if(entity.du.pCoin != null) {
            if(talent)
                entity.maxu()
            else
                entity.du
        } else {
            entity.du
        }

        val l = entity.unit.max.coerceAtMost(lev)
        when(type) {
            HP -> {
                val hp = if(talent && entity.du.pCoin != null) {
                    (((du.hp * entity.unit.lv.getMult(l)).roundToInt() * t.defMulti).toInt() * entity.du.pCoin.getStatMultiplication(Data.PC2_HP, entity.du.pCoin.max)).toInt()
                } else {
                    ((du.hp * entity.unit.lv.getMult(l)).roundToInt() * t.defMulti).toInt()
                }

                return performData(result, orand, hp)
            }
            ATK -> {
                val atk = if(talent && entity.du.pCoin != null) {
                    (((du.allAtk(curAtk) * entity.unit.lv.getMult(l)).roundToInt() * t.atkMulti).toInt() * entity.du.pCoin.getStatMultiplication(Data.PC2_ATK, entity.du.pCoin.max)).toInt()
                } else {
                    ((du.allAtk(curAtk) * entity.unit.lv.getMult(l)).roundToInt() * t.atkMulti).toInt()
                }

                return performData(result, orand, atk)
            }
            DPS -> {
                val dps = (du.allAtk(curAtk) * t.atkMulti * entity.unit.lv.getMult(l) / du.getItv(curAtk) * 30).toInt()

                return performData(result, orand, dps)
            }
            COSTDROP -> {
                return performData(result, orand, (du.price * 1.5).toInt())
            }
            CD -> {
                return performData(result, orand, t.getFinRes(du.respawn, 0))
            }
            else -> {
                return setFilter(du, result, orand)
            }
        }
    }

    fun setFilter(entity: Enemy, result : Boolean, orand: Boolean) : Boolean {
        when(type) {
            HP -> {
                return performData(result, orand, entity.de.hp * lev / 100)
            }
            ATK -> {
                return performData(result, orand, entity.de.allAtk(curAtk) * lev / 100)
            }
            DPS -> {
                val dps = (entity.de.allAtk(curAtk) * lev / 100 / (entity.de.getItv(curAtk) / 30))
                return performData(result, orand, dps)
            }
            COSTDROP -> {
                return performData(result, orand, entity.de.drop)
            } else -> {
                return setFilter(entity.de, result, orand)
            }
        }
    }

    fun setFilter(mask: MaskEntity, result: Boolean, orand: Boolean) : Boolean {
        when(type) {
            HB -> {
                return performData(result, orand, mask.hb)
            }
            RANGE -> {
                return performData(result, orand, mask.range)
            }
            SPEED -> {
                return performData(result, orand, mask.speed)
            }
            BARRIER -> {
                return performData(result, orand, mask.proc.BARRIER.health)
            }
            ATKTIME -> {
                return performData(result, orand, mask.getItv(curAtk))
            }
            PREATK -> {
                var preatk = 0
                for(data in mask.getAtks(curAtk)) {
                    preatk += data.pre
                }

                return performData(result, orand, preatk)
            }
            POSTATK -> {
                return performData(result, orand, mask.getPost(false, curAtk))
            }
            TBA -> {
                return performData(result, orand, mask.tba)
            }
            ATKCOUNT -> {
                return performData(result, orand, mask.getAtkCount(curAtk))
            }
            else -> {
                return false
            }
        }
    }

    private fun performData(result: Boolean, orand: Boolean, value: Int) : Boolean {
        val res = when(option) {
            OPTION_GREAT -> data < value
            OPTION_EQUAL -> data == value
            OPTION_LESS -> data > value
            else -> false
        }

        //True for or, false for and
        return if(orand) {
            result || res
        } else {
            result && res
        }
    }
}