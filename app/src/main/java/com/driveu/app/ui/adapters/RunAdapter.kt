package com.driveu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.driveu.app.databinding.ItemRunBinding
import com.driveu.app.db.Converters
import com.driveu.app.db.Run
import com.driveu.app.util.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class RunViewHolder(private var mBinding: ItemRunBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun bind(run: Run?) {
            run?.let {
                /*Converters().toBitmap(it.img!!)*/
                Glide.with(itemView.context).load(TrackingUtility.decodeBitmap(it.img!!)).into(mBinding.ivRunImage)
                val calender = Calendar.getInstance().apply {
                    timeInMillis = it.timestamp!!
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                mBinding.tvDate.text = dateFormat.format(calender.time)
                mBinding.tvAvgSpeed.text = "${it.avgSpeedInKMH} km/h"
                mBinding.tvDistance.text = "${it.distanceInMeters !!/ 1000f} km"
                mBinding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(it.timeInMillis!!)

            }
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(
            oldItem: Run, newItem: Run,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Run, newItem: Run,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RunViewHolder(
            ItemRunBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RunViewHolder).bind(differ.currentList[position])
    }
}