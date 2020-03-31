package fragments

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccdc.lib.customvibrator.CustomVibration
import com.ccdc.vibrator.MainActivity
import com.ccdc.vibrator.R
import kotlinx.android.synthetic.main.fragment_custom.view.*
import java.io.FileNotFoundException

private const val ARG_PARAM1 = "param1"

class FragmentCustom : Fragment() {
    private var param1: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CustomAdaptor
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataSet : MutableList<CustomVibration> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
        //testing
        myDataSet.clear()
        val fileNames : List<String> = listOf("piano_normal","forte_normal","piano_normal","forte_staccato")
        for (name in fileNames){
            try {
                //val fIO: FileInputStream = activity!!.openFileInput(name)
                //myDataSet.add(CustomVibration(fIO,name))
                val context1 = context
                if(context1 != null) {
                    myDataSet.add(CustomVibration(context1, name))
                }
            }catch (e : FileNotFoundException){
                Log.d("Exception","no file")
            }
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
        this.viewAdapter = CustomAdaptor(this.myDataSet,object : OnCustomInput(){
            override fun itemClicked(codeName: String) {
                mainActivity.addInMyRVC(codeName)
            }
        })

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
                viewAdapter.removeItemAt(position)
            }

            override fun itemClicked(position: Int) {
                //TODO 아이템을 작업장에 추가
            }
        })
        val itemTouchHelper = ItemTouchHelper(customCallback)
        itemTouchHelper.attachToRecyclerView(this.recyclerView)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                customCallback.onDraw(c)
            }
        })

        return rootView
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
