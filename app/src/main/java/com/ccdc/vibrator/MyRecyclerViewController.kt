package com.ccdc.vibrator

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import kotlinx.android.synthetic.main.main_recycle_items.view.*


class MyRecyclerViewController : OnStartDragListener{

    private var itemTouchHelper: ItemTouchHelper
    private var recyclerView: RecyclerView
    var viewAdapter: MyAdapter
    private var viewManager: RecyclerView.LayoutManager
    var Dataset: MutableList<CustomVibration>
    val size : Int
        get()=Dataset.size


    constructor(context : Context, recyclerView: RecyclerView, myDataset : MutableList<CustomVibration>) {
        this.Dataset = myDataset
        this.viewManager = myLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        this.viewAdapter = MyAdapter(this.Dataset,this)

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
    lateinit var listener: RecyclerView.OnItemTouchListener

    override fun onFocused(viewHolder: MyAdapter.MyViewHolder) {
        listener = object : RecyclerView.OnItemTouchListener{
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return false
            }
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }

        }
        this.recyclerView.addOnItemTouchListener(listener)
        (this.viewManager as myLinearLayoutManager).isScrollEnabled = false
    }

    override fun onClearFocus(viewHolder: View?) {
        if (viewHolder != null) {
            viewHolder.clearFocus()
            viewHolder.isFocusableInTouchMode = false
            this.viewAdapter.focused = false
            this.recyclerView.removeOnItemTouchListener(listener)
            (this.viewManager as myLinearLayoutManager).isScrollEnabled = true
        }
    }


}

class myLinearLayoutManager(context: Context,orientation: Int, reverseLayout : Boolean) : LinearLayoutManager(context,orientation,reverseLayout) {
    var isScrollEnabled : Boolean = true
    override fun canScrollHorizontally(): Boolean {
        return this.isScrollEnabled && super.canScrollHorizontally()
    }
}