package `in`.programy.audiorecorder.util

import `in`.programy.audiorecorder.ui.fragment.AudiosFragment
import `in`.programy.audiorecorder.ui.fragment.RecorderFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return if(position ==0) RecorderFragment()
        else AudiosFragment()
    }
}