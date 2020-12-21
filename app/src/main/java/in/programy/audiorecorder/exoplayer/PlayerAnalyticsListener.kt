package `in`.programy.audiorecorder.exoplayer

import `in`.programy.audiorecorder.exoplayer.PlayerStatic.currentItemIndex
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.currentPlayingPos
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.isPlaying
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.playbackState
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.analytics.AnalyticsListener

object PlayerAnalyticsListener {
    val analyticsListener = object : AnalyticsListener{
        override fun onPlayerError(
            eventTime: AnalyticsListener.EventTime,
            error: ExoPlaybackException
        ) {
            super.onPlayerError(eventTime, error)
            error.printStackTrace()
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlay: Boolean) {
            isPlaying.postValue(isPlay)
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            playbackState.postValue(state)
        }

        override fun onMediaItemTransition(
            eventTime: AnalyticsListener.EventTime,
            mediaItem: MediaItem?,
            reason: Int
        ) {
            super.onMediaItemTransition(eventTime, mediaItem, reason)
            currentItemIndex.postValue(eventTime.currentWindowIndex)
        }

        override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
            super.onSeekStarted(eventTime)
            currentPlayingPos.postValue(eventTime.currentPlaybackPositionMs)
        }
    }
}