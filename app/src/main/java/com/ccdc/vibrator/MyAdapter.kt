package com.ccdc.vibrator

import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycle_items.view.*

class MyAdapter(private var myDataset: MutableList<OneShot>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>(),
    DragCallbackListener.Listener{

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val recyclerItem: View) : RecyclerView.ViewHolder(recyclerItem)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val recyclerItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycle_items, parent, false) as View
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(recyclerItem)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.recyclerItem.setOnClickListener {v ->
            this.removeItemAt(holder.adapterPosition)
        }
        holder.recyclerItem.setOnDragListener { v, event ->
            val dragEvent = event.action
            when(dragEvent){
                DragEvent.ACTION_DRAG_ENTERED -> Log.i("drag","Entered")
                DragEvent.ACTION_DRAG_EXITED -> Log.i("drag","Exited")
                DragEvent.ACTION_DROP -> Log.i("drag","Dropped")
            }
            return@setOnDragListener true
        }

        (holder.recyclerItem.RecyclerView_TextView_title as TextView).text = myDataset[position].codeName
        (holder.recyclerItem.RecyclerView_ImageView_image as ImageView).setBackgroundColor(Color.parseColor(myDataset[position].codeName))
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun swapItems(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition..toPosition - 1) {
                myDataset.set(i, myDataset.set(i+1, myDataset.get(i)));
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                myDataset.set(i, myDataset.set(i-1, myDataset.get(i)));
            }
        }
        this.notifyItemMoved(fromPosition, toPosition)
    }

    fun removeItemAt(index : Int ) : Boolean{
        return try {
            this.myDataset.removeAt(index)
            this.notifyItemRemoved(index)
            true
        }catch (e: IndexOutOfBoundsException){
            false
        }
    }
    fun addItemAt(index : Int , v : OneShot) : Boolean{
        return try {
            this.myDataset.add(index,v)
            this.notifyItemInserted(index)
            true
        }catch (e: IndexOutOfBoundsException){
            false
        }
    }
    fun removeItemAll() :Boolean{
        try{
            while(this.myDataset.isNotEmpty()){
                removeItemAt(myDataset.size-1)
                this.notifyItemRemoved(myDataset.size-1)
            }
        }catch (e: IndexOutOfBoundsException){
            return false
        }
        return true
    }

    override fun onRowMoved(adapterPosition: Int, adapterPosition1: Int) {

    }

    override fun onRowSelected(viewHolder: MyAdapter.MyViewHolder) {

    }

    override fun onRowCleared(viewHolder: MyAdapter.MyViewHolder) {

    }

}