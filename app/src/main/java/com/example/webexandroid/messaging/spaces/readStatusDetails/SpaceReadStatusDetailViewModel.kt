package com.example.webexandroid.messaging.spaces.readStatusDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.messaging.spaces.SpaceReadStatusModel
import com.example.webexandroid.messaging.spaces.SpacesRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class SpaceReadStatusDetailViewModel(private val spacesRepo: SpacesRepository) : BaseViewModel() {
    private val _spaceReadStatus = MutableLiveData<SpaceReadStatusModel>()
    val spaceReadStatus : LiveData<SpaceReadStatusModel> = _spaceReadStatus

    fun getSpaceReadStatusById(spaceId: String){
        spacesRepo.getSpaceReadStatusById(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _spaceReadStatus.postValue(it)
        }, {_spaceReadStatus.postValue(null)}).autoDispose()
    }
}