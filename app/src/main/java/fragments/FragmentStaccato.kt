package fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ccdc.vibrator.MainActivity
import com.ccdc.vibrator.R
import kotlinx.android.synthetic.main.fragment_staccato.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentStaccato.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentStaccato : Fragment() {
    // TODO: Rename and change types of parameters
    private var activity : MainActivity? = null
    lateinit var rootView : ViewGroup
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        activity = getActivity() as MainActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_staccato, container, false) as ViewGroup
        if(activity != null){
            activity!!.setButton(rootView.Button_piano_staccato,true)
            activity!!.setButton(rootView.Button_forte_staccato,true)
            activity!!.setButton(rootView.Button_crescendo_staccato,true)
            activity!!.setButton(rootView.Button_decrescendo_staccato,true)
        }
        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentStaccato.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentStaccato().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
