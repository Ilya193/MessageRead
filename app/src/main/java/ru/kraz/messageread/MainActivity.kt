package ru.kraz.messageread

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kraz.messageread.databinding.ActivityMainBinding
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModel()

    private val adapter = MessagesAdapter {
        viewModel.readMessage(it)
    }

    private lateinit var uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        uuid = sharedPreferences.getString("uuid", "") ?: ""

        if (uuid == "") {
            sharedPreferences.edit().putString("uuid", UUID.randomUUID().toString()).apply()
            viewModel.init(sharedPreferences.getString("uuid", "") ?: "")
        } else viewModel.init(uuid ?: "")

        binding.btnSend.setOnClickListener {
            val text = binding.inputMessage.text.toString()
            if (text != "")
                viewModel.sendMessage(text)
            binding.inputMessage.setText("")
        }

        settingRecyclerView()

        viewModel.uiState.observe(this) {
            adapter.submitList(it)
            binding.rvMessages.smoothScrollToPosition(it.size - 1)
        }

        viewModel.fetchMessages()
    }

    private fun settingRecyclerView() {

        binding.rvMessages.setHasFixedSize(true)
        binding.rvMessages.adapter = adapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.deleteMessage(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                val senderId = adapter.getSenderId(viewHolder.adapterPosition)
                if (senderId != null && uuid == senderId) {
                    val itemView = viewHolder.itemView
                    val deleteIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.delete) ?: return
                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    deleteIcon.setBounds(
                        itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )
                    deleteIcon.draw(c)
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                } else adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvMessages)
    }
}