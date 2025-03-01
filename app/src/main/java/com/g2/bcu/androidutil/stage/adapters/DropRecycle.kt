package com.g2.bcu.androidutil.stage.adapters

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g2.bcu.R
import com.g2.bcu.androidutil.StaticStore
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import common.util.stage.info.CustomStageInfo
import common.util.stage.info.DefStageInfo
import java.text.DecimalFormat

class DropRecycle(private val st: Stage, private val activity: Activity) : RecyclerView.Adapter<DropRecycle.ViewHolder>() {
    private val dropData: List<String>

    init {
        dropData = handleDrops()
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var chance: TextView = row.findViewById(R.id.dropchance)
        var item: TextView = row.findViewById(R.id.dropitem)
        var amount: TextView = row.findViewById(R.id.dropamount)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.drop_info_layout, viewGroup, false)

        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (st.info is CustomStageInfo) {
            if (i < dropData.size) {
                val c = (i+1).toString()
                viewHolder.chance.text = c
                viewHolder.item.text = dropData[i]
                viewHolder.amount.text = "1"
            }
            return
        }
        val info = st.info as DefStageInfo

        val c = when {
            dropData.isEmpty() -> (i+1).toString()
            i >= dropData.size -> info.drop[i][0].toString() + "%"
            else -> dropData[i] + "%"
        }
        val data = info.drop[i]
        viewHolder.chance.text = c

        var reward = MultiLangCont.getStageDrop(data[1])
        if (reward == null)
            reward = data[1].toString()

        if (i == 0) {
            if (data[0] != 100) {
                val bd = BitmapDrawable(activity.resources, StaticStore.getResizeb(StaticStore.treasure, activity, 24f))

                bd.isFilterBitmap = true
                bd.setAntiAlias(true)
                viewHolder.item.setCompoundDrawablesWithIntrinsicBounds(null, null, bd, null)
            }

            if (info.rand == 1 || data[1] >= 1000)
                reward += activity.getString(R.string.stg_info_once)
        }
        viewHolder.item.text = reward
        viewHolder.amount.text = data[2].toString()
    }

    override fun getItemCount(): Int {
        return if (st.info is DefStageInfo) (st.info as DefStageInfo).drop.size
        else (st.info as CustomStageInfo).rewards.size
    }

    private fun handleDrops() : List<String> {
        val res = ArrayList<String>()
        if (st.info is CustomStageInfo) {
            val info = st.info as CustomStageInfo
            for (rwd in info.rewards) {
                val str = if (rwd.fid == 1) rwd.unit.toString() else rwd.toString()
                res.add(str)
            }
            return res
        }
        val info = st.info as DefStageInfo
        val data = info.drop

        var sum = 0
        for(i in data)
            sum += i[0]

        val df = DecimalFormat("#.##")
        if(sum == 1000) {
            for(i in data)
                res.add(df.format(i[0].toDouble()/10))
        } else if((sum == data.size && sum != 1) || info.rand == -3) {
            return res
        } else if(sum == 100) {
            for(i in data)
                res.add(i[0].toString())
        } else if(sum > 100 && (info.rand == 0 || info.rand == 1)) {
            var rest = 100.0

            if(data[0][0] == 100) {
                res.add("100")
                for(i in 1 until data.size) {
                    val filter = rest * data[i][0].toDouble() / 100.0
                    rest -= filter
                    res.add(df.format(filter))
                }
            } else
                for(i in data) {
                    val filter = rest * i[0].toDouble() / 100.0
                    rest -= filter
                    res.add(df.format(filter))
                }
        } else if(info.rand == -4) {
            var total = 0
            for(i in data)
                total += i[0]

            if(total == 0) {
                for(i in data)
                    res.add(i[0].toString())
                return res
            }

            for(i in data)
                res.add(df.format(i[0] * 100.0 / total))
        } else {
            for(i in data)
                res.add(i[0].toString())
        }

        return res
    }

}