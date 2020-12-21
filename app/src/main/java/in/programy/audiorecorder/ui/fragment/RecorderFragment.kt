package `in`.programy.audiorecorder.ui.fragment

import `in`.programy.audiorecorder.R
import `in`.programy.audiorecorder.ui.MainActivity
import `in`.programy.audiorecorder.viewmodel.AudioViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_recorder.*

class RecorderFragment : Fragment(R.layout.fragment_recorder) {
    private lateinit var viewModel: AudioViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).audioViewModel       //Taking view model from Main Activity

        //Track the position of the media recorder and change the text of the record button
        viewModel.btRecorderText.observe(activity as MainActivity, Observer {
            btRecord.text = it
        })

        //Track the recording time
        viewModel.currTime.observe(activity as MainActivity, Observer {
            tvTime.text = it
        })

        // After checking the permission start recording or stop recording
        btRecord.setOnClickListener {
            if (checkPermission()){
                viewModel.record()
            }
        }
    }

    // Check and, if not given the ask for the Record_Audio permission
    private fun checkPermission(): Boolean{
        return if(ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) true
        else {
            ActivityCompat.requestPermissions(activity as MainActivity,arrayOf(Manifest.permission.RECORD_AUDIO),2)
            false
        }
    }

    //When app goes to background close close or release and null the recorder
    override fun onStop() {
        super.onStop()
        viewModel.closeRecording()
    }
}