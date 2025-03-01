package com.g2.bcu.androidutil.filter

object KoreanFilter {
    private const val BEGIN = 44032
    private const val END = 55203
    private const val INTERVAL = 588

    private val segments = arrayOf('ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ', ' ')

    private fun isSegments(src: String) : Boolean {
        val n = src.replace(" ", "")

        for(i in n.indices) {
            val c = n[i]
            if(!segments.contains(c)) {
                return false
            }
        }

        return true
    }

    private fun isKorean(c: Char) : Boolean {
        return c.code in BEGIN..END
    }

    private fun isSegment(c: Char) : Boolean {
        return segments.contains(c)
    }

    private fun extractSegment(src: String) : String {
        val res = StringBuilder()

        for(c in src) {
            if(isKorean(c) && !isSegment(c)) {
                res.append(segments[(c.code -BEGIN) / INTERVAL])
            } else {
                res.append(c)
            }
        }

        return res.toString()
    }

    fun filter(name: String, sch: String) : Boolean {
        return if(isSegments(sch)) {
            extractSegment(name).contains(sch.lowercase())
        } else {
            name.contains(sch.lowercase())
        }
    }
}