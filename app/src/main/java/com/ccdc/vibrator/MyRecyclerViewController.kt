package com.ccdc.vibrator

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import kotlinx.android.synthetic.main.main_recycle_items.view.*


class MyRecyclerViewController(context: Context, recyclerView: RecyclerView, var myDataset: MutableList<CustomVibration>) : OnStartDragListener{

    private var itemTouchHelper: ItemTouchHelper
    private var recyclerView: RecyclerView
    private var viewAdapter: MyAdapter = MyAdapter(this.myDataset,this)
    private var viewManager: RecyclerView.LayoutManager =
        mLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    val size : Int
        get()=myDataset.size

    init {
        this.recyclerView = recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = this@MyRecyclerViewController.viewManager

            // specify an viewAdapter (see also next example)
            adapter = this@MyRecyclerViewController.viewAdapter

        }
        this.itemTouchHelper =  ItemTouchHelper(DragCallback(this.viewAdapter))
        this.itemTouchHelper.attachToRecyclerView(this.recyclerView)
    }

    fun removeItemAt(index : Int ) : Boolean{
        return this.viewAdapter.removeItemAt(index)
    }
    fun addItemAt(index : Int , v : CustomVibration) : Boolean{
        return this.viewAdapter.addItemAt(index,v)
    }
    fun removeItemAll() :Boolean{
        return this.viewAdapter.removeItemAll()
    }

    override fun onStartDrag(viewHolder: MyAdapter.MyViewHolder) {
        this.itemTouchHelper.startDrag(viewHolder)
    }


    override fun onFocused(viewHolder: MyAdapter.MyViewHolder) {
        viewHolder.recyclerItem.VibeBlockView_recyclerView.hideRipple()
        viewHolder.recyclerItem.VibeBlockView_recyclerView.isFocusableInTouchMode = true
        viewAdapter.focused =  viewHolder.recyclerItem.VibeBlockView_recyclerView.requestFocus()
        (this.viewManager as mLinearLayoutManager).isScrollEnabled = false
        viewHolder.recyclerItem.VibeBlockView_recyclerView.setBlock()
    }

    override fun onClearFocus(view: View?) { //view : VibeBlockView
        if (view != null) {
            view.clearFocus()
            view.isFocusableInTouchMode = false
            this.viewAdapter.focused = false
            (this.viewManager as mLinearLayoutManager).isScrollEnabled = true
            view.VibeBlockView_recyclerView.showRipple()
            view.VibeBlockView_recyclerView.setBlock()
        }
    }

    override fun onLongClickedDown() {
        (this.viewManager as mLinearLayoutManager).isScrollEnabled = false
    }

    override fun onLongClickedUp() {
        (this.viewManager as mLinearLayoutManager).isScrollEnabled = true
    }
}

class mLinearLayoutManager(context: Context, orientation: Int, reverseLayout : Boolean) : LinearLayoutManager(context,orientation,reverseLayout) {
    var isScrollEnabled : Boolean = true
    override fun canScrollHorizontally(): Boolean {
        return this.isScrollEnabled && super.canScrollHorizontally()
    }
}