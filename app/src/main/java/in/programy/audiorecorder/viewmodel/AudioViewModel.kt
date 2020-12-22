package `in`.programy.audiorecorder.viewmodel

import `in`.programy.audiorecorder.exoplayer.PlayerAnalyticsListener.analyticsListener
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.currentPlayingPos
import `in`.programy.audiorecorder.util.AudioApplication
import `in`.programy.audiorecorder.util.Item
import `in`.programy.audiorecorder.util.Methods.longToTime
import android.app.Application
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class AudioViewModel(application: Application): AndroidViewModel(application) {
    private var recorder : MediaRecorder? = null        // Instance of media recorder
    private val context = getApplication<AudioApplication>()    //taking context using application class

    val btRecorderText = MutableLiveData<String>()          //Live data to track mediaRecorder state and change the recorder button text
    val pauseButtonText = MutableLiveData<String>()         //Live data to change the pause button text
    val pauseButtonState = MutableLiveData<Boolean>()       //Live data to track Pause Button State (visibility)
    val startList = MutableLiveData<MutableList<Item>>()    //Live data to read the previous recordings saved in memory
    val currTime = MutableLiveData<String>()                //currTime of mediaRecorder
    val newItem = MutableLiveData<Item>()                   //If any recording completes then add the recycler view

    private val list = mutableListOf<Item>()                //Private list to store the previous recordings
    private var tempFileName = ""                           //Current file name that is recording
    private var tempTime = 0L                               //Private value for current time of media recorder
    private var isRecording = false                         // Checking the state of media recorder using this boolean
    private var isRecordingPaused = false                   // Boolean to track if recording is paused

    private val path = "${context.externalCacheDir?.absolutePath}"  // Path fo cache where we store the recording
    val player = SimpleExoPlayer.Builder(context).build()   // Instance of ExoPlayer

    //DataSourceFactory for creating the progressive media source
    private val factory = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context,Util.getUserAgent(context,"Audio Recorder")))


    init {
        player.addAnalyticsListener(analyticsListener)      // Adding the analytics listener to the player
        listFiles()
    }

    //Start or stop recording after checking the state of media recorder
    fun record(){
        if(!isRecording && !isRecordingPaused) startRecording()
        else stopRecording()
    }

    // Start the recording
    private fun startRecording(){
        tempFileName = "${System.currentTimeMillis()}.3gp"
        val fileName = "${context.externalCacheDir?.absolutePath}/$tempFileName"
        recorder = MediaRecorder().apply {          //  Creating the Instance of Media Recorder
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(fileName)
            try {
                prepare()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            start()
        }
            isRecording = true
            startRecorderTimer()
            visiblePauseButton()
            btRecorderText.postValue("STOP") // Change the text of recorder button on Starting the recording
    }

    // After release the recorder Stop the recording and add the recording to the recycler view in recorder fragment
    private fun stopRecording(){
        closeRecording()
        invisiblePauseButton()
        btRecorderText.postValue("RECORD")  // Change the text of recorder button after completing the recording
        val item = Item(tempFileName,tempTime)
        newItem.postValue(item)
        isRecording = false
        isRecordingPaused = false
        tempTime = 0L
        currTime.postValue(longToTime(tempTime))
    }

    // Release the media recorder
    fun closeRecording(){
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    // Start the player timer or get value of player and update the seekBar
    fun startPlayerTimer(){
        var runnable:Runnable? = null
        val handler = Handler()
        runnable = Runnable {
            if(player.playbackState == ExoPlayer.STATE_READY || player.playbackState== ExoPlayer.STATE_BUFFERING){
                currentPlayingPos.postValue(player.currentPosition)
                handler.postDelayed(runnable!!, 1000)
            }
        }
        handler.postDelayed(runnable, 0)
    }

    // Start the upper time in recording fragment using runnable
    private fun startRecorderTimer(){
        var runnable: Runnable? = null
        val handler = Handler()
        runnable = Runnable {
            if(isRecording){
                tempTime += 1000
                handler.postDelayed(runnable!!, 1000)
            }
            currTime.postValue(longToTime(tempTime))
        }
        handler.postDelayed(runnable, 0)
    }

    // get all the previous recordings saved in memory and add in list
    private fun listFiles(){
        val path = path
        val directory = File(path)
        val files: Array<File> = directory.listFiles()!!

        val mmr = MediaMetadataRetriever()      // Metadata retriever for getting the duration

        for (i in files) {
            val uri: Uri = Uri.parse("$path/${i.name}")
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val millis = durationStr.toLong()
            list.add(Item(i.name,millis))
        }
        startList.postValue(list)
    }

    // Play audio in using exoPlayer by taking the name and adding it to path
    fun playAudio(name: String){
        player.clearMediaItems()
        player.setMediaSource(factory.createMediaSource(MediaItem.fromUri("$path/$name")))
        player.prepare()
        player.playWhenReady = true
    }

    //Release the player when app goes to the background
    fun releasePlayer(){
        player.release()
    }

    // Making visible the pause Button on starting the recording
    private fun visiblePauseButton(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pauseButtonState.postValue(true)
    }

    //Making invisible the pause Button on Completing the recording
    private fun invisiblePauseButton(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pauseButtonState.postValue(false)
    }

    //Pause/Resume recorder on api>24
    fun pauseOrResumeRecorder(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if(isRecording) {
                recorder?.pause()
                pauseButtonText.postValue("RESUME")
                isRecordingPaused = true
            }
            else {
                recorder?.resume()
                pauseButtonText.postValue("PAUSE")
                isRecordingPaused = false
            }
            isRecording = !isRecording
            startRecorderTimer()
        }
    }
}