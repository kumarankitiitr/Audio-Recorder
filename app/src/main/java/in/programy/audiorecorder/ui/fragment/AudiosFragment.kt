package `in`.programy.audiorecorder.ui.fragment

import `in`.programy.audiorecorder.R
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.currentItemIndex
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.currentPlayingPos
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.isPlaying
import `in`.programy.audiorecorder.exoplayer.PlayerStatic.playbackState
import `in`.programy.audiorecorder.ui.MainActivity
import `in`.programy.audiorecorder.util.Item
import `in`.programy.audiorecorder.util.RvAudioAdapter
import `in`.programy.audiorecorder.viewmodel.AudioViewModel
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ShuffleOrder
import kotlinx.android.synthetic.main.fragment_audios.*
import kotlin.random.Random


class AudiosFragment : Fragment(R.layout.fragment_audios) {

    lateinit var rvAudioAdapter: RvAudioAdapter
    private lateinit var viewModel: AudioViewModel
    lateinit var audioList: MutableList<Item>
    lateinit var player: SimpleExoPlayer
    private var isShuffleMode = false
    private var currentPlayingIndex: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).audioViewModel
        player = viewModel.player
        audioList = mutableListOf()
        var isShuffleMode = false
        var isRepeatMode = false

        setUpRecyclerView()

        viewModel.startList.observe(activity as MainActivity, Observer {
            audioList.addAll(it)
            rvAudioAdapter.notifyDataSetChanged()
        })

        viewModel.newItem.observe(activity as MainActivity, Observer {
            audioList.add(it)
            rvAudioAdapter.notifyItemInserted(audioList.size-1)
        })

        currentItemIndex.observe(activity as MainActivity, Observer {
            tvTitle.text = audioList[currentPlayingIndex].name
            seekBar.max = audioList[currentPlayingIndex].duration.toInt()
            viewModel.startPlayerTimer()
        })

        currentPlayingPos.observe(activity as MainActivity, Observer {
            Log.e("current pos",it.toString())
            seekBar.progress = it.toInt()
        })

        isPlaying.observe(activity as MainActivity, Observer {
            if(it) changeToPauseIcon()
            else changeToPlayIcon()
        })

        playbackState.observe(activity as MainActivity, Observer {
            if(it == ExoPlayer.STATE_ENDED) playerLayout.visibility = View.GONE
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?){
                if(seekBar != null) player.seekTo(seekBar.progress.toLong())
            }
        })

        rvAudioAdapter.setOnClickListener {
            viewModel.playAudio(audioList[it].name)
            currentPlayingIndex = it
            playerLayout.visibility = View.VISIBLE
        }

        ivPlayPause.setOnClickListener {
            if(player.isPlaying) pause()
            else play()
        }

        ivNext.setOnClickListener {
            if (currentPlayingIndex != -1){
                if(isShuffleMode) setShufflePlayingIndex()
                else{
                    if(audioList.size-1 > currentPlayingIndex) viewModel.playAudio(audioList[++currentPlayingIndex].name)
                    else Toast.makeText(context,"Not Available",Toast.LENGTH_SHORT).show()
                }
            }
        }

        ivPrev.setOnClickListener {
            if (currentPlayingIndex != -1){
                if (isShuffleMode) setShufflePlayingIndex()
                else{
                    if(0 < currentPlayingIndex) viewModel.playAudio(audioList[--currentPlayingIndex].name)
                    else Toast.makeText(context,"Not Available",Toast.LENGTH_SHORT).show()
                }
            }
        }

        ivShuffle.setOnClickListener {
            if (isShuffleMode) offShuffle()
            else onShuffle()
            isShuffleMode = !isShuffleMode
        }

        ivRepeat.setOnClickListener {
            if(isRepeatMode) offRepeat()
            else onRepeat()
            isRepeatMode = !isRepeatMode
        }
    }

    private fun setUpRecyclerView(){
        rvAudioAdapter = RvAudioAdapter(audioList)
        rvAudio.apply {
            adapter = rvAudioAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun play(){
        player.play()
        changeToPauseIcon()
    }

    private fun changeToPauseIcon(){
        ivPlayPause.setImageDrawable(
                context?.let { c ->
                    ContextCompat.getDrawable(c, R.drawable.ic_baseline_pause_24)
                }
        )
    }

    private fun pause(){
        player.pause()
        changeToPlayIcon()
    }

    private fun changeToPlayIcon(){
        ivPlayPause.setImageDrawable(
                context?.let { c ->
                    ContextCompat.getDrawable(c, R.drawable.ic_baseline_play_arrow_24)
                }
        )
    }

    private fun onShuffle(){
        isShuffleMode = true
        ivShuffle.background = context?.let { c ->
            ContextCompat.getDrawable(c, R.drawable.item_enable) }
    }

    private fun offShuffle(){
        isShuffleMode = false
        ivShuffle.background = null
    }

    private fun onRepeat(){
        player.repeatMode = Player.REPEAT_MODE_ONE
        ivRepeat.background = context?.let { c ->
            ContextCompat.getDrawable(c, R.drawable.item_enable) }
    }

    private fun offRepeat(){
        player.repeatMode = Player.REPEAT_MODE_OFF
        ivRepeat.background = null
    }

    private fun setShufflePlayingIndex(){
        currentPlayingIndex = Random.nextInt(audioList.size)
        viewModel.playAudio(audioList[currentPlayingIndex].name)
    }
}