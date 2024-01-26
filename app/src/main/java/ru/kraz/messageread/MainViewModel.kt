package ru.kraz.messageread

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val db: FirebaseDatabase
) : ViewModel() {

    private val messages = mutableListOf<MessageCloud>()

    private val _uiState = MutableLiveData<List<MessageCloud>>()
    val uiState: LiveData<List<MessageCloud>> get() = _uiState

    private var uuid = ""

    fun init(uuid: String) {
        this.uuid = uuid
    }

    fun readMessage(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val message = messages[position].copy(messageRead = true)
        db.reference.child("messages/${message.id}").setValue(message)
    }

    fun sendMessage(text: String) = viewModelScope.launch(Dispatchers.IO) {
        val id = db.reference.child("messages").push().key ?: ""
        db.reference.child("messages/$id").setValue(MessageCloud(id, text, uuid))
    }

    fun fetchMessages() = viewModelScope.launch(Dispatchers.IO) {
        db.reference.child("messages").orderByChild("createdDate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (i in snapshot.children) {
                    val messageCloud = i.getValue(MessageCloud::class.java)
                    if (messageCloud!!.senderId == uuid) messages.add(messageCloud.copy(iSendThis = true))
                    else messages.add(messageCloud)
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
    @get:Exclude val iSendThis: Boolean = false,
    val messageRead: Boolean = false
)

