package com.g2.bcu.androidutil.unit.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
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
import androidx.fragment.app.Fragment
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
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbUnit
import common.util.unit.Form
import common.util.unit.Level
import common.util.unit.Unit

class UnitInfoPager : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(form: Int, data: Identifier<AbUnit>, names: Array<String>): UnitInfoPager {
            val pager = UnitInfoPager()

            val bundle = Bundle()

            bundle.putString("Data", JsonEncoder.encode(data).toString())
            bundle.putInt("Form", form)
            bundle.putStringArray("Names", names)

            pager.arguments = bundle

            return pager
        }
    }

    private var form = 0
    private var frames = true
    private lateinit var s: GetStrings
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private var color: IntArray = IntArray(1)
    private var talents = false
    private val level = Level(8)
    
    private var isRaw = false
    private val talentIndex = ArrayList<Int>()
    private val superTalentIndex = ArrayList<Int>()
    private var catk = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.unit_table, container, false)
        val frse = view.findViewById<Button>(R.id.unitinffrse)
        val unitpack = view.findViewById<TextView>(R.id.unitinfpackr)
        val unitname = view.findViewById<TextView>(R.id.unitinfname)
        val unitid = view.findViewById<TextView>(R.id.unitinfidr)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unithb = view.findViewById<TextView>(R.id.unitinfhbr)
        val uniticon = view.findViewById<ImageView>(R.id.unitinficon)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unittrait = view.findViewById<FlexboxLayout>(R.id.unitinftraitr)
        val unitcost = view.findViewById<TextView>(R.id.unitinfcostr)
        val unitsimu = view.findViewById<FlexboxLayout>(R.id.unitinfsimur)
        val unitspd = view.findViewById<TextView>(R.id.unitinfspdr)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unitrang = view.findViewById<TextView>(R.id.unitinfrangr)
        val unitpreatk = view.findViewById<TextView>(R.id.unitinfpreatkr)
        val unitpost = view.findViewById<TextView>(R.id.unitinfpostr)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val unitabilt = view.findViewById<TextView>(R.id.unitinfabiltr)
        val none = view.findViewById<TextView>(R.id.unitabilnone)
        val unitabil = view.findViewById<RecyclerView>(R.id.unitinfabilr)
        val unittalen = view.findViewById<CheckBox>(R.id.unitinftalen)
        val npreset = view.findViewById<Button>(R.id.unitinftalreset)
        val nprow = view.findViewById<TableRow>(R.id.talenrow)
        val supernprow = view.findViewById<TableRow>(R.id.supertalenrow)
        val prevatk = view.findViewById<Button>(R.id.btn_prevatk)
        val curatk = view.findViewById<TextView>(R.id.atk_index)
        val nextatk = view.findViewById<Button>(R.id.btn_nextatk)
        val activity = requireActivity()
        s = GetStrings(activity)

        color = intArrayOf(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))

        val arg = arguments

        if(arg == null) {
            Log.e("UnitinfPager", "Arguments is null")
            return view
        }

        form = arg.getInt("Form")

        unitabil.isFocusableInTouchMode = false
        unitabil.isFocusable = false
        unitabil.isNestedScrollingEnabled = false

        val cdlev = activity.findViewById<TextInputLayout>(R.id.cdlev)
        val cdtrea = activity.findViewById<TextInputLayout>(R.id.cdtrea)
        val atktrea = activity.findViewById<TextInputLayout>(R.id.atktrea)
        val healtrea = activity.findViewById<TextInputLayout>(R.id.healtrea)

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

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        frames = shared.getBoolean("frame", true)
        frse.text = if (frames) activity.getString(R.string.unit_info_fr) else activity.getString(R.string.unit_info_sec)

        val t = BasisSet.current().t()
        val au = Identifier.get(StaticStore.transformIdentifier<AbUnit>(arg.getString("Data")))

        if(au == null) {
            Log.e("UnitinfPager", "Identifier is null\nArgument : ${arg.getString("Data")}")
            return view
        }
        val u = au as Unit
        val f = u.forms[form]

        level.setLevel(f.unit.preferredLevel)
        level.setPlusLevel(f.unit.preferredPlusLevel)

        val cdlevt = activity.findViewById<TextInputEditText>(R.id.cdlevt)
        val cdtreat = activity.findViewById<TextInputEditText>(R.id.cdtreat)
        val atktreat = activity.findViewById<TextInputEditText>(R.id.atktreat)
        val healtreat = activity.findViewById<TextInputEditText>(R.id.healtreat)

        cdlevt.setText(t.tech[0].toString())
        cdtreat.setText(t.trea[2].toString())
        atktreat.setText(t.trea[0].toString())
        healtreat.setText(t.trea[1].toString())

        val name = MultiLangCont.get(f) ?: f.names.toString()
        val tempIcon = f.anim?.uni?.img?.bimg()

        var icon = if(tempIcon is Bitmap) tempIcon else StaticStore.empty(StaticStore.dptopx(48f, activity),StaticStore.dptopx(48f, activity))
        icon = if (icon.height != icon.width) StaticStore.makeIcon(activity, icon, 48f)
        else StaticStore.getResizeb(icon, activity, 48f)

        uniticon.setImageBitmap(icon)
        unitname.text = name

        unitpack.text = s.getPackName(f.unit.id, isRaw)
        unitid.text = s.getID(form, StaticStore.trio(u.id.id))
        unithp.text = s.getHP(f, t, false, level)
        unithb.text = s.getHB(f, false, level)

        setUpTrait(f, unittrait)
        unitcost.text = s.getCost(f, false, level)
        unitspd.text = s.getSpd(f, false, level)
        unitcd.text = s.getCD(f, t, frames, false, level)
        unittba.text = s.getTBA(f, false, frames, level)

        catk = f.du.firstAtk()
        if (f.du.realAtkCount() + StaticJava.spAtkCount(f.du) == 1) {
            prevatk.visibility = View.GONE
            curatk.visibility = View.GONE
            nextatk.visibility = View.GONE
        }
        changeAtk(none, unitabil, unitatk, false, unitsimu, unitrang, unitpreatk, unitpost, unitatkt, unitabilt, prevatk, curatk, nextatk, f, t)

        if(f.du.pCoin != null) {
            for(i in f.du.pCoin.info.indices) {
                if(f.du.pCoin.getReqLv(i) > 0)
                    superTalentIndex.add(i)
                else
                    talentIndex.add(i)
            }

            val talent = Array(talentIndex.size) {
                val spin = Spinner(context)

                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (talentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                nprow.addView(spin)

                spin
            }

            val superTalent = Array(superTalentIndex.size) {
                val spin = Spinner(context)

                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (superTalentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                supernprow.addView(spin)

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

                val talentLevels = ArrayList<Int>()

                for(j in 0 until max[talentIndex[i]] + 1)
                    talentLevels.add(j)

                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, talentLevels)

                talent[i].adapter = adapter
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))

                level.talents[talentIndex[i]] = max[talentIndex[i]]
            }

            for(i in superTalent.indices) {
                if(superTalentIndex[i] >= f.du.pCoin.info.size) {
                    superTalent[i].isEnabled = false
                    continue
                }

                val superTalentLevels = ArrayList<Int>()
                for(j in 0 until max[superTalentIndex[i]] + 1)
                    superTalentLevels.add(j)

                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, superTalentLevels)
                superTalent[i].adapter = adapter
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))
                level.talents[superTalentIndex[i]] = max[superTalentIndex[i]]

                if(CommonStatic.getConfig().realLevel)
                    changeSpinner(superTalent[i], level.totalLv >= f.du.pCoin.getReqLv(superTalentIndex[i]))
            }

            if(superTalent.isEmpty())
                supernprow.visibility = View.GONE

            listeners(view, talent, superTalent)
        } else {
            unittalen.visibility = View.GONE
            npreset.visibility = View.GONE
            nprow.visibility = View.GONE
            supernprow.visibility = View.GONE

            for (i in 0 until level.talents.size)
                level.talents[i] = 0

            listeners(view, arrayOf(), arrayOf())
        }

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(view: View, talent: Array<Spinner>, superTalent: Array<Spinner>) {
        val activity = activity ?: return

        val cdlev = activity.findViewById<TextInputLayout>(R.id.cdlev)
        val cdtrea = activity.findViewById<TextInputLayout>(R.id.cdtrea)
        val atktrea = activity.findViewById<TextInputLayout>(R.id.atktrea)
        val healtrea = activity.findViewById<TextInputLayout>(R.id.healtrea)
        val cdlevt = activity.findViewById<TextInputEditText>(R.id.cdlevt)
        val cdtreat = activity.findViewById<TextInputEditText>(R.id.cdtreat)
        val atktreat = activity.findViewById<TextInputEditText>(R.id.atktreat)
        val healtreat = activity.findViewById<TextInputEditText>(R.id.healtreat)
        val reset = activity.findViewById<Button>(R.id.treasurereset)
        val frse = view.findViewById<Button>(R.id.unitinffrse)
        val pack = view.findViewById<Button>(R.id.unitinfpack)
        val unitpack = view.findViewById<TextView>(R.id.unitinfpackr)
        val unitname = view.findViewById<TextView>(R.id.unitinfname)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unitlevel = view.findViewById<Spinner>(R.id.unitinflevr)
        val unitlevelp = view.findViewById<Spinner>(R.id.unitinflevpr)
        val unitplus = view.findViewById<TextView>(R.id.unitinfplus)
        val unitatkb = view.findViewById<Button>(R.id.unitinfatk)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unitcdb = view.findViewById<Button>(R.id.unitinfcd)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unitpreatkb = view.findViewById<Button>(R.id.unitinfpreatk)
        val unitpreatk = view.findViewById<TextView>(R.id.unitinfpreatkr)
        val unitpostb = view.findViewById<Button>(R.id.unitinfpost)
        val unitpost = view.findViewById<TextView>(R.id.unitinfpostr)
        val unittbab = view.findViewById<Button>(R.id.unitinftba)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatktb = view.findViewById<Button>(R.id.unitinfatktime)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val unitabil = view.findViewById<RecyclerView>(R.id.unitinfabilr)
        val unittalen = view.findViewById<CheckBox>(R.id.unitinftalen)
        val npreset = view.findViewById<Button>(R.id.unitinftalreset)
        val npresetrow = view.findViewById<TableRow>(R.id.talresetrow)
        val nprow = view.findViewById<TableRow>(R.id.talenrow)
        val supernprow = view.findViewById<TableRow>(R.id.supertalenrow)

        val t = BasisSet.current().t()

        val arg = arguments ?: return

        val au = Identifier.get(StaticStore.transformIdentifier<AbUnit>(arg.getString("Data"))) ?: return
        val u = au as Unit

        val f = u.forms[form]

        unitplus.text = " + "

        val levels: MutableList<Int> = ArrayList()

        for (j in 1 until f.unit.max + 1)
            levels.add(j)

        val levelsp = ArrayList<Int>()

        for (j in 0 until f.unit.maxp + 1)
            levelsp.add(j)

        val arrayAdapter = ArrayAdapter(activity, R.layout.spinneradapter, levels)
        val arrayAdapterp = ArrayAdapter(activity, R.layout.spinneradapter, levelsp)

        unitname.setOnLongClickListener(OnLongClickListener {
            if (getActivity() == null)
                return@OnLongClickListener false

            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, unitname.text)

            clipboardManager.setPrimaryClip(data)

            StaticStore.showShortMessage(activity, R.string.unit_info_copied)

            true
        })

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        level.setLevel(
            when {
                shared.getInt("default_level", 50) > f.unit.max -> f.unit.max
                f.unit.rarity != 0 -> shared.getInt("default_level", 50)
                else -> f.unit.max
            }
        )
        level.setPlusLevel(f.unit.preferredPlusLevel)

        unitlevel.adapter = arrayAdapter
        unitlevel.setSelection(getIndex(unitlevel, level.lv))

        unitlevelp.adapter = arrayAdapterp
        unitlevelp.setSelection(getIndex(unitlevelp, level.plusLv))

        if (levelsp.size == 1) {
            unitlevelp.visibility = View.GONE
            unitplus.visibility = View.GONE
        }

        pack.setOnClickListener {
            isRaw = !isRaw
            unitpack.text = s.getPackName(f.unit.id, isRaw)
        }

        frse.setOnClickListener {
            frames = !frames
            frse.text = if (frames) activity.getString(R.string.unit_info_fr) else activity.getString(R.string.unit_info_sec)

            unitcd.text = s.getCD(f, t, frames, talents, level)
            unitpreatk.text = s.getPre(f, frames, catk)
            unitpost.text = s.getPost(f, frames, catk)
            unittba.text = s.getTBA(f, talents, frames, level)
            unitatkt.text = s.getAtkTime(f, talents, frames, level, catk)

            if (unitabil.visibility != View.GONE) {
                val du = if (talents && f.du.pCoin != null) f.du.pCoin.improve(level.talents) else f.du

                val ability = Interpret.getAbi(du, fragment, 0, activity)
                val abilityicon = Interpret.getAbiid(du)
                val proc = Interpret.getProc(du, !frames, false, arrayOf(1.0, 1.0).toDoubleArray(), requireContext())

                val linearLayoutManager = LinearLayoutManager(activity)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                unitabil.layoutManager = linearLayoutManager
                val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)
                unitabil.adapter = adapterAbil
                ViewCompat.setNestedScrollingEnabled(unitabil, false)
            }
        }

        val unitsimu = view.findViewById<FlexboxLayout>(R.id.unitinfsimur)
        val unitrang = view.findViewById<TextView>(R.id.unitinfrangr)
        val unitabilt = view.findViewById<TextView>(R.id.unitinfabiltr)
        val prevatk = view.findViewById<Button>(R.id.btn_prevatk)
        val curatk = view.findViewById<TextView>(R.id.atk_index)
        val nextatk = view.findViewById<Button>(R.id.btn_nextatk)
        val none = view.findViewById<TextView>(R.id.unitabilnone)
        prevatk.setOnClickListener {
            if (--catk >= f.du.atkTypeCount) {
                while (catk >= f.du.atkTypeCount && f.du.getSpAtks(true,catk - f.du.atkTypeCount).isEmpty()) catk--
                if (catk < f.du.atkTypeCount)
                    while (f.du.getShare(catk) == 0) catk--
            } else while (f.du.getShare(catk) == 0) catk--
            changeAtk(none, unitabil, unitatk, unitatkb.text == activity.getString(R.string.unit_info_dps), unitsimu, unitrang, unitpreatk, unitpost, unitatkt, unitabilt, prevatk, curatk, nextatk, f, t)
        }
        nextatk.setOnClickListener {
            if (++catk < f.du.atkTypeCount) {
                while (catk < f.du.atkTypeCount && f.du.getShare(catk) == 0) catk++
                if (catk >= f.du.atkTypeCount)
                    while (f.du.getSpAtks(true, catk - f.du.atkTypeCount).isEmpty()) catk++
            } else while (f.du.getSpAtks(true, catk - f.du.atkTypeCount).isEmpty()) catk++
            changeAtk(none, unitabil, unitatk, unitatkb.text == activity.getString(R.string.unit_info_dps), unitsimu, unitrang, unitpreatk, unitpost, unitatkt, unitabilt, prevatk, curatk, nextatk, f, t)
        }

        unitcdb.setOnClickListener {
            unitcd.text = s.getCD(f, t, !unitcd.text.toString().endsWith("f"), talents, level)
        }

        unitpreatkb.setOnClickListener {
            unitpreatk.text = s.getPre(f, !unitpreatk.text.toString().endsWith("f"), catk)
        }

        unitpostb.setOnClickListener {
            unitpost.text = s.getPost(f, !unitpost.text.toString().endsWith("f"), catk)
        }
        unittbab.setOnClickListener {
            unittba.text = s.getTBA(f, talents, !unittba.text.toString().endsWith("f"), level)
        }

        unitatkb.setOnClickListener {
            unitatkb.text = if (unitatkb.text == activity.getString(R.string.unit_info_atk)) activity.getString(R.string.unit_info_dps) else activity.getString(R.string.unit_info_atk)
            setAtkText(unitatk, unitatkb.text == activity.getString(R.string.unit_info_dps), f, t)
        }
        unitatktb.setOnClickListener {
            unitatkt.text = s.getAtkTime(f, talents, !unitatkt.text.toString().endsWith("f"), level, catk)
        }
        unitlevel.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val level = (unitlevel.selectedItem ?: 1) as Int
                val levelp = (unitlevelp.selectedItem ?: 0) as Int
                this@UnitInfoPager.level.setLevel(level)
                setLvStats(level+levelp, superTalent, view, unithp, unitatk, unitatkb.text == "DPS", f, t)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        unitlevelp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val level = (unitlevel.selectedItem ?: 1) as Int
                val levelp = (unitlevelp.selectedItem ?: 0) as Int
                this@UnitInfoPager.level.setPlusLevel(levelp)
                setLvStats(level+levelp, superTalent, view, unithp, unitatk, unitatkb.text == "DPS", f, t)
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
                        cdlev.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (cdlev.isErrorEnabled) {
                    cdlev.error = null
                    cdlev.isErrorEnabled = false
                    cdlev.isHelperTextEnabled = true
                    cdlev.setHelperTextColor(ColorStateList(states, color))
                    cdlev.helperText = "Lv1~30"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 1..30) {
                    t.tech[0] = text.toString().toInt()
                } else
                    t.tech[0] = 1
                unitcd.text = s.getCD(f, t, unitcd.text.toString().endsWith("f"), talents, level)
            }
        })
        cdtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (cdtrea.isHelperTextEnabled) {
                        cdtrea.isHelperTextEnabled = false
                        cdtrea.isErrorEnabled = true
                        cdtrea.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (cdtrea.isErrorEnabled) {
                    cdtrea.error = null
                    cdtrea.isErrorEnabled = false
                    cdtrea.isHelperTextEnabled = true
                    cdtrea.setHelperTextColor(ColorStateList(states, color))
                    cdtrea.helperText = "0~300 %"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    val trea = text.toString().toInt()
                    t.trea[2] = trea
                } else
                    t.trea[2] = 0
                unitcd.text = s.getCD(f, t, unitcd.text.toString().endsWith("f"), talents, level)
            }
        })
        atktreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (atktrea.isHelperTextEnabled) {
                        atktrea.isHelperTextEnabled = false
                        atktrea.isErrorEnabled = true
                        atktrea.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (atktrea.isErrorEnabled) {
                    atktrea.error = null
                    atktrea.isErrorEnabled = false
                    atktrea.isHelperTextEnabled = true
                    atktrea.setHelperTextColor(ColorStateList(states, color))
                    atktrea.helperText = "0~300 %"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[0] = text.toString().toInt()
                } else
                    t.trea[0] = 0
                setAtkText(unitatk, unitatkb.text.toString() == activity.getString(R.string.unit_info_dps), f, t)
            }
        })
        healtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.toString().toInt() !in 0..300) {
                    if (healtrea.isHelperTextEnabled) {
                        healtrea.isHelperTextEnabled = false
                        healtrea.isErrorEnabled = true
                        healtrea.error = activity.getString(R.string.treasure_invalid)
                    }
                } else if (healtrea.isErrorEnabled) {
                    healtrea.error = null
                    healtrea.isErrorEnabled = false
                    healtrea.isHelperTextEnabled = true
                    healtrea.setHelperTextColor(ColorStateList(states, color))
                    healtrea.helperText = "0~300 %"
                }
            }
            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty() && text.toString().toInt() in 0..300) {
                    t.trea[1] = text.toString().toInt()
                } else
                    t.trea[1] = 0
                unithp.text = s.getHP(f, t, talents, level)
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

            unitcd.text = s.getCD(f, t, unitcd.text.toString().endsWith("f"), talents, level)
            setAtkText(unitatk, unitatkb.text.toString() == activity.getString(R.string.unit_info_dps), f, t)
            unithp.text = s.getHP(f, t, talents, level)
        }

        unittalen.setOnCheckedChangeListener { _, isChecked ->
            talents = isChecked
            validate(view, f, t)
            val from1 = if (isChecked) 0 else StaticStore.dptopx(100f, activity)
            val from2 = if (isChecked) 0 else StaticStore.dptopx(48f, activity)
            val from3 = if (isChecked) 0 else StaticStore.dptopx(16f, activity)
            val to1 = StaticStore.dptopx(100f, activity) - from1
            val to2 = StaticStore.dptopx(48f, activity) - from2
            val to3 = StaticStore.dptopx(16f, activity) - from3
            ScaleAnimator(npresetrow, AnimatorConst.Dimension.WIDTH, 300, AnimatorConst.Accelerator.DECELERATE, from1, to1).start()
            ScaleAnimator(nprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, from2, to2).start()
            ScaleAnimator(nprow, AnimatorConst.Dimension.TOP_MARGIN, 300, AnimatorConst.Accelerator.DECELERATE, from3, to3).start()
            ScaleAnimator(supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, from2, to2).start()
        }
        for (i in talent.indices) {
            talent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[talentIndex[i]] = talent[i].selectedItem as Int
                    validate(view, f, t)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            talent[i].setOnLongClickListener {
                talent[i].isClickable = false
                StaticStore.showShortMessage(activity, s.getTalentName(talentIndex[i], f, activity))
                true
            }
        }
        for(i in superTalent.indices) {
            superTalent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[superTalentIndex[i]] = superTalent[i].selectedItem as Int
                    validate(view, f, t)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            superTalent[i].setOnLongClickListener {
                superTalent[i].isClickable = false
                StaticStore.showShortMessage(activity, s.getTalentName(superTalentIndex[i], f, activity))
                true
            }
        }
        npreset.setOnClickListener {
            val max = f.du.pCoin.max
            for(i in max.indices)
                level.talents[i] = max[i]
            for (i in talent.indices)
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))
            for (i in superTalent.indices)
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))
            validate(view, f, t)
        }
    }

    private fun validate(view: View, f: Form, t: Treasure) {
        val activity = activity ?: return

        val unithb = view.findViewById<TextView>(R.id.unitinfhbr)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unitlevel = view.findViewById<Spinner>(R.id.unitinflevr)
        val unitlevelp = view.findViewById<Spinner>(R.id.unitinflevpr)
        val unitatkb = view.findViewById<Button>(R.id.unitinfatk)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unittrait = view.findViewById<FlexboxLayout>(R.id.unitinftraitr)
        val unitcost = view.findViewById<TextView>(R.id.unitinfcostr)
        val unitspd = view.findViewById<TextView>(R.id.unitinfspdr)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val none = view.findViewById<TextView>(R.id.unitabilnone)
        val unitabil = view.findViewById<RecyclerView>(R.id.unitinfabilr)

        val level = unitlevel.selectedItem as Int
        val levelp = unitlevelp.selectedItem as Int
        this.level.setLevel(level)
        this.level.setPlusLevel(levelp)

        unithp.text = s.getHP(f, t, talents, this.level)
        unithb.text = s.getHB(f, talents, this.level)
        setAtkText(unitatk, unitatkb.text.toString() == activity.getString(R.string.unit_info_dps), f, t)
        unitcost.text = s.getCost(f, talents, this.level)
        unitcd.text = s.getCD(f, t, unitcd.text.toString().endsWith("f"), talents, this.level)
        setUpTrait(f, unittrait)
        unitspd.text = s.getSpd(f, talents, this.level)
        unittba.text = s.getTBA(f, talents, frames, this.level)
        unitatkt.text = s.getAtkTime(f, talents, frames, this.level, catk)

        val du: MaskUnit = if (f.du.pCoin != null && talents) f.du.pCoin.improve(this.level.talents) else f.du
        setAbility(none, unitabil, du)
    }

    private fun getIndex(spinner: Spinner?, lev: Int): Int {
        var index = 0
        for (i in 0 until spinner!!.count)
            if (lev == spinner.getItemAtPosition(i) as Int) index = i
        return index
    }

    private fun changeSpinner(spinner: Spinner, enable: Boolean) {
        spinner.isEnabled = enable
        spinner.background.alpha = if(enable) 255 else 64

        if(spinner.childCount >= 1 && spinner.getChildAt(0) is AutoMarquee)
            (spinner.getChildAt(0) as AutoMarquee).setTextColor((spinner.getChildAt(0) as AutoMarquee).textColors.withAlpha(spinner.background.alpha))
    }
    private fun setUpTrait(f : Form, unittrait : FlexboxLayout) {
        unittrait.removeAllViews()
        val icns = s.getTrait(f, talents, level)
        for (icn in icns) {
            val iconn = ImageView(activity)
            iconn.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            iconn.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, activity)
            iconn.setPadding(pad, pad, pad, pad)
            unittrait.addView(iconn)
        }
    }
    private fun setLvStats(lvt : Int, superTalent: Array<Spinner>, view: View, unithp : TextView, unitatk : TextView, showDPS : Boolean, f : Form, t : Treasure) {
        unithp.text = s.getHP(f, t, talents, this@UnitInfoPager.level)
        setAtkText(unitatk, showDPS, f, t)

        if(CommonStatic.getConfig().realLevel) {
            for(i in superTalent.indices)
                changeSpinner(superTalent[i], lvt >= f.du.pCoin.getReqLv(superTalentIndex[i]))
            validate(view, f, t)
        }
    }
    private fun setAtkText(unitatk : TextView, showDPS : Boolean, f : Form, t : Treasure) {
        if (showDPS) unitatk.text = s.getDPS(f, t, talents, this@UnitInfoPager.level, catk)
        else unitatk.text = s.getAtk(f, t, talents, this@UnitInfoPager.level, catk)
    }
    private fun changeAtk(none:TextView, unitabil:RecyclerView, unitatk:TextView, dps:Boolean, unitsimu:FlexboxLayout, unitrang:TextView, unitpreatk:TextView,
                          unitpost:TextView, unitatkt:TextView, unitabilt:TextView, prevatk:TextView, curatk:TextView, nextatk:TextView, f:Form, t:Treasure) {
        val activity = this.activity ?: return
        setAtkText(unitatk, dps, f, t)
        unitsimu.removeAllViews()
        val icns = s.getSimus(f, catk)
        for (icn in icns) {
            val icon = ImageView(activity)
            icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icon.setImageBitmap(icn)
            val pad = StaticStore.dptopx(1f, activity)
            icon.setPadding(pad, pad, pad, pad)
            unitsimu.addView(icon)
        }
        unitrang.text = s.getRange(f, catk, talents, level)
        unitpreatk.text = s.getPre(f, frames, catk)
        unitpost.text = s.getPost(f, frames, catk)
        unitatkt.text = s.getAtkTime(f, talents, frames, level, catk)
        unitabilt.text = s.getAbilT(f, catk)

        val fir = f.du.firstAtk()
        val tex = if (catk < f.du.atkTypeCount)
            activity.getString(R.string.info_current_hit).replace("_", (catk-f.du.firstAtk()+1).toString())
        else f.du.getSpAtks(true, catk-f.du.atkTypeCount)[0].name
        curatk.text = tex
        val ratk = if (StaticJava.spAtkCount(f.du) == 0) f.du.realAtkCount() + fir else f.du.atkTypeCount + StaticJava.spAtkCount(f.du)
        nextatk.isEnabled = catk < ratk - 1
        prevatk.isEnabled = catk > fir
        val du: MaskUnit = if (f.du.pCoin != null && talents) f.du.pCoin.improve(this.level.talents) else f.du
        setAbility(none, unitabil, du)
    }
    private fun setAbility(none : TextView, unitabil : RecyclerView, du : MaskUnit) {
        val activity = this.activity ?: return
        val ability = Interpret.getAbi(du, fragment, 0, activity)
        val abilityicon = Interpret.getAbiid(du)
        val proc = Interpret.getProc(du, !frames, false, arrayOf(1.0, 1.0).toDoubleArray(), requireContext(), catk)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            none.visibility = View.GONE
            unitabil.visibility = View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            unitabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)
            unitabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(unitabil, true)
        } else {
            unitabil.visibility = View.GONE
            none.visibility = View.VISIBLE
        }
    }
}