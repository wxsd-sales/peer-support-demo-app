package com.example.webexandroid.messaging.teams.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.messaging.teams.TeamModel
import com.example.webexandroid.messaging.teams.TeamsRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class TeamDetailViewModel(private val teamsRepo: TeamsRepository) : BaseViewModel() {
    private val _team = MutableLiveData<TeamModel>()
    val team : LiveData<TeamModel> = _team

    fun getTeamById(teamId: String){
        teamsRepo.fetchTeamById(teamId).observeOn(AndroidSchedulers.mainThread()).subscribe({ teamModel ->
            _team.postValue((teamModel))
        }, { _team.postValue(null)}).autoDispose()
    }
}