package `in`.programy.audiorecorder.util

import `in`.programy.audiorecorder.R
import `in`.programy.audiorecorder.util.Methods.longToTime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_audio.view.*

class RvAudioAdapter(private val list: List<Item>) : RecyclerView.Adapter<RvAudioAdapter.AudioViewHolder>() {
    inner class AudioViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        // Inflating the layout with of of items in recycler view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio,parent,false)
        return AudioViewHolder(view)
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val currItem = list[position]

        holder.itemView.apply {
            tvName.text = currItem.name
            tvDuration.text = longToTime(currItem.duration)
            setOnClickListener {
                clickListener?.let { it(position) }
            }
        }
    }

    // On ClickListener for handle outside the adapter
    private var clickListener : ((Int)-> Unit)? = null

    fun setOnClickListener(listener: (Int)->Unit){
        clickListener = listener
    }
}