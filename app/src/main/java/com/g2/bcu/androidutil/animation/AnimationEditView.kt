package com.g2.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.g2.bcu.MaModelEditor
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.fakeandroid.CVGraphics
import com.g2.bcu.androidutil.supports.PP
import common.CommonStatic
import common.pack.Source
import common.system.P
import common.util.anim.AnimCE
import common.util.anim.AnimU
import common.util.anim.EAnimI
import common.util.anim.EAnimS
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("ViewConstructor")
class AnimationEditView(private val context : Context, private val data : AnimCE, night : Boolean, axis : Boolean) : View(context) {

    @JvmField
    var anim: EAnimI
    var types: Array<AnimU.UType> = when (data.id.base) {
        Source.BasePath.ANIM -> AnimU.TYPEDEF
        Source.BasePath.BG -> AnimU.BGEFFECT
        else -> AnimU.SOUL
    }
    var aind: Int = AnimU.WALK

    var started = false
    var fps: Long = 0
    var size = 1f
    var pos = PP(0f, 0f)
    private var p2: P

    private val backgroundPaint = Paint()
    private val colorPaint = Paint()
    private val bitmapPaint = Paint()
    private val range = Paint()
    private val cv: CVGraphics

    init {
        anim = if (context is MaModelEditor)
            EAnimS(data, data.mamodel)
        else
            data.getEAnim(getType())
        StaticStore.play = context !is MaModelEditor

        CommonStatic.getConfig().ref = axis

        if(CommonStatic.getConfig().viewerColor != -1) {
            backgroundPaint.color = CommonStatic.getConfig().viewerColor
            range.color = 0xFFFFFF - CommonStatic.getConfig().viewerColor
        } else {
            if (night) {
                backgroundPaint.color = 0x363636
            } else
                backgroundPaint.color = Color.WHITE

            range.color = StaticStore.getAttributeColor(context, R.attr.TextPrimary)
        }
        colorPaint.isFilterBitmap = true
        p2 = P(width.toFloat() / 2, height.toFloat() * 2f / 3f)

        cv = CVGraphics(Canvas(), colorPaint, bitmapPaint, night)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        started = true
    }

    override fun onDraw(canvas: Canvas) {
        p2 = P.newP(width / 2f + pos.x, height * 2f / 3 + pos.y)
        cv.setCanvas(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        cv.setColor(range.color)
        anim.draw(cv, p2, size)
        if (StaticStore.play) {
            anim.update(true)
            StaticStore.frame += CommonStatic.fltFpsDiv(1f)
        }
        P.delete(p2)
    }

    fun animationChanged() {
        val s = anim.sele
        anim = if (context is MaModelEditor)
            EAnimS(data, data.mamodel)
        else
            data.getEAnim(getType())
        anim.sele = s
        invalidate()
    }

    fun getType() : AnimU.UType {
        return types[aind]
    }

    fun getPartPos(i : Int) : P {
        val parts = data.mamodel.parts
        val ppos = realPos(parts, parts[i])
        return P((width / 2f + pos.x) - (ppos.x * size), (height * 2f / 3 + pos.y) - (ppos.y * size))
    }
    fun getPartRect(i : Int) : RotationRect {
        val rect = FloatArray(4)
        val parts = data.mamodel.parts
        val p = parts[i]
        val cut = data.imgcut.cuts[p[2]]

        val piv = getPartPos(i)
        val siz = realScale(parts, p, false).times(size)
        rect[0] = piv.x
        rect[1] = piv.y
        if (i != 0) {
            rect[0] -= siz.x * p[6]
            rect[1] -= siz.y * p[7]
        }
        rect[2] = rect[0] + siz.x * cut[2]
        rect[3] = rect[1] + siz.y * cut[3]
        val rr = RotationRect(piv, rect)
        rr.rotate(getAngle(parts, p, false) / 1800.0 * Math.PI)
        return rr
    }
    private fun realPos(parts : Array<IntArray>, part: IntArray): P {
        val pos = if (part == parts[0])
            P(-part[6].toFloat(), -part[7].toFloat())
        else
            P(-part[4].toFloat(), -part[5].toFloat())
        if (part[0] != -1) {
            pos.times(realScale(parts, parts[part[0]], false))
            val angle: Double = getAngle(parts, parts[part[0]], false) / 1800.0 * Math.PI
            if (angle == 0.0) {
                pos.plus(realPos(parts, parts[part[0]]))
            } else {
                val sin = sin(angle)
                val cos = cos(angle)
                val p = realPos(parts, parts[part[0]])
                pos.plus(((p.x * cos) + (p.y * sin)).toFloat(), ((p.y * cos) + (p.x * sin)).toFloat())
            }
        }
        return pos
    }
    fun realScale(parts : Array<IntArray>, part: IntArray, ignoreFirst: Boolean): P {
        val scale = if (ignoreFirst)
            P(1f, 1f)
        else
            P(part[8] / 1000f, part[9] / 1000f)
        if (part[0] != -1)
            scale.times(realScale(parts, parts[part[0]], false))
        return scale
    }
    fun getAngle(parts : Array<IntArray>, part: IntArray, ignoreDef: Boolean): Int {
        var a = if (ignoreDef) 0 else part[10]
        if (part[0] != -1)
            a += getAngle(parts, parts[part[0]], false)
        return a
    }

    class RotationRect(private val pos: P, rect: FloatArray) {

        val r = floatArrayOf(pos.x - rect[0], pos.y - rect[1], rect[2] - pos.x, rect[3] - pos.y)
        private val corners : Array<P> = Array(4) {
            P(r[(it % 2) * 2], r[(it / 2 * 2) + 1])
        }

        fun rotate(angle : Double) {
            val sin = sin(angle)
            val cos = cos(angle)

            corners[0].setTo((pos.x - (r[0] * cos) - (r[1] * sin)).toFloat(), (pos.y - (r[1] * cos) - (r[0] * sin)).toFloat())
            corners[1].setTo((pos.x + (r[2] * cos) - (r[1] * sin)).toFloat(), (pos.y - (r[1] * cos) + (r[2] * sin)).toFloat())
            corners[2].setTo((pos.x - (r[0] * cos) - (r[3] * sin)).toFloat(), (pos.y + (r[3] * cos) - (r[0] * sin)).toFloat())
            corners[3].setTo((pos.x + (r[2] * cos) - (r[3] * sin)).toFloat(), (pos.y + (r[3] * cos) + (r[2] * sin)).toFloat())
        }

        fun inBox(x : Float, y : Float) : Boolean {
            var mx = 1.0E10f
            var cx = -1.0E10f
            var my = 1.0E10f
            var cy = -1.0E10f
            for (c in corners) {
                mx = mx.coerceAtMost(c.x)
                cx = cx.coerceAtLeast(c.x)
                my = my.coerceAtMost(c.y)
                cy = cy.coerceAtLeast(c.y)
            }
            if (x < mx || x > cx || y < my || y > cy)
                return false
            return checkEdge(x, y, 0, 1) && checkEdge(x, y, 1, 3) && checkEdge(x, y, 3, 2) && checkEdge(x, y, 2, 0)
        }

        private fun checkEdge(x : Float, y : Float, c1 : Int, c2 : Int) : Boolean {
            val minx = corners[c1].x.coerceAtMost(corners[c2].x)
            val maxx = corners[c1].x.coerceAtLeast(corners[c2].x)
            val miny = corners[c1].y.coerceAtMost(corners[c2].y)
            val maxy = corners[c1].y.coerceAtLeast(corners[c2].y)
            if (minx == maxx || miny == maxy || x !in minx..maxx || y !in miny..maxy)
                return true
            val px = (x - minx) / (maxx - minx)
            val py = miny + (maxy - miny) * px

            return if (corners[c1].y > corners[c2].y)
                y <= py
            else
                y >= py
        }
    }
}