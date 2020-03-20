package com.ccdc.vibrator

import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycle_items.view.*
import java.util.*

class MyAdapter(private var myDataset: MutableList<OneShot>,private val startDragListener: OnStartDragListener) :
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

        holder.recyclerItem.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                this.startDragListener.onStartDrag(holder)
            }
            return@setOnTouchListener true
        }

        (holder.recyclerItem.RecyclerView_TextView_title as TextView).text = myDataset[position].codeName
        (holder.recyclerItem.RecyclerView_ImageView_image as ImageView).setBackgroundColor(Color.parseColor(myDataset[position].codeName))
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

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

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        Log.i("Moved","from $fromPosition to $toPosition")
        if(fromPosition < toPosition){
            for(i in fromPosition until toPosition){
                Collections.swap(myDataset,i,i+1)
            }
        }else{
            for(i in fromPosition until toPosition){
                Collections.swap(myDataset,i,i-1)
            }
        }
        notifyItemMoved(fromPosition,toPosition )
    }

    override fun onRowSelected(viewHolder: MyAdapter.MyViewHolder) {
        Log.i("selected",viewHolder.adapterPosition.toString())
    }

    override fun onRowCleared(viewHolder: MyAdapter.MyViewHolder) {
        Log.i("Cleared",viewHolder.adapterPosition.toString())
    }

    override fun onSwiped(itemViewHolder: MyViewHolder) {
        removeItemAt(itemViewHolder.adapterPosition)
    }

}