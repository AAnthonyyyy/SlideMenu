package com.hgm.slidemenu

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hgm.slidemenu.databinding.ItemInfoLayoutBinding

/**
 * @author：  HGM
 * @date：  2023-06-23 14:38
 */
class ListAdapter(val list: ArrayList<String>) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
      class MyViewHolder(binding: ItemInfoLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            //val text: TextView = binding.tvText
            //val slideMenu:SlideMenu=binding.slideMenu
      }


      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                  ItemInfoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
      }

      override fun getItemCount(): Int {
            return list.size
      }

      override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val s = list[position]
            //holder.text.text=s
            //holder.slideMenu.setOnActionsClickListener(object:SlideMenu.OnActionsClickListener{
            //      override fun onReadClick() {
            //            Log.d(TAG, "onReadClick: ......")
            //      }
            //
            //      override fun onTopClick() {
            //            Log.d(TAG, "onTopClick: ......")
            //      }
            //
            //      override fun onDeleteClick() {
            //            list.removeAt(position)
            //            notifyItemRemoved(position)
            //            Log.d(TAG, "onDeleteClick: ......")
            //      }
            //
            //})
      }
}