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

        viewModel = (activity as MainActivity).audioViewModel

        viewModel.btRecorderText.observe(activity as MainActivity, Observer {
            btRecord.text = it
        })

        viewModel.currTime.observe(activity as MainActivity, Observer {
            tvTime.text = it
        })

        btRecord.setOnClickListener {
            if (checkPermission()){
                viewModel.record()
            }
        }
    }

    private fun checkPermission(): Boolean{
        return if(ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) true
        else {
            ActivityCompat.requestPermissions(activity as MainActivity,arrayOf(Manifest.permission.RECORD_AUDIO),2)
            false
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.closeRecording()
    }
}