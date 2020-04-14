package fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ccdc.vibrator.MainActivity
import com.ccdc.vibrator.R
import kotlinx.android.synthetic.main.fragment_staccato.view.*

class FragmentStaccato : Fragment() {
    private var activity : MainActivity? = null
    lateinit var rootView : ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as MainActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_staccato, container, false) as ViewGroup
        if(activity != null){
            activity!!.setButton(rootView.Button_piano_staccato)
            activity!!.setButton(rootView.Button_forte_staccato)
            activity!!.setButton(rootView.Button_crescendo_staccato)
            activity!!.setButton(rootView.Button_decrescendo_staccato)
        }
        return rootView
    }
}
