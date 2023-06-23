package com.hgm.slidemenu

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.hgm.slidemenu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

      private val binding:ActivityMainBinding by lazy {
            ActivityMainBinding.inflate(layoutInflater)
      }

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(binding.root)

            binding.slideMenu.setOnActionsClickListener(object : SlideMenu.OnActionsClickListener {
                  override fun onReadClick() {
                        Log.d(TAG, "onReadClick: ......")
                  }

                  override fun onTopClick() {
                        Log.d(TAG, "onTopClick: .....")
                  }

                  override fun onDeleteClick() {
                        Log.d(TAG, "onDeleteClick: .......")
                  }
            })


            //val list = arrayListOf("111", "222", "333", "444", "555", "666")
            //val listAdapter = ListAdapter(list)
            //val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            //recyclerView.apply {
            //      adapter = listAdapter
            //}
      }
}