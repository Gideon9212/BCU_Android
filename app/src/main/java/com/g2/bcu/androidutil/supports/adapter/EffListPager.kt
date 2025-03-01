package com.g2.bcu.androidutil.supports.adapter

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.g2.bcu.ImageViewer
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import com.g2.bcu.androidutil.animation.AnimationCView
import common.CommonStatic
import common.pack.UserProfile
import common.util.Data
import common.util.anim.AnimI
import common.util.pack.EffAnim
import common.util.pack.NyCastle

class EffListPager<T> : Fragment() where T : AnimI<*, *> {
    companion object {
        fun <T> newInstance(type: AnimationCView.AnimationType): EffListPager<T> where T : AnimI<*, *> {
            val res = EffListPager<T>()
            val bundle = Bundle()

            bundle.putString("type", type.name)

            res.arguments = bundle

            return res
        }

        private val effID = intArrayOf(R.string.eff_weak, R.string.eff_weake, R.string.eff_str
                , R.string.eff_stre, R.string.eff_slow, R.string.eff_slowe, R.string.eff_freeze
                , R.string.eff_freezee, R.string.eff_surv, R.string.eff_surve, R.string.eff_imwp
                , R.string.eff_imwpe, R.string.eff_imwv, R.string.eff_imwve, R.string.eff_wvsh
                , R.string.eff_wvshe, R.string.eff_wvgu, R.string.eff_wvgue, R.string.eff_imm
                , R.string.eff_imdef, R.string.eff_zk, R.string.eff_barrier, R.string.eff_barriere
                , R.string.eff_warp, R.string.eff_warpe, R.string.eff_cu, R.string.eff_zde
                , R.string.eff_bw, R.string.eff_cr, R.string.eff_kb, R.string.eff_sn
                , R.string.eff_zd, R.string.eff_seal, R.string.eff_seale
                , R.string.eff_vdef, R.string.eff_vt1, R.string.eff_vt1e, R.string.eff_vt2, R.string.eff_vt3
                , R.string.eff_vdefn, R.string.eff_vt1n, R.string.eff_vt2n, R.string.eff_vt3n
                , R.string.eff_svb , R.string.eff_inv, R.string.eff_tox, R.string.eff_vol, R.string.eff_vole
                , R.string.eff_cursee, R.string.eff_wv, R.string.eff_wve, R.string.eff_arm
                , R.string.eff_arme, R.string.eff_has, R.string.eff_hase, R.string.eff_weau
                , R.string.eff_weaue, R.string.eff_minwv, R.string.eff_minwve, R.string.eff_atksmoke
                , R.string.eff_whitesmok, R.string.eff_heal, R.string.eff_heale, R.string.eff_ds, R.string.eff_dse
                , R.string.eff_cou, R.string.eff_coue, R.string.eff_cut, R.string.eff_cute, R.string.eff_cap, R.string.eff_cape
                , R.string.eff_misu, R.string.eff_misue, R.string.eff_cosu, R.string.eff_cosue, R.string.eff_guar, R.string.eff_meki
                , R.string.eff_mekie
        )

        val canonID = intArrayOf(R.string.eff_defc, R.string.eff_slowc, R.string.eff_wallc
                , R.string.eff_stopc, R.string.eff_waterc, R.string.eff_zombc, R.string.eff_breackc
                , R.string.eff_cursec)
    }

    private var type = AnimationCView.AnimationType.UNIT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        val key = arguments?.getString("type") ?: return view
        type = AnimationCView.AnimationType.valueOf(key)

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val data = when(type) {
            AnimationCView.AnimationType.EFFECT -> {
                ArrayList<EffAnim<*>>(CommonStatic.getBCAssets().effas.values().toMutableList())
            }
            AnimationCView.AnimationType.SOUL -> {
                UserProfile.getBCData().souls.list
            }
            AnimationCView.AnimationType.CANNON -> {
                ArrayList<NyCastle>(CommonStatic.getBCAssets().atks.toMutableList())
            }
            AnimationCView.AnimationType.DEMON_SOUL -> {
                CommonStatic.getBCAssets().demonSouls
            }
            else -> {
                throw IllegalStateException("Invalid type $type in EffListPager")
            }
        }

        val name = ArrayList<String>()

        when(type) {
            AnimationCView.AnimationType.EFFECT -> {
                for(id in data.indices) {
                    name.add(if(id >= effID.size)
                        generateEffName(id)
                    else
                        generateEffName(id, effID[id]))
                }
            }
            AnimationCView.AnimationType.SOUL -> {
                for(i in data.indices) {
                    name.add(requireContext().getString(R.string.eff_soul)+" - "+Data.trio(i))
                }
            }
            AnimationCView.AnimationType.CANNON -> {
                for(i in data.indices) {
                    name.add(requireContext().getString(R.string.eff_cannon) + " - "+Data.trio(i) + " : " + requireContext().getString(canonID[i]))
                }
            }
            AnimationCView.AnimationType.DEMON_SOUL -> {
                for(i in data.indices) {
                    name.add(requireContext().getString(R.string.eff_akusoul) + " - " + Data.trio(i))
                }
            }
            else -> {

            }
        }

        if(data.isEmpty()) {
            list.visibility = View.GONE
        } else {
            nores.visibility = View.GONE

            val adapter = ArrayAdapter(requireContext(), R.layout.list_layout_text, name.toTypedArray())

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->
                if(SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.bglistClick = SystemClock.elapsedRealtime()

                val intent = Intent(requireActivity(), ImageViewer::class.java)

                when(type) {
                    AnimationCView.AnimationType.EFFECT -> {
                        intent.putExtra("Img", ImageViewer.ViewerType.EFFECT.name)
                    }
                    AnimationCView.AnimationType.SOUL -> {
                        intent.putExtra("Img", ImageViewer.ViewerType.SOUL.name)
                    }
                    AnimationCView.AnimationType.CANNON -> {
                        intent.putExtra("Img", ImageViewer.ViewerType.CANNON.name)
                    }
                    AnimationCView.AnimationType.DEMON_SOUL -> {
                        intent.putExtra("Img", ImageViewer.ViewerType.DEMON_SOUL.name)
                    }
                    else -> {

                    }
                }

                intent.putExtra("Index", position)

                requireActivity().startActivity(intent)
            }
        }

        return view
    }

    private fun generateEffName(index: Int, id: Int) : String {
        return requireContext().getString(R.string.eff_eff) + " - " + Data.trio(index) + " : "+requireContext().getString(id)
    }

    private fun generateEffName(index: Int) : String {
        return requireContext().getString(R.string.eff_eff) + " - " + Data.trio(index)
    }
}