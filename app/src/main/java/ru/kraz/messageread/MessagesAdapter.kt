package ru.kraz.messageread

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kraz.messageread.databinding.ItemMessageReceivingBinding
import ru.kraz.messageread.databinding.ItemMessageSenderBinding

class MessagesAdapter(
    private val read: (Int) -> Unit
) : ListAdapter<MessageCloud, MessagesAdapter.ViewHolder>(Diff()) {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MessageCloud)
        open fun bindRead(item: MessageCloud) {}
    }

    class SenderViewHolder(private val view: ItemMessageSenderBinding) : ViewHolder(view.root) {
        override fun bind(item: MessageCloud) {
            view.tvMessage.text = item.message
            bindRead(item)
        }

        override fun bindRead(item: MessageCloud) {
            view.stateMessage.setImageResource(if (item.messageRead) R.drawable.done_all else R.drawable.done)
        }
    }

    class ReceivingViewHolder(private val view: ItemMessageReceivingBinding) : ViewHolder(view.root) {
        override fun bind(item: MessageCloud) {
            view.tvMessage.text = item.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).iSendThis) {
            true -> SENDER_VIEW_TYPE
            false -> RECEIVING_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SENDER_VIEW_TYPE -> SenderViewHolder(ItemMessageSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RECEIVING_VIEW_TYPE -> ReceivingViewHolder(ItemMessageReceivingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!getItem(position).iSendThis) read(position)
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else {
            val action = payloads[0] as Boolean
            if (action) holder.bindRead(getItem(position))
        }
    }
    
    companion object {
        private const val SENDER_VIEW_TYPE = 0
        private const val RECEIVING_VIEW_TYPE = 1
    }
}

class Diff : DiffUtil.ItemCallback<MessageCloud>() {
    override fun areItemsTheSame(oldItem: MessageCloud, newItem: MessageCloud): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MessageCloud, newItem: MessageCloud): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: MessageCloud, newItem: MessageCloud): Any? =
        oldItem.messageRead != newItem.messageRead

}