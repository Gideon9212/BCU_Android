package com.g2.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.g2.bcu.androidutil.supports.PP
import common.util.anim.AnimCE

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class SpriteView(context: Context, val anim : AnimCE) : View(context) {

    var sele : Int = -1
    private var initzoom : Float = 1f
    var zoom : Float = 1f
    var pos : PP = PP(0f, 0f)
    private var cwhite : Boolean = true

    val paint : Paint = Paint()
    val bitPaint : Paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    val m: Matrix = Matrix()
    lateinit var postMove : (sav : String) -> Unit
    lateinit var selectionChanged : () -> Unit

    init {
        val scaleListener = ScaleListener(this@SpriteView)
        val detector = ScaleGestureDetector(context, scaleListener)
        setOnTouchListener(object : OnTouchListener {
            var preid = -1
            var spriteSelected = false
            var spriteMoved = false
            var preX = 0f
            var preY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                detector.onTouchEvent(event)
                if (preid == -1)
                    preid = event.getPointerId(0)

                val id = event.getPointerId(0)

                val x = event.x
                val y = event.y

                if (event.action == MotionEvent.ACTION_DOWN) {
                    spriteSelected = false
                    if (sele != -1) {
                        val cut = anim.imgcut.cuts[sele]
                        val sx = -pos.x + (cut[0] * zoom - 1)
                        val sy = -pos.y + (cut[1] * zoom - 1)
                        val sw = sx + (cut[2] * zoom + 2)
                        val sh = sy + (cut[3] * zoom + 2)
                        if (!(x in sx..sw && y in sy..sh)) {
                            sele = -1
                            selectionChanged()
                            limit()
                        }
                    } else {
                        val ic = anim.imgcut
                        for (i in 0 until ic.n) {
                            val cut = ic.cuts[i]
                            val sx = -pos.x + (cut[0] * zoom - 1)
                            val sy = -pos.y + (cut[1] * zoom - 1)
                            val sw = sx + (cut[2] * zoom + 2)
                            val sh = sy + (cut[3] * zoom + 2)

                            if (x in sx..sw && y in sy..sh) {
                                sele = i
                                scaleListener.setCut(cut)
                                selectionChanged()
                                spriteSelected = true
                                limit()
                                break
                            }
                        }
                    }
                    scaleListener.updateScale = true
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    if (event.pointerCount == 1 && id == preid) {
                        var dx = x - preX
                        var dy = y - preY

                        if (sele == -1) {
                            pos.x -= dx
                            pos.y -= dy
                        } else {
                            dx /= zoom
                            dy /= zoom
                            anim.imgcut.cuts[sele][0] += dx.toInt()
                            anim.imgcut.cuts[sele][1] += dy.toInt()
                            if (dx.toInt() != 0 || dy.toInt() != 0) {
                                spriteMoved = true
                                postMove("")
                            }
                        }
                        if (dx != 0f || dy != 0f)
                            limit()
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (spriteMoved) {
                        spriteMoved = false
                        postMove("imgcut move part $sele")
                    } else if (!spriteSelected) {
                        var selected = -1
                        val ic = anim.imgcut
                        for (i in 0 until ic.n) {
                            if (sele == i)
                                continue
                            val cut = ic.cuts[i]
                            val sx = -pos.x + (cut[0] * zoom - 1)
                            val sy = -pos.y + (cut[1] * zoom - 1)
                            val sw = sx + (cut[2] * zoom + 2)
                            val sh = sy + (cut[3] * zoom + 2)

                            if (x in sx..sw && y in sy..sh) {
                                selected = i
                                break
                            }
                        }
                        sele = selected
                        selectionChanged()
                        limit()
                    } else if (sele != -1 && scaleListener.scaled())
                        postMove("imgcut scale part $sele")
                }

                preX = x
                preY = y

                preid = id

                return true
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        calculateSize(true)
    }

    override fun onDraw(c: Canvas) {
        calculateSize(false)
        val spr : Bitmap = anim.num.bimg() as Bitmap

        val aw: Int = spr.getWidth()
        val ah: Int = spr.getHeight()
        val rw: Int = (zoom * aw).toInt()
        val rh: Int = (zoom * ah).toInt()
        val bw : Float = if (rw % 20 != 0) 20f * (1 + rw / 20) else rw.toFloat()
        val bh : Float = if (rh % 20 != 0) 20f * (1 + rh / 20) else rh.toFloat()

        m.reset()
        c.setMatrix(m)

        paint.style = Paint.Style.FILL
        paint.color = if (cwhite) Color.WHITE else Color.DKGRAY
        c.drawRect(0f, 0f, bw, bh, paint)

        paint.color = Color.LTGRAY
        var wi = 0f
        while (wi < rw) {
            var hi = (wi / 20).toInt() % 2 * 20f
            while (hi < rh) {
                c.drawRect(wi, hi, wi + 20f, hi + 20f, paint)
                hi += 40
            }
            wi += 20
        }

        m.preTranslate(-pos.x, -pos.y)
        m.preScale(zoom, zoom)
        c.drawBitmap(spr, m, bitPaint)

        val ic = anim.imgcut
        for (i in 0 until ic.n) {
            val cut = ic.cuts[i]
            val sx = -pos.x + (cut[0] * zoom - 1)
            val sy = -pos.y + (cut[1] * zoom - 1)
            val sw = sx + (cut[2] * zoom + 2)
            val sh = sy + (cut[3] * zoom + 2)
            if (i == sele) {
                paint.color = Color.RED
                paint.style = Paint.Style.FILL
                c.drawRect(sx - 5, sy - 5, sx, sh + 5, paint)//left
                c.drawRect(sx - 5, sy - 5, sw, sy, paint)//top
                c.drawRect(sw, sy - 5, sw + 5, sh + 5, paint)//right
                c.drawRect(sx, sh, sw, sh + 5, paint)
            } else {
                paint.color = Color.BLACK
                paint.style = Paint.Style.STROKE
                c.drawRect(sx, sy, sw, sh, paint)
            }
        }
    }

    fun limit() {
        if (zoom <= initzoom)
            zoom = initzoom

        val spriteW = (anim.num.width * zoom).toInt()
        val spriteH = (anim.num.height * zoom).toInt()

        if (pos.x < 0 || width >= spriteW) {
            pos.x = 0f
        } else if (pos.x + width >= spriteW)
            pos.x = (spriteW - width).toFloat().coerceAtLeast(0f)

        if (pos.y < 0 || height >= spriteH) {
            pos.y = 0f
        } else if (pos.y + height >= spriteH)
            pos.y = (spriteH - height).toFloat().coerceAtLeast(0f)
        invalidate()
    }

    fun calculateSize(first: Boolean) {
        val img: Bitmap = anim.num.bimg() as Bitmap

        val spriteW: Int = img.getWidth()
        val spriteH: Int = img.getHeight()

        val w = width
        val h = height

        initzoom = (1f * w / spriteW).coerceAtMost(1f * h / spriteH)
        if (first || zoom == 0f) {
            zoom = initzoom
            limit()
        }
    }

    inner class ScaleListener(private val cView: SpriteView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.scaleFactor == 1f)
                return true

            if (cView.sele != -1) {
                val cut = cView.anim.imgcut
                cut.cuts[cView.sele][2] = (cut.cuts[cView.sele][2] * detector.scaleFactor).toInt()
                cut.cuts[cView.sele][3] = (cut.cuts[cView.sele][3] * detector.scaleFactor).toInt()
                cView.postMove("")
            } else {
                cView.zoom *= detector.scaleFactor
                val diffX = realFX * (cView.zoom / previousScale)
                val diffY = realFY * (cView.zoom / previousScale)

                cView.pos.x = previousX + diffX
                cView.pos.y = previousY + diffY
            }
            cView.limit()

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                if (cView.sele != -1) {
                    setCut(cView.anim.imgcut.cuts[cView.sele])
                } else {
                    realFX = detector.focusX - (cView.width / 2f)
                    previousX = cView.pos.x

                    realFY = detector.focusY - (cView.height * 2f / 3f)
                    previousY = cView.pos.y
                }
                previousScale = cView.zoom
                updateScale = false
            }
            return super.onScaleBegin(detector)
        }

        fun setCut(cut : IntArray) {
            realFX = cut[2].toFloat()
            realFY = cut[3].toFloat()
        }

        fun scaled() : Boolean {
            val cut = cView.anim.imgcut.cuts[cView.sele]
            return cut[2] != realFX.toInt() || cut[3] != realFY.toInt()
        }
    }
}