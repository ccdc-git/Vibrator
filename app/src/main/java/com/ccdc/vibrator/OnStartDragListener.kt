package com.ccdc.vibrator

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder


/**
 * Listener for manual initiation of a drag.
 */
interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    fun onStartDrag(viewHolder: MyAdapter.MyViewHolder)
    fun onFocused(viewHolder: MyAdapter.MyViewHolder)
    fun onClearFocus(viewHolder: View?)
    fun onLongClickedDown()
    fun onLongClickedUp()
}