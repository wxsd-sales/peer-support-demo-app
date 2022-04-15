package com.example.webexandroid.messaging.spaces.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.message.LocalFile
import com.example.webexandroid.BaseActivity
import com.example.webexandroid.R
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.databinding.ActivitySpaceDetailBinding
import com.example.webexandroid.databinding.SentMessageBinding
import com.example.webexandroid.messaging.composer.MessageComposerActivity
import com.example.webexandroid.messaging.spaces.ReplyMessageModel
import com.example.webexandroid.messaging.spaces.SpaceMessageModel
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.example.webexandroid.calling.CallActivity
import com.example.webexandroid.databinding.DialogPostMessageHandlerBinding
import com.example.webexandroid.messaging.composer.MentionsPlugin
import com.example.webexandroid.messaging.composer.MessageComposerViewModel
import com.example.webexandroid.utils.extensions.hideKeyboard
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_space_detail.*
import org.koin.android.ext.android.inject

class SpaceDetailActivity : BaseActivity() {

    companion object {
        enum class ComposerType {
            POST_SPACE,
            POST_PERSON_ID,
            POST_PERSON_EMAIL
        }
        fun getIntent(context: Context, spaceId: String, type: ComposerType, messageId: String? = null, replyParentMessage: ReplyMessageModel?,guestUser:Boolean): Intent {
            val intent = Intent(context, SpaceDetailActivity::class.java)
            intent.putExtra(Constants.Intent.SPACE_ID, spaceId)
            intent.putExtra(Constants.Intent.COMPOSER_TYPE, type)
            intent.putExtra(Constants.Intent.MESSAGE_ID, messageId)
            intent.putExtra(Constants.Intent.COMPOSER_REPLY_PARENT_MESSAGE, replyParentMessage)
            intent.putExtra(Constants.Intent.GUEST_USER, guestUser)
            return intent
        }

    }

    lateinit var messageClientAdapter: MessageClientAdapter
    lateinit var binding: ActivitySpaceDetailBinding


    private val spaceDetailViewModel: SpaceDetailViewModel by inject()
    private val messageComposerViewModel: MessageComposerViewModel by inject()
    private lateinit var composerType: ComposerType
    private lateinit var spaceId: String
    var guestUser: Boolean = false
    private var messageId: String? = null
    private var id: String? = null
    private var replyParentMessage: ReplyMessageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SpaceDetailActivity"

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""
        composerType = intent.getSerializableExtra(Constants.Intent.COMPOSER_TYPE) as Companion.ComposerType
        guestUser = intent.getBooleanExtra(Constants.Intent.GUEST_USER,false)

        messageId = intent.getStringExtra(Constants.Intent.MESSAGE_ID)
        replyParentMessage = intent.getParcelableExtra(Constants.Intent.COMPOSER_REPLY_PARENT_MESSAGE)
        spaceDetailViewModel.spaceId = spaceId
        id = spaceDetailViewModel.spaceId
        //spaceMessageRecyclerView.scrollToPosition(messageClientAdapter.itemCount-1)
        DataBindingUtil.setContentView<ActivitySpaceDetailBinding>(this, R.layout.activity_space_detail)
                .also { binding = it }
                .apply {
                    val messageActionBottomSheetFragment = MessageActionBottomSheetFragment({ message -> spaceDetailViewModel.deleteMessage(message) },
                            { message -> spaceDetailViewModel.markMessageAsRead(message) },
                            { message -> replyMessageListener(message) },
                            { message -> editMessage(message)})

                    messageClientAdapter = MessageClientAdapter(messageActionBottomSheetFragment, supportFragmentManager)
                    spaceMessageRecyclerView.adapter = messageClientAdapter

                    //spaceMessageRecyclerView.scrollToPosition(spaceDetailViewModel.getMessages().get)

                    binding.phoneImage.setOnClickListener {
                    it.context.startActivity(CallActivity.getOutgoingIntent(it.context, spaceId,guestUser))
                    }
                    setUpObservers()

                    swipeContainer.setOnRefreshListener {
                        spaceDetailViewModel.getMessages()
                       // spaceMessageRecyclerView.smoothScrollToPosition(spaceMessageRecyclerView.getAdapter()?.itemCount!!)
                    }
                    spaceMessageRecyclerView.scrollToPosition(50)
                    //Log.i(tag,"message"+messageClientAdapter.itemCount)
                    //spaceMessageRecyclerView.scrollToPosition(messageClientAdapter.itemCount-1)
//                    postMessageFAB.setOnClickListener {
//                        ContextCompat.startActivity(this@SpaceDetailActivity,
//                                MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE, spaceDetailViewModel.spaceId, null), null)
//                    }
                    postMessageFAB.setOnClickListener {
                        sendButtonClicked()
                    }
                    setUpObservers()

                }
    }

    private fun sendButtonClicked() {
        if (binding.editGchatMessage.text.isEmpty()) {
            showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_error, getString(R.string.post_message_empty_error))
        } else {
            messageId?.let {
                // Edit message flow
                editMessage(it) }
                ?: composerType.let { type ->
                    id?.let {
                        when (type) {
                            Companion.ComposerType.POST_SPACE -> {
                                postToSpace(it, null)
                            }
                            Companion.ComposerType.POST_PERSON_ID -> {
                                postPersonById(it, null)
                            }
                            Companion.ComposerType.POST_PERSON_EMAIL -> {
                                postPersonByEmail(it, null)
                            }
                        }
                    }
                }
        }
    }

    private fun editMessage(messageId: String) {
        val str = binding.editGchatMessage.text.toString()
        val messageContent = binding.editGchatMessage.getMessageContent()
        val text: Message.Text = Message.Text.plain(str)


        messageComposerViewModel.editMessage(messageId, text, messageContent.messageInputMentions)
    }

    private fun postPersonByEmail(email: String, files: ArrayList<LocalFile>?) {
        val emailAddress = EmailAddress.fromString(email)
        emailAddress?.let {
            messageComposerViewModel.postToPerson(emailAddress, binding.editGchatMessage.text.toString(), true, files)
            showProgress()
        } ?: run {
            showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_error, getString(R.string.post_message_email_empty))
        }
    }

    private fun postPersonById(personId: String, files: ArrayList<LocalFile>?) {
        messageComposerViewModel.postToPerson(personId, binding.editGchatMessage.text.toString(), true, files)
        showProgress()
    }

    private fun showProgress() {
        binding.progressLayout.visibility = View.VISIBLE
    }

    private fun postToSpace(spaceId: String, files: ArrayList<LocalFile>?) {
        val messageContent = binding.editGchatMessage.getMessageContent()

        var progress = true

        replyParentMessage?.let { replyMessage ->
            val str = binding.editGchatMessage.text.toString()

            val text: Message.Text? = Message.Text.plain(str)


            text?.let { msgTxt ->
                val draft = Message.draft(msgTxt)

                messageContent.messageInputMentions?.let { mentionsArray ->
                    for (item in mentionsArray) {
                        draft.addMentions(item)
                    }
                }

                files?.let { filesArray ->
                    for (item in filesArray) {
                        draft.addAttachments(item)
                    }
                }

                draft.setParent(replyMessage.getMessage())

                messageComposerViewModel.postMessageDraft(spaceId, draft)
            } ?: run {
                progress = false
                showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_error, getString(R.string.post_message_invalid_message))
            }
        } ?: run {
            messageComposerViewModel.postToSpace(spaceId, binding.editGchatMessage.text.toString(), true, messageContent.messageInputMentions, files)
        }

        if (progress) {
            showProgress()
        }
    }



    private fun replyMessageListener(message: SpaceMessageModel) {
        val model = ReplyMessageModel(
                        message.spaceId,
                        message.messageId,
                        message.created,
                        message.isSelfMentioned,
                        message.parentId,
                        message.isReply,
                        message.personId,
                        message.personEmail,
                        message.toPersonId,
                        message.toPersonEmail)
        ContextCompat.startActivity(this@SpaceDetailActivity,
                MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE, spaceDetailViewModel.spaceId, model), null)
    }

    private fun editMessage(message: SpaceMessageModel) {
        startActivity(MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE,
                spaceDetailViewModel.spaceId, null, message.messageId))
    }

    override fun onResume() {
        super.onResume()
        spaceDetailViewModel.getSpaceById()
        getMessages()
        spaceMessageRecyclerView.smoothScrollToPosition(spaceMessageRecyclerView.getAdapter()?.itemCount!!)
    }

    private fun getMessages() {
        binding.noMessagesLabel.visibility = View.GONE
        binding.progressLayout.visibility = View.VISIBLE
        spaceDetailViewModel.getMessages()
    }

    private fun displayPostMessageHandler(message: Message) {
        val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this)

        builder.setTitle(R.string.message_details)

        DialogPostMessageHandlerBinding.inflate(layoutInflater)
            .apply {
                messageData = message
                val msg = message.getTextAsObject()

                msg.getMarkdown()?.let {
                    messageBodyTextView.text = Html.fromHtml(msg.getMarkdown(), Html.FROM_HTML_MODE_LEGACY)
                } ?: run {
                    msg.getPlain()?.let {
                        messageBodyTextView.text = Html.fromHtml(msg.getPlain(), Html.FROM_HTML_MODE_LEGACY)
                    }
                }
                builder.setView(this.root)
                builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }
    }

    private fun resetView() {
        binding.editGchatMessage.text.clear()
        hideKeyboard(binding.editGchatMessage)
        hideProgress()
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun hideProgress() {
        binding.progressLayout.visibility = View.GONE
    }

    private fun setUpObservers() {
        messageComposerViewModel.postMessages.observe(this@SpaceDetailActivity, Observer {
            it?.let {
                //displayPostMessageHandler(it)
            } ?: run {
                showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_internal_error, "")
            }
            resetView()
        })

        messageComposerViewModel.postMessageError.observe(this@SpaceDetailActivity, Observer {
            it?.let {
                showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_internal_error, it)
            } ?: run {
                showDialogWithMessage(this@SpaceDetailActivity, R.string.post_message_internal_error, "")
            }
            resetView()
        })

        messageComposerViewModel.editMessage.observe(this@SpaceDetailActivity, Observer {
            it?.let {
                showDialogWithMessage(this@SpaceDetailActivity, null, getString(R.string.message_edit_successful))
            } ?: run {
                showDialogWithMessage(this@SpaceDetailActivity, null, getString(R.string.edit_message_internal_error))
            }
            resetView()
        })

        spaceDetailViewModel.space.observe(this@SpaceDetailActivity, Observer {
            binding.space = it
        })

        spaceDetailViewModel.spaceMessages.observe(this@SpaceDetailActivity, Observer { list ->
            list?.let {
                binding.progressLayout.visibility = View.GONE
                binding.swipeContainer.isRefreshing = false

                if (it.isEmpty()) {
                    binding.noMessagesLabel.visibility = View.VISIBLE
                } else {
                    binding.noMessagesLabel.visibility = View.GONE
                }

                messageClientAdapter.messages.clear()
                messageClientAdapter.messages.addAll(it)
//                Toast.makeText(this, messageClientAdapter.itemCount.toString(), Toast.LENGTH_LONG)
//                    .show();
//                spaceMessageRecyclerView.scrollToPosition(messageClientAdapter.itemCount)
                messageClientAdapter.notifyDataSetChanged()


            }
        })

        spaceDetailViewModel.deleteMessage.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                val position = messageClientAdapter.messages.indexOf(it)
                messageClientAdapter.messages.removeAt(position)
                messageClientAdapter.notifyItemRemoved(position)
            }
        })

        spaceDetailViewModel.messageError.observe(this@SpaceDetailActivity, Observer { errorMessage ->
            errorMessage?.let {
                showErrorDialog(it)
            }
        })

        spaceDetailViewModel.markMessageAsReadStatus.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                showDialogWithMessage(this@SpaceDetailActivity, R.string.success, "Message with id ${it.messageId} marked as read")
            }
        })

        spaceDetailViewModel.getMeData.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                MessageActionBottomSheetFragment.selfPersonId = it.personId
            }
        })

        spaceDetailViewModel.messageEventLiveData.observe(this@SpaceDetailActivity, Observer { pair ->
            if(pair != null) {
                when (pair.first) {
                    WebexRepository.MessageEvent.Received -> {
                       // Log.d(tag, "Message Received event fired!")
                        if(pair.second is Message) {
                            val message = pair.second as Message
                            // For replies, find parent and add to replies list at bottom.
                            if(message.isReply()){
                                val parentMessagePosition = messageClientAdapter.getPositionById(message.getParentId()?: "")
                                // Ignore case when parent is not found, as parent might not be present in the list
                                if(parentMessagePosition != -1) {
                                    if(parentMessagePosition == messageClientAdapter.messages.size - 1 ){
                                        messageClientAdapter.messages.add(SpaceMessageModel.convertToSpaceMessageModel(message))
                                        messageClientAdapter.notifyItemInserted(messageClientAdapter.messages.size - 1)
                                    }else {
                                        var positionToInsert = parentMessagePosition + 1
                                        for(i in (parentMessagePosition + 1) until messageClientAdapter.messages.size - 1) {
                                            if (!messageClientAdapter.messages[i].isReply){
                                                positionToInsert = i;
                                                break;
                                            }
                                        }
                                        messageClientAdapter.messages.add(positionToInsert, SpaceMessageModel.convertToSpaceMessageModel(message))
                                        messageClientAdapter.notifyItemInserted(positionToInsert)
                                    }
                                }
                            }else {
                                messageClientAdapter.messages.add(SpaceMessageModel.convertToSpaceMessageModel(message))
                                messageClientAdapter.notifyItemInserted(messageClientAdapter.messages.size - 1)
                            }
                        }
                    }
                    WebexRepository.MessageEvent.Deleted -> {
                        if (pair.second is String?) {
                            //Log.d(tag, "Message Deleted event fired!")
                            val position = messageClientAdapter.getPositionById(pair.second as String? ?: "")
                            if (!messageClientAdapter.messages.isNullOrEmpty() && position != -1) {
                                messageClientAdapter.messages.removeAt(position)
                                messageClientAdapter.notifyItemRemoved(position)
                            }
                        }
                    }
                    WebexRepository.MessageEvent.MessageThumbnailUpdated -> {
                        //Log.d(tag, "Message ThumbnailUpdated event fired!")
                        val fileList: List<RemoteFile>? = pair.second as? List<RemoteFile>
                        if(!fileList.isNullOrEmpty()){
                            for( thumbnail in fileList){
                                Log.d(tag, "Message Updated thumbnail : ${thumbnail.getDisplayName()}")
                            }
                        }

                    }
//                    WebexRepository.MessageEvent.Edited -> {
//                        if (pair.second is Message) {
//                            val message = pair.second as Message
//                            val position = messageClientAdapter.getPositionById(message.getId() ?: "")
//                            if (!messageClientAdapter.messages.isNullOrEmpty() && position != -1) {
//                                messageClientAdapter.messages[position] = SpaceMessageModel.convertToSpaceMessageModel(message)
//                                messageClientAdapter.notifyItemChanged(position)
//                            }
//                        }
//                    }
                }
            }
        })
    }

}


class MessageClientAdapter(private val messageActionBottomSheetFragment: MessageActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<MessageClientViewHolder>() {
    var messages: MutableList<SpaceMessageModel> = mutableListOf()

    fun getPositionById(messageId: String): Int {
        return messages.indexOfFirst { it.messageId == messageId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageClientViewHolder {
        return MessageClientViewHolder(SentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                messageActionBottomSheetFragment, fragmentManager)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageClientViewHolder, position: Int) {
        holder.bind(messages[position])
    }

}

class MessageClientViewHolder(private val binding: SentMessageBinding, private val messageActionBottomSheetFragment: MessageActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.ViewHolder(binding.root) {
    var messageItem: SpaceMessageModel? = null
    val tag = "MessageClientViewHolder"
    private lateinit var userRef: DatabaseReference
    var anonymousname: String?=null

    init {
        binding.messageContainer.setOnClickListener {
            messageItem?.let { message ->
                MessageDetailsDialogFragment.newInstance(message.messageId).show(fragmentManager, "MessageDetailsDialogFragment")
            }
        }
    }

    fun bind(message: SpaceMessageModel) {
        binding.message = message
        messageItem = message

        binding.messageContainer.setOnLongClickListener { view ->
            messageActionBottomSheetFragment.message = message
            messageActionBottomSheetFragment.show(fragmentManager, MessageActionBottomSheetFragment.TAG)
            true
        }

        //binding.messagerName?.text=message.personEmail
        var personEmail=message.personEmail
        personEmail=personEmail.replace(".","*")
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(personEmail).child("anonymous name");

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.e("datasnapshot","exists")
//                    if(dataSnapshot.child("anonymous name").exists())
//                    {
//                        anonymousname=dataSnapshot.child("anonymous name").value.toString()
//                        Log.e("anonymousName",anonymousname)
//                       // Toast.makeText(this@MessageClientViewHolder,dataSnapshot.child("anonymous name").value.toString(),Toast.LENGTH_LONG)
//                    }
                    //val map: Map<*,*> ?= dataSnapshot.getValue(Map<*,*>::class.java)

                    anonymousname = dataSnapshot.getValue(String::class.java)
                    //Log.e("anonymousName",anonymousname)
                    binding.messagerName?.text=anonymousname
                }
                else{
                    Log.e("datasnapshot","not exists")
                    binding.messagerName?.text=message.name
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userRef.addValueEventListener(postListener)

//        if(anonymousname==null)
//        {
//            binding.messagerName?.text=message.name
//        }
//        else{
//            binding.messagerName?.text=anonymousname
//        }

        when {
            message.messageBody.getMarkdown() != null -> {
                binding.textGchatMessageMe.text =  Html.fromHtml(message.messageBody.getMarkdown(), Html.FROM_HTML_MODE_LEGACY)
            }
            message.messageBody.getPlain() != null -> {
                binding.textGchatMessageMe.text = message.messageBody.getPlain()
            }
            else -> {
                binding.textGchatMessageMe.text = ""
            }
        }

        binding.executePendingBindings()
    }
}