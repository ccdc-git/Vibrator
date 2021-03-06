package com.ccdc.vibrator

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.RippleDrawable
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import kotlinx.android.synthetic.main.main_recycle_items.view.*
import java.util.*

class MyAdapter(private var myDataset: MutableList<CustomVibration>, private val startDragListener: OnStartDragListener) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>(),
    DragCallback.Listener{

    var focused : Boolean = false

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val recyclerItem: View) : RecyclerView.ViewHolder(recyclerItem)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val recyclerItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_recycle_items, parent, false) as View
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(recyclerItem)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val recyclerItem = holder.recyclerItem
        recyclerItem.setOnLongClickListener {
            Log.i("click","LongClicked")
            this.startDragListener.onLongClickedDown()
            setUpListener(recyclerItem,holder)
            return@setOnLongClickListener false
        }

        recyclerItem.VibeBlockView_recyclerView.customVibration = myDataset[position]
        (recyclerItem.RecyclerView_TextView_title as TextView).text = ""
        recyclerItem.VibeBlockView_recyclerView.setBlock(96F)

    }
    private fun setUpListener(v: View, holder: MyViewHolder){
        v.setOnTouchListener{view,e ->
            if (e.action == MotionEvent.ACTION_UP) {  //롱클릭 후 바로 떼면 포커싱
                this.startDragListener.onFocused(holder)
                removeTouchListener(v)
            }else if(e.action == MotionEvent.ACTION_MOVE){ //조금의 움직임은 허용 벗어나면 startDrag로
                setMoveListener(view,e,holder)
            }
            return@setOnTouchListener false
        }
    }
    private fun setMoveListener(v : View, e: MotionEvent, holder: MyViewHolder){
        val downX = e.x.toInt()
        val downY = e.y.toInt()
        val rect = Rect(downX - 25, downY - 25, downX + 25, downY +25)
        v.setOnTouchListener(){ _, event ->
            Log.i("li","setMove ${MotionEvent.actionToString(e.action)}")
            if (event.action == MotionEvent.ACTION_MOVE){
                if(!rect.contains(event.x.toInt(), event.y.toInt())){ // 벗어날 시 startDrag
                    this.startDragListener.onStartDrag(holder)
                    this.startDragListener.onLongClickedUp()
                    removeTouchListener(v)
                }
            }else if(event.action == MotionEvent.ACTION_UP){ // 조금의 움직임 허용
                this.startDragListener.onFocused(holder)
                this.startDragListener.onLongClickedUp()
                removeTouchListener(v)
            }
            return@setOnTouchListener false
        }
    }
    private fun removeTouchListener(v : View){v.setOnTouchListener { _, _ ->  return@setOnTouchListener false}}

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
    fun addItemAt(index : Int , v : CustomVibration) : Boolean{
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
//        Log.i("Moved","from $fromPosition to $toPosition")
        if(fromPosition < toPosition){
            for(i in fromPosition until toPosition){
                Collections.swap(myDataset,i,i+1)
                //Log.i("Swapped"," $i and ${1+i}")
            }
        }else{
            for(i in fromPosition downTo toPosition+1){
                Collections.swap(myDataset,i,i-1)
                //Log.i("Swapped"," $i and ${i-1}")
            }
        }
        notifyItemMoved(fromPosition,toPosition )
    }

    override fun onRowSelected(itemViewHolder: MyViewHolder) {
        val context = itemViewHolder.recyclerItem.context
        itemViewHolder.recyclerItem.VibeBlockView_recyclerView.hideRipple()
        itemViewHolder.recyclerItem.VibeBlockView_recyclerView.setBlock(88F)
        itemViewHolder.recyclerItem.ConstraintLayout_recyclerItem.background = context.getDrawable(R.drawable.selected_item)
    }
    override fun onRowCleared(itemViewHolder: MyViewHolder) {
        itemViewHolder.recyclerItem.VibeBlockView_recyclerView.showRipple()
        itemViewHolder.recyclerItem.VibeBlockView_recyclerView.setBlock(96F)
        itemViewHolder.recyclerItem.ConstraintLayout_recyclerItem.background = null
    }

    override fun onSwiped(itemViewHolder: MyViewHolder) {
        removeItemAt(itemViewHolder.adapterPosition)
    }
    private fun dpToPx(size : Float, context : Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,context.resources.displayMetrics).toInt()

}