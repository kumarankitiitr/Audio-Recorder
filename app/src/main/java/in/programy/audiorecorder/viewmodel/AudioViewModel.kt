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
import android.os.Handler
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

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AudioViewModel(application: Application): AndroidViewModel(application) {
    private var recorder : MediaRecorder? = null
    private val context = getApplication<AudioApplication>()

    val btRecorderText = MutableLiveData<String>()
    val startList = MutableLiveData<MutableList<Item>>()
    val currTime = MutableLiveData<String>()
    val newItem = MutableLiveData<Item>()

    private val list = mutableListOf<Item>()
    private var tempFileName = ""
    private var tempTime = 0L
    private var isRecording = false

    private val path = "${context.externalCacheDir?.absolutePath}"
    val player = SimpleExoPlayer.Builder(context).build()
    private val factory = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context,Util.getUserAgent(context,"Audio Recorder")))

    init {
        player.addAnalyticsListener(analyticsListener)
        listFiles()
    }

    fun record(){
        if(!isRecording) startRecording()
        else stopRecording()
    }

    private fun startRecording(){
        tempFileName = "${System.currentTimeMillis()}.3gp"
        val fileName = "${context.externalCacheDir?.absolutePath}/$tempFileName"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(fileName)
            try {
                prepare()
            }catch (t: Throwable){
                t.printStackTrace()
            }
            start()
            isRecording = true
            startRecorderTimer()
            btRecorderText.postValue("STOP")
        }
    }

    fun stopRecording(){
        closeRecording()
        btRecorderText.postValue("RECORD")
        val item = Item(tempFileName,tempTime)
        newItem.postValue(item)
        //player.addMediaSource(factory.createMediaSource(MediaItem.fromUri("$path/${item.name}")))
        //player.prepare()
        isRecording = false
    }

    fun closeRecording(){
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

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

    private fun startRecorderTimer(){
        var runnable: Runnable? = null
        val handler = Handler()
        runnable = Runnable {
            if(isRecording){
                tempTime += 1000
                handler.postDelayed(runnable!!, 1000)
            }else{
                tempTime = 0L
            }
            currTime.postValue(longToTime(tempTime))
        }
        handler.postDelayed(runnable, 0)
    }

    private fun listFiles(){
        val path = path
        val directory = File(path)
        val files: Array<File> = directory.listFiles()!!

        val mmr = MediaMetadataRetriever()

        for (i in files) {
            val uri: Uri = Uri.parse("$path/${i.name}")
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val millis = durationStr.toLong()
            list.add(Item(i.name,millis))
        }
        startList.postValue(list)
        //preparePlayer()
    }

    fun playAudio(name: String){
        player.clearMediaItems()
        player.setMediaSource(factory.createMediaSource(MediaItem.fromUri("$path/$name")))
        player.prepare()
        player.playWhenReady = true
    }

    private fun preparePlayer(){
        val mediaSources = mutableListOf<MediaSource>()
        for(i in list){
            mediaSources.add(factory.createMediaSource(MediaItem.fromUri("$path/${i.name}")))
        }
        player.addMediaSources(mediaSources)
        player.prepare()
    }
}