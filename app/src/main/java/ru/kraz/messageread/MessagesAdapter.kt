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
) : ListAdapter<MessageUi, MessagesAdapter.ViewHolder>(Diff()) {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MessageUi)
        open fun bindRead(item: MessageUi) {}
    }

    class SenderViewHolder(private val view: ItemMessageSenderBinding) : ViewHolder(view.root) {
        override fun bind(item: MessageUi) {
            view.tvMessage.text = item.message
            view.tvCreatedDate.text = item.createdDate
            bindRead(item)
        }

        override fun bindRead(item: MessageUi) {
            view.stateMessage.setImageResource(if (item.messageRead) R.drawable.done_all else R.drawable.done)
        }
    }

    class ReceivingViewHolder(private val view: ItemMessageReceivingBinding) : ViewHolder(view.root) {
        override fun bind(item: MessageUi) {
            view.tvMessage.text = item.message
            view.tvCreatedDate.text = item.createdDate
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
        if (!getItem(position).iSendThis && !getItem(position).messageRead) read(position)
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

class Diff : DiffUtil.ItemCallback<MessageUi>() {
    override fun areItemsTheSame(oldItem: MessageUi, newItem: MessageUi): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MessageUi, newItem: MessageUi): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: MessageUi, newItem: MessageUi): Any? =
        oldItem.messageRead != newItem.messageRead

}