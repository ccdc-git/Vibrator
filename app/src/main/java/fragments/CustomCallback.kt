package fragments

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_fragment_recycle_items.view.*
import kotlin.math.max
import kotlin.math.min


//https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28 recyclerView swipe menu

enum class ButtonsState{
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

class CustomCallback(private var customAdapter : CustomAdaptor) : ItemTouchHelper.Callback() {

    private var swipeBack : Boolean = false
    private var buttonShowedState = ButtonsState.GONE
    private val buttonWidth = 300f
    private var currentItemViewHolder : RecyclerView.ViewHolder? = null
    private var buttonInstance : RectF? = null
    private var buttonsActions : CustomCallbackActions? = null

    constructor(customAdapter: CustomAdaptor, buttonActions: CustomCallbackActions) : this(customAdapter){
        this.buttonsActions = buttonActions
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeLeft = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(0, swipeLeft )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if(swipeBack){
            swipeBack = buttonShowedState!=ButtonsState.GONE
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        var mdX = dX
        //Log.v("swipe","${actionState} , $dX $dY $buttonShowedState $swipeBack")
        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE)  mdX= max(dX, buttonWidth);
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) mdX = min(dX, -buttonWidth);
                super.onChildDraw(c, recyclerView, viewHolder, mdX, dY, actionState, isCurrentlyActive);
            }
            else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        currentItemViewHolder = viewHolder
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean){

        recyclerView.setOnTouchListener{ _: View, event : MotionEvent->
            //Log.v("event", MotionEvent.actionToString(event.action))
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if(swipeBack){
                if(dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE
                else if(dX > buttonWidth) buttonShowedState = ButtonsState.LEFT_VISIBLE
            }
            if(buttonShowedState != ButtonsState.GONE){
                setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                setItemClickable(recyclerView,false)
            }
            return@setOnTouchListener false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchDownListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean){
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener{
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {  }
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                //Log.v("onIntercept",MotionEvent.actionToString(e.action))
                if (e.action == MotionEvent.ACTION_DOWN){
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
                return false
            }
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchUpListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener{
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if(e.action == MotionEvent.ACTION_UP){
                    super@CustomCallback.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive)
                    recyclerView.setOnTouchListener { _, _ -> false }
                    setItemClickable(recyclerView,true)
                    swipeBack = false
                    if(buttonInstance != null) {
                        if (buttonInstance!!.contains(e.x,e.y)) {
                            if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                                buttonsActions!!.onLeftClicked(currentItemViewHolder!!.adapterPosition);
                            } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                                buttonsActions!!.onRightClicked(currentItemViewHolder!!.adapterPosition);
                            }
                        }
                    }
                    currentItemViewHolder = null;
                    buttonShowedState = ButtonsState.GONE
                }
                return false
            }
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun setItemClickable(recyclerView: RecyclerView, clickable : Boolean){
        for(i in 0 until recyclerView.childCount){
            recyclerView.getChildAt(i).CardView_fragment_custom_card.isClickable = clickable
        }
    }

    private fun drawButtons(c:Canvas, viewHolder: RecyclerView.ViewHolder){
        val buttonWidthWithoutPadding = buttonWidth - 20
        val corners = 6f

        val itemView = viewHolder.itemView
        val relativeLayout = viewHolder.itemView.RelativeLayout_fragment_custom_itemLayout

        val paddingHorizontal = itemView.paddingLeft + relativeLayout.left
        val paddingTop = relativeLayout.top + itemView.paddingTop
        val buttonHeight = relativeLayout.height

        val p = Paint()

        val leftButton = RectF(
            itemView.left.toFloat() + paddingHorizontal,
            itemView.top.toFloat() + paddingTop,
            itemView.left + paddingHorizontal + buttonWidthWithoutPadding,
            itemView.top.toFloat() + paddingTop + buttonHeight )
        p.color = Color.BLUE
        c.drawRoundRect(leftButton, corners, corners, p)
        drawText("EDIT", c, leftButton, p)

        val rightButton = RectF(
            itemView.right - buttonWidthWithoutPadding - paddingHorizontal,
            itemView.top.toFloat() + paddingTop,
            itemView.right.toFloat() - paddingHorizontal,
            itemView.top.toFloat() + paddingTop + buttonHeight )
        p.color = Color.RED
        c.drawRoundRect(rightButton, corners, corners, p)
        drawText("DELETE", c, rightButton, p)

        buttonInstance = null
        if (buttonShowedState === ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton
        } else if (buttonShowedState === ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }
    private fun drawText(text : String, c : Canvas, button : RectF, p : Paint) {
        val textSize = 60F
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p)
    }

    fun onDraw(c: Canvas?) {
        if (currentItemViewHolder != null) {
            drawButtons(c!!, currentItemViewHolder!!)
        }
    }
}