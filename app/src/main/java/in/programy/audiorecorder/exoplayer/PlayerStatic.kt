package `in`.programy.audiorecorder.exoplayer

import androidx.lifecycle.MutableLiveData

object PlayerStatic {
    val currentPlayingPos = MutableLiveData<Long>()
    val currentItemIndex = MutableLiveData<Int>()
    val isPlaying = MutableLiveData<Boolean>()
    val playbackState = MutableLiveData<Int>()
}