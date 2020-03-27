package com.ccdc.vibrator

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MyRecyclerViewController : OnStartDragListener{

    var itemTouchHelper: ItemTouchHelper
    private var _recyclerView: RecyclerView
    val recyclerView : RecyclerView
        get() = _recyclerView
    private var viewAdapter: MyAdapter
    private var viewManager: RecyclerView.LayoutManager
    var Dataset: MutableList<OneShot>
    val size : Int
        get()=Dataset.size


    constructor(context : Context, recyclerView: RecyclerView, myDataset : MutableList<OneShot>) {
        this.Dataset = myDataset
        this.viewManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        this.viewAdapter = MyAdapter(this.Dataset,this)

        this._recyclerView = recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = this@MyRecyclerViewController.viewManager

            // specify an viewAdapter (see also next example)
            adapter = this@MyRecyclerViewController.viewAdapter
        }
        this.itemTouchHelper =  ItemTouchHelper(
            DragCallbackListener(
                this.viewAdapter
            ))
        this.itemTouchHelper.attachToRecyclerView(_recyclerView)

    }
    fun removeItemAt(index : Int ) : Boolean{
        return this.viewAdapter.removeItemAt(index)
    }
    fun addItemAt(index : Int , v : OneShot) : Boolean{
        return this.viewAdapter.addItemAt(index,v)
    }
    fun removeItemAll() :Boolean{
        return this.viewAdapter.removeItemAll()
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        this.itemTouchHelper.startDrag(viewHolder)
    }


}
