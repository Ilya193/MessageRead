package ru.kraz.messageread

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("uuid", "")

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

        binding.rvMessages.setHasFixedSize(true)
        binding.rvMessages.adapter = adapter

        viewModel.uiState.observe(this) {
            adapter.submitList(it)
        }

        viewModel.fetchMessages()
    }
}