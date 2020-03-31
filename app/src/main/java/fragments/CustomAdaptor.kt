package fragments

import com.ccdc.vibrator.R
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import kotlinx.android.synthetic.main.custom_fragment_recycle_items.view.*

class CustomAdaptor(private var myDataset: MutableList<CustomVibration> , private var onCustomInput: OnCustomInput) :
    RecyclerView.Adapter<CustomAdaptor.MyViewHolder>(){

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
            .inflate(R.layout.custom_fragment_recycle_items, parent, false) as View
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(recyclerItem)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.recyclerItem.CardView_fragment_custom_card.setOnTouchListener{v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                Log.v("v","ACTION_DOWN")
                if(v.isClickable) setUpTouch(v,event)
            }
            return@setOnTouchListener false
        }
        val context : Context = holder.recyclerItem.context
        holder.recyclerItem.VibeBlockView_fragment_custom_item.customVibration = myDataset[position]
        (holder.recyclerItem.TextView_fragment_custom_title as TextView).text = myDataset[position].codeName
        holder.recyclerItem.VibeBlockView_fragment_custom_item.setBlock(96F)

    }
    private fun setUpTouch(v : View, e: MotionEvent){
        v.setOnTouchListener{v, event ->
            if(event.action == MotionEvent.ACTION_UP){
                Log.v("v","mClicked")
                //선택됨
                onCustomInput.itemClicked(v.TextView_fragment_custom_title.text.toString())
            }
            if(event.action == MotionEvent.ACTION_MOVE){
                return@setOnTouchListener false
            }
            v.setOnTouchListener { v, event ->
                if(event.action == MotionEvent.ACTION_DOWN){
                    Log.v("v","ACTION_DOWN")
                    if(v.isClickable) setUpTouch(v,event)
                }
                false
            }
            return@setOnTouchListener false
        }
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


    private fun dpToPx(size : Float, context : Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,context.resources.displayMetrics).toInt()

}