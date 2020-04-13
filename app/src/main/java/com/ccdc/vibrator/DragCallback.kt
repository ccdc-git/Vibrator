package com.ccdc.vibrator

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragCallback(private val myAdapter: MyAdapter) : ItemTouchHelper.Callback() {


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeDown = ItemTouchHelper.DOWN
        if(myAdapter.focused){
            return 0
        }
        return makeMovementFlags(dragFlags, swipeDown)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        myAdapter.onRowMoved(viewHolder.adapterPosition,target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
            if(viewHolder is MyAdapter.MyViewHolder){
                myAdapter.onRowSelected(viewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if(viewHolder is MyAdapter.MyViewHolder){
            myAdapter.onRowCleared(viewHolder)
            getMovementFlags(recyclerView, viewHolder)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if(viewHolder is MyAdapter.MyViewHolder){
            myAdapter.onSwiped(viewHolder)
        }
    }


    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(itemViewHolder: MyAdapter.MyViewHolder)
        fun onRowCleared(itemViewHolder: MyAdapter.MyViewHolder)
        fun onSwiped(itemViewHolder: MyAdapter.MyViewHolder)
    }


}