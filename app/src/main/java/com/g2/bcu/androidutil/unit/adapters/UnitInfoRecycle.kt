package com.g2.bcu.androidutil.unit.adapters

import android.annotation.SuppressLint
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
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableRow
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
import com.g2.bcu.androidutil.supports.AnimatorConst
import com.g2.bcu.androidutil.supports.AutoMarquee
import com.g2.bcu.androidutil.supports.ScaleAnimator
import com.g2.bcu.androidutil.supports.adapter.AdapterAbil
import common.CommonStatic
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskUnit
import common.pack.Identifier
import common.util.unit.AbUnit
import common.util.unit.Enemy
import common.util.unit.Form
import common.util.unit.Level

class UnitInfoRecycle(private val context: Activity, private val names: ArrayList<String>, private val forms: Array<Form>, private val data: Identifier<AbUnit>) : RecyclerView.Adapter<UnitInfoRecycle.ViewHolder>() {
    private var frame = true
    private val s: GetStrings = GetStrings(this.context)
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private val color: IntArray = intArrayOf(
            StaticStore.getAttributeColor(context, R.attr.TextPrimary)
    )
    
    private var talents = false
    private val level = Level(8)

    private var isRaw = false
    private val talentIndex = java.util.ArrayList<Int>()
    private val superTalentIndex = java.util.ArrayList<Int>()
    private var catk = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack = itemView.findViewById<Button>(R.id.unitinfpack)!!
        val unitpack = itemView.findViewById<TextView>(R.id.unitinfpackr)!!
        val frse = itemView.findViewById<Button>(R.id.unitinffrse)!!
        val unitname: TextView = itemView.findViewById(R.id.unitinfname)
        val unitid: TextView = itemView.findViewById(R.id.unitinfidr)
        val unithp: TextView = itemView.findViewById(R.id.unitinfhpr)
        val unithb: TextView = itemView.findViewById(R.id.unitinfhbr)
        val unitlevel: Spinner = itemView.findViewById(R.id.unitinflevr)
        val unitlevelp: Spinner = itemView.findViewById(R.id.unitinflevpr)
        val unitplus: TextView = itemView.findViewById(R.id.unitinfplus)
        val uniticon: ImageView = itemView.findViewById(R.id.unitinficon)
        val unitatkb: Button = itemView.findViewById(R.id.unitinfatk)
        val unitatk: TextView = itemView.findViewById(R.id.unitinfatkr)
        val unittrait: FlexboxLayout = itemView.findViewById(R.id.unitinftraitr)
        val unitcost: TextView = itemView.findViewById(R.id.unitinfcostr)
        val unitsimu: FlexboxLayout = itemView.findViewById(R.id.unitinfsimur)
        val unitspd: TextView = itemView.findViewById(R.id.unitinfspdr)
        val unitcdb: Button = itemView.findViewById(R.id.unitinfcd)
        val unitcd: TextView = itemView.findViewById(R.id.unitinfcdr)
        val unitrang: TextView = itemView.findViewById(R.id.unitinfrangr)
        val unitpreatkb: Button = itemView.findViewById(R.id.unitinfpreatk)
        val unitpreatk: TextView = itemView.findViewById(R.id.unitinfpreatkr)
        val unitpostb: Button = itemView.findViewById(R.id.unitinfpost)
        val unitpost: TextView = itemView.findViewById(R.id.unitinfpostr)
        val unittbab: Button = itemView.findViewById(R.id.unitinftba)
        val unittba: TextView = itemView.findViewById(R.id.unitinftbar)
        val unitatktb: Button = itemView.findViewById(R.id.unitinfatktime)
        val unitatkt: TextView = itemView.findViewById(R.id.unitinfatktimer)
        val unitabilt: TextView = itemView.findViewById(R.id.unitinfabiltr)
        val none: TextView = itemView.findViewById(R.id.unitabilnone)
        val unitabil: RecyclerView = itemView.findViewById(R.id.unitinfabilr)
        val unittalen: CheckBox = itemView.findViewById(R.id.unitinftalen)
        val npresetrow: TableRow = itemView.findViewById(R.id.talresetrow)
        val npreset: Button = itemView.findViewById(R.id.unitinftalreset)
        val nprow: TableRow = itemView.findViewById(R.id.talenrow)
        val supernprow: TableRow = itemView.findViewById(R.id.supertalenrow)
        val prevatk = itemView.findViewById<Button>(R.id.btn_prevatk)
        val curatk = itemView.findViewById<TextView>(R.id.atk_index)
        val nextatk = itemView.findViewById<Button>(R.id.btn_nextatk)
        init {
            unitplus.text = " + "
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.unit_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val u = data.get() ?: return

        val cdlev = context.findViewById<TextInputLayout>(R.id.cdlev)
        val cdtrea = context.findViewById<TextInputLayout>(R.id.cdtrea)
        val atktrea = context.findViewById<TextInputLayout>(R.id.atktrea)
        val healtrea = context.findViewById<TextInputLayout>(R.id.healtrea)

        cdlev.isCounterEnabled = true
        cdlev.counterMaxLength = 2
        cdtrea.isCounterEnabled = true
        cdtrea.counterMaxLength = 3
        atktrea.isCounterEnabled = true
        atktrea.counterMaxLength = 3
        healtrea.isCounterEnabled = true
        healtrea.counterMaxLength = 3
        
        cdlev.setHelperTextColor(ColorStateList(states, color))
        cdtrea.setHelperTextColor(ColorStateList(states, color))
        atktrea.setHelperTextColor(ColorStateList(states, color))
        healtrea.setHelperTextColor(ColorStateList(states, color))

        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        frame = shared.getBoolean("frame", true)
        viewHolder.frse.text = if (frame) context.getString(R.string.unit_info_fr) else context.getString(R.string.unit_info_sec)

        val t = BasisSet.current().t()
        val f = forms[viewHolder.bindingAdapterPosition]

        level.setLevel(f.unit.preferredLevel)
        level.setPlusLevel(f.unit.preferredPlusLevel)

        val cdlevt = context.findViewById<TextInputEditText>(R.id.cdlevt)
        val cdtreat = context.findViewById<TextInputEditText>(R.id.cdtreat)
        val atktreat = context.findViewById<TextInputEditText>(R.id.atktreat)
        val healtreat = context.findViewById<TextInputEditText>(R.id.healtreat)

        cdlevt.setText(t.tech[0].toString())
        cdtreat.setText(t.trea[2].toString())
        atktreat.setText(t.trea[0].toString())
        healtreat.setText(t.trea[1].toString())

        val icon = f.anim?.uni?.img?.bimg()
        if(icon == null) viewHolder.uniticon.setImageBitmap(StaticStore.makeIcon(context, null, 48f))
        else viewHolder.uniticon.setImageBitmap(StaticStore.makeIcon(context, icon as Bitmap, 48f))

        catk = f.du.firstAtk()
        if (f.du.realAtkCount() + StaticJava.spAtkCount(f.du) == 1) {
            viewHolder.prevatk.visibility = View.GONE
            viewHolder.curatk.visibility = View.GONE
            viewHolder.nextatk.visibility = View.GONE
        }
        viewHolder.unitname.text = names[position]
        viewHolder.unitpack.text = s.getPackName(f.unit.id, isRaw)
        viewHolder.unitid.text = s.getID(viewHolder, StaticStore.trio(u.id.id))
        viewHolder.unithp.text = s.getHP(f, t, false, level)
        viewHolder.unithb.text = s.getHB(f, false, level)
        viewHolder.unitatk.text = s.getTotAtk(f, t, false, level, catk)
        setTraits(viewHolder, f)
        viewHolder.unitcost.text = s.getCost(f, false, level)
        setSimus(viewHolder, f)
        viewHolder.unitspd.text = s.getSpd(f, false, level)
        viewHolder.unitcd.text = s.getCD(f, t, frame, false, level)
        viewHolder.unitrang.text = s.getRange(f, catk, false, level)
        viewHolder.unitpreatk.text = s.getPre(f, frame, catk)
        viewHolder.unitpost.text = s.getPost(f, frame, catk)
        viewHolder.unittba.text = s.getTBA(f, false, frame, level)
        viewHolder.unitatkt.text = s.getAtkTime(f, false, frame, level, catk)
        viewHolder.unitabilt.text = s.getAbilT(f, catk)

        setAbis(viewHolder, f)

        if (f.du.pCoin == null) {
            viewHolder.unittalen.visibility = View.GONE
            viewHolder.npreset.visibility = View.GONE
            viewHolder.nprow.visibility = View.GONE
            viewHolder.supernprow.visibility = View.GONE

            for(i in level.talents.indices)
                level.talents[i] = 0

            listeners(viewHolder, arrayOf(), arrayOf())
        } else {
            for(i in f.du.pCoin.info.indices) {
                if(f.du.pCoin.getReqLv(i) > 0) superTalentIndex.add(i)
                else talentIndex.add(i)
            }

            val talent = Array(talentIndex.size) {
                val spin = Spinner(context)
                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (talentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)
                viewHolder.nprow.addView(spin)
                spin
            }

            val superTalent = Array(superTalentIndex.size) {
                val spin = Spinner(context)
                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (superTalentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)
                viewHolder.supernprow.addView(spin)
                spin
            }

            val max = f.du.pCoin.max
            for(i in max.indices)
                level.talents[i] = max[i]

            for(i in talent.indices) {
                if(talentIndex[i] >= f.du.pCoin.info.size) {
                    talent[i].isEnabled = false
                    continue
                }

                val talentLevels = java.util.ArrayList<Int>()

                for(j in 0 until max[talentIndex[i]] + 1)
                    talentLevels.add(j)

                val adapter = ArrayAdapter(context, R.layout.spinneradapter, talentLevels)

                talent[i].adapter = adapter
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))

                level.talents[talentIndex[i]] = max[talentIndex[i]]
            }

            for(i in superTalent.indices) {
                if(superTalentIndex[i] >= f.du.pCoin.info.size) {
                    superTalent[i].isEnabled = false
                    continue
                }

                val superTalentLevels = java.util.ArrayList<Int>()

                for(j in 0 until max[superTalentIndex[i]] + 1)
                    superTalentLevels.add(j)

                val adapter = ArrayAdapter(context, R.layout.spinneradapter, superTalentLevels)

                superTalent[i].adapter = adapter
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))

                level.talents[superTalentIndex[i]] = max[superTalentIndex[i]]

                if(CommonStatic.getConfig().realLevel)
                    changeSpinner(superTalent[i], level.totalLv >= f.du.pCoin.getReqLv(i))
            }

            if(superTalent.isEmpty())
                viewHolder.supernprow.visibility = View.GONE

            listeners(viewHolder, talent, superTalent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(viewHolder: ViewHolder, talent: Array<Spinner>, superTalent: Array<Spinner>) {
        val cdlev = context.findViewById<TextInputLayout>(R.id.cdlev)
        val cdtrea = context.findViewById<TextInputLayout>(R.id.cdtrea)
        val atktrea = context.findViewById<TextInputLayout>(R.id.atktrea)
        val healtrea = context.findViewById<TextInputLayout>(R.id.healtrea)
        val cdlevt = context.findViewById<TextInputEditText>(R.id.cdlevt)
        val cdtreat = context.findViewById<TextInputEditText>(R.id.cdtreat)
        val atktreat = context.findViewById<TextInputEditText>(R.id.atktreat)
        val healtreat = context.findViewById<TextInputEditText>(R.id.healtreat)
        val reset = context.findViewById<Button>(R.id.treasurereset)

        val t = BasisSet.current().t()
        val f = forms[viewHolder.bindingAdapterPosition]

        val levels: MutableList<Int> = ArrayList()
        for (j in 1 until f.unit.max + 1)
            levels.add(j)
        val levelsp = ArrayList<Int>()
        for (j in 0 until f.unit.maxp + 1)
            levelsp.add(j)

        val arrayAdapter = ArrayAdapter(context, R.layout.spinneradapter, levels)
        val arrayAdapterp = ArrayAdapter(context, R.layout.spinneradapter, levelsp)

        viewHolder.unitname.setOnLongClickListener {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, viewHolder.unitname.text)
            clipboardManager.setPrimaryClip(data)
            StaticStore.showShortMessage(context, R.string.unit_info_copied)
            true
        }

        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        level.setLevel(
            when {
                shared.getInt("default_level", 50) > f.unit.max -> f.unit.max
                f.unit.rarity != 0 -> shared.getInt("default_level", 50)
                else -> f.unit.max
            }
        )

        level.setPlusLevel(f.unit.preferredPlusLevel)

        viewHolder.unitlevel.adapter = arrayAdapter
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel, level.lv))
        
        viewHolder.unitlevelp.adapter = arrayAdapterp
        viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, level.plusLv))

        if (levelsp.size == 1) {
            viewHolder.unitlevelp.visibility = View.GONE
            viewHolder.unitplus.visibility = View.GONE
        }
        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw
            viewHolder.unitpack.text = s.getPackName(f.unit.id, isRaw)
        }

        viewHolder.frse.setOnClickListener {
            frame = !frame
            viewHolder.frse.text = if (frame) context.getString(R.string.unit_info_fr) else context.getString(R.string.unit_info_sec)

            viewHolder.unitcd.text = s.getCD(f, t, frame, talents, level)
            viewHolder.unitpreatk.text = s.getPre(f, frame, catk)
            viewHolder.unitpost.text = s.getPost(f, frame, catk)
            viewHolder.unittba.text = s.getTBA(f, talents, frame, level)
            viewHolder.unitatkt.text = s.getAtkTime(f, talents, frame, level, catk)

            if (viewHolder.unitabil.visibility != View.GONE) {
                val du = if (f.du.pCoin != null && talents) f.du.pCoin.improve(level.talents) else f.du

                val ability = Interpret.getAbi(du, fragment, 0, context)
                val abilityicon = Interpret.getAbiid(du)
                val proc = Interpret.getProc(du, !frame, false, arrayOf(1.0, 1.0).toDoubleArray(), context, catk)

                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                viewHolder.unitabil.layoutManager = linearLayoutManager
                val adapterAbil = AdapterAbil(ability, proc, abilityicon, context)
                viewHolder.unitabil.adapter = adapterAbil
                ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
            }
        }

        viewHolder.unitcdb.setOnClickListener {
            viewHolder.unitcd.text = s.getCD(f, t, !viewHolder.unitcd.text.toString().endsWith("f"), talents, level)
        }
        viewHolder.unitpreatkb.setOnClickListener {
            viewHolder.unitpreatk.text = s.getPre(f, !viewHolder.unitpreatk.text.toString().endsWith("f"), catk)
        }
        viewHolder.unitpostb.setOnClickListener {
            viewHolder.unitpost.text = s.getPost(f, !viewHolder.unitpost.text.toString().endsWith("f"), catk)
        }
        viewHolder.unittbab.setOnClickListener {
            viewHolder.unittba.text = s.getTBA(f, talents, !viewHolder.unittba.text.toString().endsWith("f"), level)
        }
        viewHolder.unitatkb.setOnClickListener {
            viewHolder.unitatkb.text = if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk)) context.getString(R.string.unit_info_dps) else context.getString(R.string.unit_info_atk)
            setAtk(viewHolder, f, t)
        }
        viewHolder.unitatktb.setOnClickListener {
            viewHolder.unitatkt.text = s.getAtkTime(f, talents, !viewHolder.unitatkt.text.toString().endsWith("f"), level, catk)
        }
        viewHolder.prevatk.setOnClickListener {
            if (--catk >= f.du.atkTypeCount) {
                while (catk >= f.du.atkTypeCount && f.du.getSpAtks(true,catk - f.du.atkTypeCount).isEmpty()) catk--
                if (catk < f.du.atkTypeCount)
                    while (f.du.getShare(catk) == 0) catk--
            } else while (f.du.getShare(catk) == 0) catk--
            changeAtk(viewHolder, f, t)
        }
        viewHolder.nextatk.setOnClickListener {
            if (++catk < f.du.atkTypeCount) {
                while (catk < f.du.atkTypeCount && f.du.getShare(catk) == 0) catk++
                if (catk >= f.du.atkTypeCount)
                    while (f.du.getSpAtks(true, catk - f.du.atkTypeCount).isEmpty()) catk++
            } else while (f.du.getSpAtks(true, catk - f.du.atkTypeCount).isEmpty()) catk++
            changeAtk(viewHolder, f, t)
        }

        viewHolder.unitlevel.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = (viewHolder.unitlevel.selectedItem ?: 1) as Int
                val levelp = (viewHolder.unitlevelp.selectedItem ?: 0) as Int
                this@UnitInfoRecycle.level.setLevel(level)

                changeLv(viewHolder, superTalent, level + levelp, f, t)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        viewHolder.unitlevelp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = (viewHolder.unitlevel.selectedItem ?: 1) as Int
                val levelp = (viewHolder.unitlevelp.selectedItem ?: 0) as Int
                this@UnitInfoRecycle.level.setPlusLevel(levelp)

                changeLv(viewHolder, superTalent, level + levelp, f, t)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cdlevt.setSelection(cdlevt.text?.length ?: 0)
        cdtreat.setSelection(cdtreat.text?.length ?: 0)
        atktreat.setSelection(atktreat.text?.length ?: 0)
        healtreat.setSelection(healtreat.text?.length ?: 0)

        cdlevt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 1..30) {
                    if (cdlev.isHelperTextEnabled) {
                        cdlev.isHelperTextEnabled = false
                        cdlev.isErrorEnabled = true
                        cdlev.error = context.getString(R.string.treasure_invalid)
                    }
                } else if (cdlev.isErrorEnabled) {
                    cdlev.error = null
                    cdlev.isErrorEnabled = false
                    cdlev.isHelperTextEnabled = true
                    cdlev.setHelperTextColor(ColorStateList(states, color))
                    cdlev.helperText = "1~30 Lv."
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 1..30)
                    t.tech[0] = text.toString().toInt()
                else
                    t.tech[0] = 1
                viewHolder.unitcd.text = s.getCD(f, t, viewHolder.unitcd.text.toString().endsWith("f"), this@UnitInfoRecycle.talents, level)
            }
        })
        cdtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (cdtrea.isHelperTextEnabled) {
                        cdtrea.isHelperTextEnabled = false
                        cdtrea.isErrorEnabled = true
                        cdtrea.error = context.getString(R.string.treasure_invalid)
                    }
                } else if (cdtrea.isErrorEnabled) {
                    cdtrea.error = null
                    cdtrea.isErrorEnabled = false
                    cdtrea.isHelperTextEnabled = true
                    cdtrea.setHelperTextColor(ColorStateList(states, color))
                    cdtrea.helperText = "0~300"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[2] = text.toString().toInt()
                } else
                    t.trea[2] = 0
                viewHolder.unitcd.text = s.getCD(f, t, viewHolder.unitcd.text.toString().endsWith("f"), this@UnitInfoRecycle.talents, level)
            }
        })
        atktreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (atktrea.isHelperTextEnabled) {
                        atktrea.isHelperTextEnabled = false
                        atktrea.isErrorEnabled = true
                        atktrea.error = context.getString(R.string.treasure_invalid)
                    }
                } else if (atktrea.isErrorEnabled) {
                    atktrea.error = null
                    atktrea.isErrorEnabled = false
                    atktrea.isHelperTextEnabled = true
                    atktrea.setHelperTextColor(ColorStateList(states, color))
                    atktrea.helperText = "0~300"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[0] = text.toString().toInt()
                } else
                    t.trea[0] = 0
                setAtk(viewHolder, f, t)
            }
        })
        healtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (healtrea.isHelperTextEnabled) {
                        healtrea.isHelperTextEnabled = false
                        healtrea.isErrorEnabled = true
                        healtrea.error = context.getString(R.string.treasure_invalid)
                    }
                } else if (healtrea.isErrorEnabled) {
                    healtrea.error = null
                    healtrea.isErrorEnabled = false
                    healtrea.isHelperTextEnabled = true
                    healtrea.setHelperTextColor(ColorStateList(states, color))
                    healtrea.helperText = "0~300"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[1] = text.toString().toInt()
                } else
                    t.trea[1] = 0
                viewHolder.unithp.text = s.getHP(f, t, this@UnitInfoRecycle.talents, level)
            }
        })

        reset.setOnClickListener {
            t.tech[0] = 30
            t.trea[0] = 300
            t.trea[1] = 300
            t.trea[2] = 300

            cdlevt.setText(t.tech[0].toString())
            cdtreat.setText(t.trea[0].toString())
            atktreat.setText(t.trea[1].toString())
            healtreat.setText(t.trea[2].toString())

            viewHolder.unitcd.text = s.getCD(f, t, viewHolder.unitcd.text.toString().endsWith("f"), talents, level)
            setAtk(viewHolder, f, t)
            viewHolder.unithp.text = s.getHP(f, t, talents, level)
        }

        viewHolder.unittalen.setOnCheckedChangeListener { _, isChecked ->
            talents = true
            validate(viewHolder, f, t)

            val from1 = if (isChecked) 0 else StaticStore.dptopx(100f, context)
            val from2 = if (isChecked) 0 else StaticStore.dptopx(48f, context)
            val from3 = if (isChecked) 0 else StaticStore.dptopx(16f, context)
            val to1 = StaticStore.dptopx(100f, context) - from1
            val to2 = StaticStore.dptopx(48f, context) - from2
            val to3 = StaticStore.dptopx(16f, context) - from3

            val anim = ScaleAnimator(viewHolder.npresetrow, AnimatorConst.Dimension.WIDTH, 300, AnimatorConst.Accelerator.DECELERATE, from1, to1)
            anim.start()
            val anim2 = ScaleAnimator(viewHolder.nprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, from2, to2)
            anim2.start()
            val anim3 = ScaleAnimator(viewHolder.nprow, AnimatorConst.Dimension.TOP_MARGIN, 300, AnimatorConst.Accelerator.DECELERATE, from3, to3)
            anim3.start()
            val anim4 = ScaleAnimator(viewHolder.supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, from2, to2)
            anim4.start()
        }

        for (i in talent.indices) {
            talent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[talentIndex[i]] = talent[i].selectedItem as Int
                    validate(viewHolder, f, t)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            talent[i].setOnLongClickListener {
                talent[i].isClickable = false
                StaticStore.showShortMessage(context, s.getTalentName(talentIndex[i], f, context))
                true
            }
        }

        for(i in superTalent.indices) {
            superTalent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[superTalentIndex[i]] = superTalent[i].selectedItem as Int
                    validate(viewHolder, f, t)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            superTalent[i].setOnLongClickListener {
                superTalent[i].isClickable = false
                StaticStore.showShortMessage(context, s.getTalentName(superTalentIndex[i], f, context))
                true
            }
        }

        viewHolder.npreset.setOnClickListener {
            val max = f.du.pCoin.max

            for(i in max.indices)
                level.talents[i] = max[i]
            for (i in talent.indices)
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))
            for (i in superTalent.indices)
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))
            validate(viewHolder, f, t)
        }
    }

    private fun getIndex(spinner: Spinner?, lev: Int): Int {
        var index = 0
        for (i in 0 until spinner!!.count)
            if (lev == spinner.getItemAtPosition(i) as Int)
                index = i

        return index
    }

    override fun getItemCount(): Int {
        return names.size
    }

    private fun validate(viewHolder: ViewHolder, f: Form, t: Treasure) {
        viewHolder.unithp.text = s.getHP(f, t, talents, level)
        viewHolder.unithb.text = s.getHB(f, talents, level)
        setAtk(viewHolder, f, t)
        viewHolder.unitcost.text = s.getCost(f, talents, level)
        viewHolder.unitcd.text = s.getCD(f, t, viewHolder.unitcd.text.toString().endsWith("f"), talents, level)
        setTraits(viewHolder, f)
        viewHolder.unitspd.text = s.getSpd(f, talents, level)
        viewHolder.unittba.text = s.getTBA(f, talents, frame, level)
        viewHolder.unitatkt.text = s.getAtkTime(f, talents, frame, level, catk)

        val lv = viewHolder.unitlevel.selectedItem as Int
        val lvp = viewHolder.unitlevelp.selectedItem as Int
        level.setLevel(lv)
        level.setPlusLevel(lvp)
        setAbis(viewHolder, f)
    }
    private fun changeAtk(viewHolder: ViewHolder, f: Form, t : Treasure) {
        setAtk(viewHolder, f, t)
        viewHolder.unitpreatk.text = s.getPre(f, frame, catk)
        viewHolder.unitpost.text = s.getPost(f, frame, catk)
        viewHolder.unitrang.text = s.getRange(f, catk, talents, level)
        setSimus(viewHolder, f)

        val fir = f.du.firstAtk()
        val tex = if (catk < f.du.atkTypeCount)
            context.getString(R.string.info_current_hit).replace("_", (catk-f.du.firstAtk()+1).toString())
        else f.du.getSpAtks(true, catk-f.du.atkTypeCount)[0].name
        viewHolder.curatk.text = tex
        val ratk = if (StaticJava.spAtkCount(f.du) == 0) f.du.realAtkCount() + fir else f.du.atkTypeCount + StaticJava.spAtkCount(f.du)
        viewHolder.nextatk.isEnabled = catk < ratk - 1
        viewHolder.prevatk.isEnabled = catk > fir

        setAbis(viewHolder, f)
    }
    private fun setSimus(viewHolder: ViewHolder, f: Form) {
        viewHolder.unitsimu.removeAllViews()
        val icns = s.getSimus(f, catk)
        for (icn in icns) {
            val icon = ImageView(context)
            icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icon.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, context)
            icon.setPadding(pad, pad, pad, pad)
            viewHolder.unitsimu.addView(icon)
        }
    }
    private fun setTraits(viewHolder: ViewHolder, f: Form) {
        viewHolder.unittrait.removeAllViews()
        val icns = s.getTrait(f, talents, level)
        for (icn in icns) {
            val icon = ImageView(context)
            icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icon.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, context)
            icon.setPadding(pad, pad, pad, pad)
            viewHolder.unittrait.addView(icon)
        }
    }
    private fun setAtk(viewHolder: ViewHolder, f : Form, t : Treasure) {
        if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
            viewHolder.unitatk.text = s.getAtk(f, t, this@UnitInfoRecycle.talents, this@UnitInfoRecycle.level, catk)
        else viewHolder.unitatk.text = s.getDPS(f, t, this@UnitInfoRecycle.talents, this@UnitInfoRecycle.level, catk)
    }
    private fun setAbis(viewHolder: ViewHolder, f : Form) {
        val du: MaskUnit = if (f.du.pCoin != null && talents) f.du.pCoin.improve(level.talents) else f.du
        val abil = Interpret.getAbi(du, fragment, 0, context)
        val proc = Interpret.getProc(du, !frame, false, arrayOf(1.0, 1.0).toDoubleArray(), context, catk)
        val abilityicon = Interpret.getAbiid(du)

        if (abil.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE
            viewHolder.unitabil.visibility = View.VISIBLE

            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            viewHolder.unitabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(abil, proc, abilityicon, context)
            viewHolder.unitabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
        } else {
            viewHolder.unitabil.visibility = View.GONE
            viewHolder.none.visibility = View.VISIBLE
        }
    }
    private fun changeLv(viewHolder: ViewHolder, superTalent: Array<Spinner>, lv : Int, f : Form, t : Treasure) {
        viewHolder.unithp.text = s.getHP(f, t, this@UnitInfoRecycle.talents, this@UnitInfoRecycle.level)
        setAtk(viewHolder, f, t)
        if(CommonStatic.getConfig().realLevel) {
            for(i in superTalent.indices)
                changeSpinner(superTalent[i], lv >= f.du.pCoin.getReqLv(superTalentIndex[i]))
            validate(viewHolder, f, t)
        }
    }
    private fun changeSpinner(spinner: Spinner, enable: Boolean) {
        spinner.isEnabled = enable
        spinner.background.alpha = if(enable) 255 else 64

        if(spinner.childCount >= 1 && spinner.getChildAt(0) is AutoMarquee)
            (spinner.getChildAt(0) as AutoMarquee).setTextColor((spinner.getChildAt(0) as AutoMarquee).textColors.withAlpha(if(enable) 255 else 64))
    }
}