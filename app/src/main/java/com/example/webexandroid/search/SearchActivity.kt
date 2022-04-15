package com.example.webexandroid.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.webexandroid.BaseActivity
import com.example.webexandroid.R
import com.example.webexandroid.calling.CallActivity
import com.example.webexandroid.calling.DialFragment
import com.example.webexandroid.databinding.ActivitySearchBinding
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.HorizontalFlipTransformation
import com.example.webexandroid.utils.SharedPrefUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer

class SearchActivity : BaseActivity() {
    lateinit var binding: ActivitySearchBinding

    private val searchViewModel: SearchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webexViewModel.enableBackgroundConnection(webexViewModel.enableBgConnectiontoggle)
        webexViewModel.setLogLevel(webexViewModel.logFilter)
        webexViewModel.enableConsoleLogger(webexViewModel.isConsoleLoggerEnabled)

        webexViewModel.signOutListenerLiveData.observe(this@SearchActivity, Observer {
            it?.let {
                if (it) {
                    SharedPrefUtils.clearLoginTypePref(this)
                    finish()
                }
            }
        })

        DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
                .also { binding = it }
                .apply {
                    viewPager.adapter = ViewPagerFragmentAdapter(this@SearchActivity, searchViewModel.titles)
                    viewPager.setPageTransformer(HorizontalFlipTransformation())
                    TabLayoutMediator(tabLayout, viewPager,
                            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                                tab.text = searchViewModel.titles[position]
                            }
                    ).attach()

                }
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity, val titles: List<String>) :
            FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                //0 -> return DialFragment()
                1 -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskSearchSpace)
                    val searchFragment = SearchCommonFragment()
                    searchFragment.arguments = bundle
                    return searchFragment
                }
//                2 -> {
//                    val bundle = Bundle()
//                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskCallHistory)
//                    val callHistoryFragment = SearchCommonFragment()
//                    callHistoryFragment.arguments = bundle
//                    return callHistoryFragment
//                }
                3 -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskListSpaces)
                    val spaceListFragment = SearchCommonFragment()
                    spaceListFragment.arguments = bundle
                    return spaceListFragment
                }
            }
            return SearchCommonFragment()
        }

        override fun getItemCount(): Int {
            return titles.size
        }
    }
}