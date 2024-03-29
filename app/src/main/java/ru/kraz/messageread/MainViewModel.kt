package ru.kraz.messageread

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val db: FirebaseDatabase,
) : ViewModel() {

    private val messages = mutableListOf<MessageUi>()

    private val _uiState = MutableLiveData<List<MessageUi>>()
    val uiState: LiveData<List<MessageUi>> get() = _uiState

    private var uuid = ""

    private val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun init(uuid: String) {
        this.uuid = uuid
    }

    fun deleteMessage(position: Int) {
        db.reference.child("messages/${messages[position].id}").removeValue()
    }

    fun readMessage(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val message = messages[position].copy(messageRead = true)
        db.reference.child("messages/${message.id}").setValue(message.map())
    }

    fun sendMessage(text: String) = viewModelScope.launch(Dispatchers.IO) {
        val id = db.reference.child("messages").push().key ?: ""
        db.reference.child("messages/$id").setValue(MessageCloud(id, text, uuid))
    }

    fun fetchMessages() = viewModelScope.launch(Dispatchers.IO) {
        db.reference.child("messages").orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (i in snapshot.children) {
                        val message = i.getValue(MessageCloud::class.java)
                        val date = Date(message!!.createdDate["timestamp"] as Long)
                        val formattedDate = sdf.format(date)
                        if (message.senderId == uuid) messages.add(message.map(formattedDate, true))
                        else messages.add(message.map(formattedDate, false))
                    }
                    _uiState.postValue(messages.toList())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

data class MessageCloud(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val createdDate: Map<String, Any> = mapOf("timestamp" to ServerValue.TIMESTAMP),
    val messageRead: Boolean = false,
) {
    fun map(formattedDate: String, iSendThis: Boolean): MessageUi =
        MessageUi(
            id,
            message,
            senderId,
            formattedDate,
            createdDate,
            iSendThis,
            messageRead
        )
}

data class MessageUi(
    val id: String,
    val message: String,
    val senderId: String,
    val createdDate: String,
    val createdDateMap: Map<String, Any>,
    val iSendThis: Boolean = false,
    val messageRead: Boolean = false,
) {
    fun map(): MessageCloud = MessageCloud(
        id,
        message,
        senderId,
        createdDateMap,
        messageRead
    )
}
