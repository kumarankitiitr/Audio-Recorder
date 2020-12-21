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

        val factory = AudioViewModelFactory(application)
        audioViewModel = ViewModelProvider(this,factory).get(AudioViewModel::class.java)

        viewPager.adapter =
            ViewPagerAdapter(this)

        val tabNames = listOf(R.string.tab_record, R.string.tab_audio)

        TabLayoutMediator(tabLayout,viewPager){ tab, i ->
            tab.text = getString(tabNames[i])
        }.attach()


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 2){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                audioViewModel.record()
            }
            else{
                Toast.makeText(this,"Please Give Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }


}