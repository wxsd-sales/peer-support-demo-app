package com.example.webexandroid.messaging

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.Webex
import com.example.webexandroid.messaging.spaces.SpaceMessageModel
import com.example.webexandroid.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.message.Mention
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.MessageClient
import com.ciscowebex.androidsdk.message.RemoteFile
import com.ciscowebex.androidsdk.space.Space
import com.example.webexandroid.search.SpaceDetails
import com.example.webexandroid.search.ui.home.FetchData
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

open class MessagingRepository(private val webex: Webex) {
    val tag = "MessagingRepository"
    public var id: String? =null
    public val _spaces = MutableLiveData<Space>()
    val spaces: LiveData<Space> = _spaces

    enum class FileDownloadEvent {
        DOWNLOAD_COMPLETE,
        DOWNLOAD_FAILED
    }

    fun addSpace(spaceTitle: String, teamId: String?): Observable<SpaceModel> {
        return Single.create<SpaceModel> { emitter ->
            webex.spaces.create(spaceTitle, teamId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val space = result.data
                    id= result.data?.id
                    _spaces.postValue(result.data)
                    //Log.e("space details", result.data?.id)
                    emitter.onSuccess(SpaceModel.convertToSpaceModel(space))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }


    fun deleteMessage(messageId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.messages.delete(messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun markMessageAsRead(spaceId: String, messageId: String? = null): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.messages.markAsRead(spaceId, messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getMessage(messageId: String): Observable<SpaceMessageModel> {
        return Single.create<SpaceMessageModel> { emitter ->
            webex.messages.get(messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(SpaceMessageModel.convertToSpaceMessageModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }

            })
        }.toObservable()
    }

    fun editMessage(messageId: String, messageText: Message.Text, mentions: ArrayList<Mention>?): Observable<SpaceMessageModel> {
        return Single.create<SpaceMessageModel> { emitter ->
            webex.messages.get(messageId, CompletionHandler { messageResult ->
                if (messageResult.isSuccessful) {
                    val message = messageResult.data
                    if (message != null) {
                        webex.messages.edit(message, messageText, mentions, CompletionHandler { result ->
                            if (result.isSuccessful) {
                                val messageObj = result.data
                                emitter.onSuccess(SpaceMessageModel.convertToSpaceMessageModel(messageObj))
                            } else {
                                emitter.onError(Throwable(result.error?.errorMessage))
                            }
                        })
                    } else {
                        emitter.onError(Throwable("Error: Message cannot be found"))
                    }
                } else {
                    emitter.onError(Throwable(messageResult.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun downloadThumbnail(remoteFile: RemoteFile, file: File): Observable<Uri> {
        return Single.create<Uri> { emitter ->
            webex.messages.downloadThumbnail(remoteFile, file, CompletionHandler { result ->
                if (result.isSuccessful) {
                    if (result.data != null) {
                        emitter.onSuccess(result.data!!)
                    } else {
                        emitter.onError(Throwable("Unable to retrieve thumbnail"))
                    }
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }


    fun downloadFile(remoteFile: RemoteFile, file: File, progressEmitter: Emitter<Double>, completionEmitter: Emitter<Pair<FileDownloadEvent, String>>) {
        webex.messages.downloadFile(remoteFile, file,
                object : MessageClient.ProgressHandler {
                    override fun onProgress(bytes: Double) {
                        //Log.d(tag, "downloadFile bytes: $bytes")
                        progressEmitter.onNext(bytes)
                    }
                },
                CompletionHandler { fileUrlResult ->
                    if (fileUrlResult.isSuccessful) {
                        //Log.d(tag, "downloadFile onComplete success: ${fileUrlResult.data}")
                        fileUrlResult.data?.let {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_COMPLETE, it.toString()))
                        } ?: run {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                        }
                    } else {
                        //Log.d(tag, "downloadFile onComplete failed")
                        fileUrlResult.error?.let {
                            it.errorMessage?.let { errorMessage ->
                                completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, errorMessage))
                            } ?: run {
                                completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                            }
                        } ?: run {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                        }
                    }
                }
        )
    }
}