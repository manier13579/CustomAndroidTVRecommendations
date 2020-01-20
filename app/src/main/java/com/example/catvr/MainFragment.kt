/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.catvr

import java.util.Collections
import java.util.Timer
import java.util.TimerTask

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseFragment() {

    private val mHandler = Handler()
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        loadRows()

        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(activity.window)
        mDefaultBackground = ContextCompat.getDrawable(context, R.drawable.default_background)
        mMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)  //设置title

        //HEADERS_ENABLED 显示左侧导航栏，HEADERS_DISABLED 不显示 HEADERS_HIDDEN 隐藏，到边缘按左键还能显示
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // 设置快速导航（或 headers) 背景色
        brandColor = ContextCompat.getColor(context, R.color.fastlane_background)
        // 设置搜索的颜色
        searchAffordanceColor = ContextCompat.getColor(context, R.color.search_opaque)
    }

    // 加载行
    private fun loadRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        val gridHeader = HeaderItem(NUM_ROWS.toLong(), "推荐功能")

        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add("欢迎语")
        gridRowAdapter.add("天气")
        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))

        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(context, "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    //右侧功能点击事件
    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {

            if (item is Movie) {
                Log.d(TAG, "Item: " + item.toString())
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    (itemViewHolder.view as ImageCardView).mainImageView,
                    DetailsActivity.SHARED_ELEMENT_NAME
                )
                    .toBundle()
                activity.startActivity(intent, bundle)
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(context, BrowseErrorActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            /*if (item is Movie) {
                mBackgroundUri = item.backgroundImageUrl
                startBackgroundTimer()
            }*/
        }
    }

    //右侧功能按钮类
    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.btn_background))    //右侧功能背景颜色
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private val TAG = "MainFragment"

        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
        private val NUM_ROWS = 6
        private val NUM_COLS = 15
    }
}
