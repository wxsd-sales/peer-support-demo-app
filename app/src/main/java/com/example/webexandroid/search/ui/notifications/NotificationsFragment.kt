package com.example.webexandroid.search.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.webexandroid.databinding.FragmentNotificationsBinding
import com.example.webexandroid.person.PersonModel
import com.example.webexandroid.person.PersonViewModel
import com.example.webexandroid.search.EditProfileActivity
import com.example.webexandroid.utils.Constants
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject

class NotificationsFragment : Fragment() {
    companion object {
        fun newInstance(personId: String) : NotificationsFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.PERSON_ID, personId)

            val fragment = NotificationsFragment()
            fragment.arguments = args

            return fragment
        }
    }

  private lateinit var notificationsViewModel: NotificationsViewModel
private var _binding: FragmentNotificationsBinding? = null
    private lateinit var personId : String
    private var url : String? = null
    val TAG = "NotificationsFragment"
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
    private val personViewModel : PersonViewModel by inject()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

      personId = arguments?.getString(Constants.Bundle.PERSON_ID) ?: ""
    notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

    _binding=FragmentNotificationsBinding.inflate(inflater, container, false)
        .apply{
            personViewModel.person.observe(viewLifecycleOwner, Observer { model ->
                model?.let {
                    person = it
                    url=it.avatar
                    if(!url!!.isEmpty()) {
                        Picasso.with(getActivity()).load(url).into(binding.imageView1)
                    }
                }
            })
        }
      binding.editProfile.setOnClickListener{
          val intent = Intent(activity, EditProfileActivity::class.java)
          startActivity(intent)
      }
      val root: View = binding.root

    return root
  }

    override fun onResume() {
        super.onResume()
        if(personId.isEmpty()) {
            personViewModel.getMe()
        } else{
            personViewModel.getPersonDetail(personId)
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


