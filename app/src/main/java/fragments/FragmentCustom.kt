package fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import ccdc.lib.customvibrator.InputActivity
import com.ccdc.vibrator.MainActivity
import com.ccdc.vibrator.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_custom.view.*
import kotlinx.android.synthetic.main.fragment_custom.view.floatingActionButton_add_custom
import java.io.FileInputStream
import java.io.FileNotFoundException

private const val ARG_PARAM1 = "param1"

class FragmentCustom : Fragment() {
    private var param1: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CustomAdaptor
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val myDataSet : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        //testing
        myDataSet.clear()
        //val fileNames : List<String> = listOf("piano_normal","forte_normal","piano_normal","forte_staccato") //testing code

        //make myDataSet from preference "customs"
        //myList is separate CustomVibration's code name with '\'
        val pref = context!!.getSharedPreferences("customs", MODE_PRIVATE)
        val fileNames = pref.getString("myList","")?.split("\\")

        for (name in fileNames!!){
            myDataSet.add(name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_custom, container, false)
        val mainActivity : MainActivity = activity as MainActivity


        this.recyclerView = rootView.RecyclerView_fragment_custom_customViews
        this.viewManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        this.viewAdapter = CustomAdaptor(this.myDataSet, object : OnCustomInput(){
            override fun itemClicked(codeName: String) {
                mainActivity.addInMyRVC(codeName)
            }
        })
        this.viewAdapter.mContext = this.context

        this.recyclerView = recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = this@FragmentCustom.viewManager

            // specify an viewAdapter (see also next example)
            adapter = this@FragmentCustom.viewAdapter
        }
        val customCallback = CustomCallback(viewAdapter, object : CustomCallbackActions(){
            override fun onRightClicked(position: Int) {
                viewAdapter.removeItemAt(position)}
        })
        val itemTouchHelper = ItemTouchHelper(customCallback)
        itemTouchHelper.attachToRecyclerView(this.recyclerView)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                customCallback.onDraw(c)}
        })

        (rootView.floatingActionButton_add_custom.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = mainActivity.findViewById<BottomNavigationView>(R.id.bottomNavigationView).height + dpToPx(16F,context!!)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.v("scroll","$dx $dy")
                super.onScrolled(recyclerView, dx, dy)
                val bottomNavi : BottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView)
                val floatingButton = rootView.floatingActionButton_add_custom
                val floatingButtonLayoutParams = floatingButton.layoutParams as ConstraintLayout.LayoutParams
                val layoutManager : LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount : Int = layoutManager.itemCount
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()

                if(dy > 0) {
                    bottomNavi.visibility = View.GONE
                    floatingButtonLayoutParams.bottomMargin = dpToPx(32F,context!!)
                }

                if(dy < -20) {
                    bottomNavi.visibility = View.VISIBLE
                    floatingButtonLayoutParams.bottomMargin = bottomNavi.height + dpToPx(16F,context!!)
                }

                if(firstVisible == 0) bottomNavi.visibility = View.VISIBLE

                if(lastVisible == totalItemCount - 1) floatingButton.show()
                else floatingButton.hide()
            }
        })

        rootView.floatingActionButton_add_custom.setOnClickListener {
            val mIntent : Intent = Intent(context,InputActivity::class.java)
            this.startActivityForResult(mIntent,5)
        }

        return rootView
    }
    private fun dpToPx(size : Float, context : Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,context.resources.displayMetrics).toInt()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 5){
            if (resultCode == RESULT_OK){
                val mBundle = data?.extras
                if(mBundle != null) {
                    val newFileName = mBundle.getString("newFileName","")
                    viewAdapter.add(newFileName)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            FragmentCustom().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}
