package `in`.programy.audiorecorder.ui

import `in`.programy.audiorecorder.R
import `in`.programy.audiorecorder.util.ViewPagerAdapter
import `in`.programy.audiorecorder.viewmodel.AudioViewModel
import `in`.programy.audiorecorder.viewmodel.AudioViewModelFactory
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var audioViewModel: AudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = AudioViewModelFactory(application)    // Instance of viewModel factory
        audioViewModel = ViewModelProvider(this,factory).get(AudioViewModel::class.java)    // creating instance of viewModel

        viewPager.adapter =  ViewPagerAdapter(this)    // New Instance of viewPageAdapter for the viewPager

        val tabNames = listOf(R.string.tab_record, R.string.tab_audio)      // name of both the tabes

        TabLayoutMediator(tabLayout,viewPager){ tab, i ->   //Setting the tab names
            tab.text = getString(tabNames[i])
        }.attach()

    }

    // Listen after user accepts the permission of record audio
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 2){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                audioViewModel.record() // If user accepts the permission then start recording
            }
            else{
                Toast.makeText(this,"Please Give Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }


}