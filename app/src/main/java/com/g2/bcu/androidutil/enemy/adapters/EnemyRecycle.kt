package com.g2.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.g2.bcu.R
import com.g2.bcu.androidutil.GetStrings
import com.g2.bcu.androidutil.Interpret
import com.g2.bcu.androidutil.StaticJava
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.supports.adapter.AdapterAbil
import common.battle.BasisSet
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy

class EnemyRecycle : RecyclerView.Adapter<EnemyRecycle.ViewHolder> {
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private var activity: Activity
    private var frame = true
    private var multiplication = 100
    private var attackMultiplication = 100
    private var s: GetStrings
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private var color: IntArray
    private val data: Identifier<AbEnemy>

    private var isRaw = false
    private var catk = 0

    constructor(activity: Activity, data: Identifier<AbEnemy>) {
        this.activity = activity
        s = GetStrings(activity)
        color = intArrayOf(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
        this.data = data
    }

    constructor(activity: Activity, multi: Int, attackMultiplication: Int, data: Identifier<AbEnemy>) {
        this.activity = activity
        this.multiplication = multi
        this.attackMultiplication = attackMultiplication
        s = GetStrings(activity)
        color = intArrayOf(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
        this.data = data
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack = itemView.findViewById<Button>(R.id.eneminfpack)
        val enemyPack = itemView.findViewById<TextView>(R.id.eneminfpackr)
        val name = itemView.findViewById<TextView>(R.id.eneminfname)
        val unitSwitch = itemView.findViewById<Button>(R.id.eneminffrse)
        val enemyId = itemView.findViewById<TextView>(R.id.eneminfidr)
        val enemyIcon = itemView.findViewById<ImageView>(R.id.eneminficon)
        val enemhp = itemView.findViewById<TextView>(R.id.eneminfhpr)
        val enemhb = itemView.findViewById<TextView>(R.id.eneminfhbr)
        val enemmulti = itemView.findViewById<EditText>(R.id.eneminfmultir)
        val enematkb = itemView.findViewById<Button>(R.id.eneminfatk)
        val enematk = itemView.findViewById<TextView>(R.id.eneminfatkr)
        val enematktimeb = itemView.findViewById<Button>(R.id.eneminfatktime)
        val enematktime = itemView.findViewById<TextView>(R.id.eneminfatktimer)
        val enemabilt = itemView.findViewById<TextView>(R.id.eneminfabiltr)
        val enempreb = itemView.findViewById<Button>(R.id.eneminfpre)
        val enempre = itemView.findViewById<TextView>(R.id.eneminfprer)
        val enempostb = itemView.findViewById<Button>(R.id.eneminfpost)
        val enempost = itemView.findViewById<TextView>(R.id.eneminfpostr)
        val enemtbab = itemView.findViewById<Button>(R.id.eneminftba)
        val enemtba = itemView.findViewById<TextView>(R.id.eneminftbar)
        val enemtrait = itemView.findViewById<FlexboxLayout>(R.id.eneminftraitr)
        val enematkt = itemView.findViewById<FlexboxLayout>(R.id.eneminfatktr)
        val enemdrop = itemView.findViewById<TextView>(R.id.eneminfdropr)
        val enemrange = itemView.findViewById<TextView>(R.id.eneminfranger)
        val enembarrier = itemView.findViewById<TextView>(R.id.eneminfbarrierr)
        val enemspd = itemView.findViewById<TextView>(R.id.eneminfspdr)
        val none = itemView.findViewById<TextView>(R.id.eneminfnone)
        val emabil = itemView.findViewById<RecyclerView>(R.id.eneminfabillist)
        val enemamulti = itemView.findViewById<EditText>(R.id.eneminfamultir)
        val prevatk = itemView.findViewById<Button>(R.id.btn_prevatk)
        val curatk = itemView.findViewById<TextView>(R.id.atk_index)
        val nextatk = itemView.findViewById<Button>(R.id.btn_nextatk)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.enemy_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val em = Identifier.get(data) ?: return
        if(em !is Enemy)
            return

        val t = BasisSet.current().t()
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        frame = shared.getBoolean("frame", true)
        viewHolder.unitSwitch.text = if (frame) activity.getString(R.string.unit_info_fr) else activity.getString(R.string.unit_info_sec)

        val aclev = activity.findViewById<TextInputLayout>(R.id.aclev)
        val actrea = activity.findViewById<TextInputLayout>(R.id.actrea)
        val itfcry = activity.findViewById<TextInputLayout>(R.id.itfcrytrea)
        val cotccry = activity.findViewById<TextInputLayout>(R.id.cotccrytrea)

        val godmask = arrayOf<TextInputLayout>(activity.findViewById(R.id.godmask), activity.findViewById(R.id.godmask1), activity.findViewById(R.id.godmask2))

        val aclevt = activity.findViewById<TextInputEditText>(R.id.aclevt)
        val actreat = activity.findViewById<TextInputEditText>(R.id.actreat)
        val itfcryt = activity.findViewById<TextInputEditText>(R.id.itfcrytreat)
        val cotccryt = activity.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val godmaskt = arrayOf<TextInputEditText>(activity.findViewById(R.id.godmaskt), activity.findViewById(R.id.godmaskt1), activity.findViewById(R.id.godmaskt2))

        aclev.isCounterEnabled = true
        aclev.counterMaxLength = 2
        aclev.setHelperTextColor(ColorStateList(states, color))

        actrea.isCounterEnabled = true
        actrea.counterMaxLength = 3
        actrea.setHelperTextColor(ColorStateList(states, color))

        itfcry.isCounterEnabled = true
        itfcry.counterMaxLength = 3
        itfcry.setHelperTextColor(ColorStateList(states, color))

        cotccry.isCounterEnabled = true
        cotccry.counterMaxLength = 4
        cotccry.setHelperTextColor(ColorStateList(states, color))

        for (til in godmask) {
            til.isCounterEnabled = true
            til.counterMaxLength = 3
            til.setHelperTextColor(ColorStateList(states, color))
        }

        viewHolder.name.text = MultiLangCont.get(em) ?: em.names.toString()
        val name = StaticStore.trio(em.id.id)
        viewHolder.enemyId.text = name

        val ratio = 32f / 32f
        val img = em.anim?.edi?.img

        var b: Bitmap? = null
        if (img != null)
            b = img.bimg() as Bitmap

        catk = em.de.firstAtk()
        viewHolder.prevatk.isEnabled = false
        if (em.de.realAtkCount() + StaticJava.spAtkCount(em.de) == 1) {
            viewHolder.prevatk.visibility = View.GONE
            viewHolder.curatk.visibility = View.GONE
            viewHolder.nextatk.visibility = View.GONE
        } else
            viewHolder.curatk.text = activity.getString(R.string.info_current_hit).replace("_", (catk + 1).toString())

        multiply(viewHolder, em)

        viewHolder.enemyPack.text = s.getPackName(em.id, isRaw)
        viewHolder.enemyIcon.setImageBitmap(StaticStore.getResizeb(b, activity, 85f * ratio, 32f * ratio))
        viewHolder.enemhb.text = s.getHB(em)
        viewHolder.enemmulti.setText(multiplication.toString())
        viewHolder.enemamulti.setText(attackMultiplication.toString())

        val icns = s.getTrait(em)
        for (icn in icns) {
            val icon = ImageView(activity)
            icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icon.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, activity)
            icon.setPadding(pad, pad, pad, pad)
            viewHolder.enemtrait.addView(icon)
        }
        setSimus(viewHolder, em)
        viewHolder.enemrange.text = s.getRange(em, catk)
        viewHolder.enembarrier.text = s.getBarrier(em)
        viewHolder.enemspd.text = s.getSpd(em)

        viewHolder.enematktime.text = s.getAtkTime(em, frame, catk)
        viewHolder.enemabilt.text = s.getAbilT(em, catk)
        viewHolder.enempre.text = s.getPre(em, frame, catk)
        viewHolder.enempost.text = s.getPost(em, frame, catk)

        viewHolder.enemtba.text = s.getTBA(em, frame)
        viewHolder.enemdrop.text = s.getDrop(em, t)

        aclevt.setText(t.tech[1].toString())
        actreat.setText(t.trea[3].toString())
        itfcryt.setText(t.alien.toString())
        cotccryt.setText(t.star.toString())

        for (j in godmaskt.indices)
            godmaskt[j].setText(t.gods[j].toString())

        listeners(viewHolder)
    }

    private fun listeners(viewHolder: ViewHolder) {
        val em = data.get() ?: return

        if(em !is Enemy)
            return

        val t = BasisSet.current().t()

        val aclev = activity.findViewById<TextInputLayout>(R.id.aclev)
        val actrea = activity.findViewById<TextInputLayout>(R.id.actrea)
        val itfcry = activity.findViewById<TextInputLayout>(R.id.itfcrytrea)
        val cotccry = activity.findViewById<TextInputLayout>(R.id.cotccrytrea)

        val godmask = arrayOf<TextInputLayout>(activity.findViewById(R.id.godmask), activity.findViewById(R.id.godmask1), activity.findViewById(R.id.godmask2))

        val aclevt = activity.findViewById<TextInputEditText>(R.id.aclevt)
        val actreat = activity.findViewById<TextInputEditText>(R.id.actreat)
        val itfcryt = activity.findViewById<TextInputEditText>(R.id.itfcrytreat)
        val cotccryt = activity.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val godmaskt = arrayOf<TextInputEditText>(activity.findViewById(R.id.godmaskt), activity.findViewById(R.id.godmaskt1), activity.findViewById(R.id.godmaskt2))

        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw
            viewHolder.enemyPack.text = s.getPackName(em.id, isRaw)
        }

        viewHolder.name.setOnLongClickListener {
            val clipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val data = ClipData.newPlainText(null, viewHolder.name.text)
            clipboardManager.setPrimaryClip(data)
            StaticStore.showShortMessage(activity, R.string.enem_info_copied)
            true
        }
        viewHolder.prevatk.setOnClickListener {
            if (--catk >= em.de.atkTypeCount) {
                while (catk >= em.de.atkTypeCount && em.de.getSpAtks(true,catk - em.de.atkTypeCount).isEmpty()) catk--
                if (catk < em.de.atkTypeCount)
                    while (em.de.getShare(catk) == 0) catk--
            } else while (em.de.getShare(catk) == 0) catk--
            changeAtk(viewHolder, em)
        }
        viewHolder.nextatk.setOnClickListener {
            if (++catk < em.de.atkTypeCount) {
                while (catk < em.de.atkTypeCount && em.de.getShare(catk) == 0) catk++
                if (catk >= em.de.atkTypeCount)
                    while (em.de.getSpAtks(true, catk - em.de.atkTypeCount).isEmpty()) catk++
            } else while (em.de.getSpAtks(true, catk - em.de.atkTypeCount).isEmpty()) catk++
            changeAtk(viewHolder, em)
        }
        viewHolder.enemmulti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                multiplication = if (viewHolder.enemmulti.text.toString() == "") {
                    100
                } else {
                    if (viewHolder.enemmulti.text.toString().toDouble() > Int.MAX_VALUE) Int.MAX_VALUE
                    else Integer.valueOf(viewHolder.enemmulti.text.toString())
                }
                multiply(viewHolder, em)
            }
            override fun afterTextChanged(s: Editable) {}
        })
        viewHolder.enemamulti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                attackMultiplication = if (viewHolder.enemamulti.text.toString() == "") {
                    100
                } else {
                    if (viewHolder.enemamulti.text.toString().toDouble() > Int.MAX_VALUE) Int.MAX_VALUE
                    else Integer.valueOf(viewHolder.enemamulti.text.toString())
                }
                multiply(viewHolder, em)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        viewHolder.unitSwitch.setOnClickListener {
            frame = !frame
            retime(viewHolder, em)
            viewHolder.unitSwitch.text = if (frame) activity.getString(R.string.unit_info_fr) else activity.getString(R.string.unit_info_sec)
        }
        viewHolder.enematkb.setOnClickListener {
            viewHolder.enematkb.text = if (viewHolder.enematkb.text == activity.getString(R.string.unit_info_atk)) activity.getString(R.string.unit_info_dps) else activity.getString(R.string.unit_info_atk)
            setAtkText(viewHolder, em)
        }
        viewHolder.enempreb.setOnClickListener {
            viewHolder.enempre.text = s.getPre(em, !viewHolder.enempre.text.toString().endsWith("f"), catk)
        }
        viewHolder.enematktimeb.setOnClickListener {
            viewHolder.enematktime.text = s.getAtkTime(em, !viewHolder.enematktime.text.toString().endsWith("f"), catk)
        }
        viewHolder.enempostb.setOnClickListener {
            viewHolder.enempost.text = s.getPost(em, !viewHolder.enempost.text.toString().endsWith("f"), catk)
        }
        viewHolder.enemtbab.setOnClickListener {
            viewHolder.enemtba.text = s.getTBA(em, !viewHolder.enemtba.text.toString().endsWith("f"))
        }

        aclevt.setSelection(aclevt.text?.length ?: 0)
        actreat.setSelection(actreat.text?.length ?: 0)
        itfcryt.setSelection(itfcryt.text?.length ?: 0)
        cotccryt.setSelection(cotccryt.text?.length ?: 0)

        for (tiet in godmaskt)
            tiet.setSelection(tiet.text?.length ?: 0)

        aclevt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..30) {
                    if (aclev.isHelperTextEnabled) {
                        aclev.isHelperTextEnabled = false
                        aclev.isErrorEnabled = true
                        aclev.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (aclev.isErrorEnabled) {
                    aclev.error = null
                    aclev.isErrorEnabled = false
                    aclev.isHelperTextEnabled = true
                    aclev.setHelperTextColor(ColorStateList(states, color))
                    aclev.helperText = "1~30"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 1..30) {
                    t.tech[1] = text.toString().toInt()
                } else
                    t.tech[1] = 1
                viewHolder.enemdrop.text = s.getDrop(em, t)
            }
        })
        actreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (actrea.isHelperTextEnabled) {
                        actrea.isHelperTextEnabled = false
                        actrea.isErrorEnabled = true
                        actrea.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (actrea.isErrorEnabled) {
                    actrea.error = null
                    actrea.isErrorEnabled = false
                    actrea.isHelperTextEnabled = true
                    actrea.setHelperTextColor(ColorStateList(states, color))
                    actrea.helperText = "0~300"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[3] = text.toString().toInt()
                } else
                    t.trea[3] = 0
                viewHolder.enemdrop.text = s.getDrop(em, t)
            }
        })

        itfcryt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..600) {
                    if (itfcry.isHelperTextEnabled) {
                        itfcry.isHelperTextEnabled = false
                        itfcry.isErrorEnabled = true
                        itfcry.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (itfcry.isErrorEnabled) {
                    itfcry.error = null
                    itfcry.isErrorEnabled = false
                    itfcry.isHelperTextEnabled = true
                    itfcry.setHelperTextColor(ColorStateList(states, color))
                    itfcry.helperText = "0~600"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..600) {
                    t.alien = text.toString().toInt()
                } else
                    t.alien = 0
                multiply(viewHolder, em)
            }
        })

        cotccryt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..1500) {
                    if (cotccry.isHelperTextEnabled) {
                        cotccry.isHelperTextEnabled = false
                        cotccry.isErrorEnabled = true
                        cotccry.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (cotccry.isErrorEnabled) {
                    cotccry.error = null
                    cotccry.isErrorEnabled = false
                    cotccry.isHelperTextEnabled = true
                    cotccry.setHelperTextColor(ColorStateList(states, color))
                    cotccry.helperText = "0~1500"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..1500) {
                    t.star = text.toString().toInt()
                } else
                    t.star = 0
                multiply(viewHolder, em)
            }
        })

        for (i in godmaskt.indices) {
            godmaskt[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..100) {
                        if (godmask[i].isHelperTextEnabled) {
                            godmask[i].isHelperTextEnabled = false
                            godmask[i].isErrorEnabled = true
                            godmask[i].error = activity.getString(R.string.treasure_invalid)
                        }
                    } else if (godmask[i].isErrorEnabled) {
                        godmask[i].error = null
                        godmask[i].isErrorEnabled = false
                        godmask[i].isHelperTextEnabled = true
                        godmask[i].setHelperTextColor(ColorStateList(states, color))
                        godmask[i].helperText = "0~100"
                    }
                }
                override fun afterTextChanged(text: Editable) {
                    if (text.toString().isNotEmpty() && text.toString().toInt() in 0..100) {
                        t.gods[i] = text.toString().toInt()
                    } else
                        t.gods[i] = 0
                    multiply(viewHolder, em)
                }
            })
        }
        val reset = activity.findViewById<Button>(R.id.enemtreareset)
        reset.setOnClickListener {
            t.tech[1] = 30
            t.trea[3] = 300
            t.alien = 600
            t.star = 1500

            for (i in t.gods.indices)
                t.gods[i] = 100

            aclevt.setText(t.tech[1].toString())
            actreat.setText(t.trea[3].toString())
            itfcryt.setText(t.alien.toString())
            cotccryt.setText(t.star.toString())

            for (i in t.gods.indices)
                godmaskt[i].setText(t.gods[i].toString())

            multiply(viewHolder, em)
            viewHolder.enemdrop.text = s.getDrop(em, t)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    private fun changeAtk(viewHolder: ViewHolder, em: Enemy) {
        setAtkText(viewHolder, em)
        setSimus(viewHolder, em)
        viewHolder.enemrange.text = s.getRange(em, catk)

        val fir = em.de.firstAtk()
        val tex = if (catk < em.de.atkTypeCount)
            activity.getString(R.string.info_current_hit).replace("_", (catk-em.de.firstAtk()+1).toString())
            else em.de.getSpAtks(true, catk-em.de.atkTypeCount)[0].name
        viewHolder.curatk.text = tex
        val ratk = if (StaticJava.spAtkCount(em.de) == 0) em.de.realAtkCount() + fir else em.de.atkTypeCount + StaticJava.spAtkCount(em.de)
        viewHolder.nextatk.isEnabled = catk < ratk - 1
        viewHolder.prevatk.isEnabled = catk > fir

        retime(viewHolder, em)
    }
    private fun setSimus(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematkt.removeAllViews()
        val icns = s.getSimus(em, catk)
        for (icn in icns) {
            val icon = ImageView(activity)
            icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icon.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, activity)
            icon.setPadding(pad, pad, pad, pad)
            viewHolder.enematkt.addView(icon)
        }
    }

    private fun multiply(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enemhp.text = s.getHP(em, multiplication)
        setAtkText(viewHolder, em)
        viewHolder.enemabilt.text = s.getAbilT(em, catk)
        setAbi(viewHolder, em)
    }
    private fun setAtkText(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematk.text = if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps))
            s.getDPS(em, attackMultiplication, catk) else s.getAtk(em, attackMultiplication, catk)
    }

    private fun retime(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematktime.text = s.getAtkTime(em, frame, catk)
        viewHolder.enempre.text = s.getPre(em, frame, catk)
        viewHolder.enempost.text = s.getPost(em, frame, catk)
        viewHolder.enemtba.text = s.getTBA(em, frame)

        setAbi(viewHolder, em)
    }

    private fun setAbi(viewHolder: ViewHolder, em: Enemy) {
        val proc: List<String> = Interpret.getProc(em.de, !frame, true, arrayOf(multiplication / 100.0, attackMultiplication / 100.0).toDoubleArray(), activity, catk)
        val ability = Interpret.getAbi(em.de, fragment, 0, activity)
        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE
            viewHolder.emabil.visibility = View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            viewHolder.emabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)
            viewHolder.emabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        } else {
            viewHolder.emabil.visibility = View.GONE
            viewHolder.none.visibility = View.VISIBLE
        }
    }
}