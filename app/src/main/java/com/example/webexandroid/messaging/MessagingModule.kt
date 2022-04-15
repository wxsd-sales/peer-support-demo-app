package com.example.webexandroid.messaging

import com.example.webexandroid.messaging.composer.MessageComposerRepository
import com.example.webexandroid.messaging.composer.MessageComposerViewModel
import com.example.webexandroid.messaging.spaces.SpacesRepository
import com.example.webexandroid.messaging.spaces.SpacesViewModel
import com.example.webexandroid.messaging.spaces.detail.MessageViewModel
import com.example.webexandroid.messaging.spaces.detail.SpaceDetailViewModel
import com.example.webexandroid.messaging.spaces.members.MembershipRepository
import com.example.webexandroid.messaging.spaces.members.MembershipViewModel
import com.example.webexandroid.messaging.spaces.members.membersReadStatus.MembershipReadStatusViewModel
import com.example.webexandroid.messaging.spaces.readStatusDetails.SpaceReadStatusDetailViewModel
import com.example.webexandroid.messaging.teams.TeamsRepository
import com.example.webexandroid.messaging.teams.TeamsViewModel
import com.example.webexandroid.messaging.teams.detail.TeamDetailViewModel
import com.example.webexandroid.messaging.teams.membership.TeamMembershipRepository
import com.example.webexandroid.messaging.teams.membership.TeamMembershipViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val messagingModule = module {
    viewModel { TeamsViewModel(get(), get()) }
    viewModel { TeamDetailViewModel(get()) }
    viewModel { TeamMembershipViewModel(get()) }

    single { TeamsRepository(get()) }


    viewModel { SpacesViewModel(get(), get(), get(), get()) }
    viewModel { SpaceDetailViewModel(get(), get(), get()) }

    single { SpacesRepository(get()) }

    viewModel { MembershipViewModel(get(), get()) }

    single { MembershipRepository(get()) }

    single { TeamMembershipRepository(get()) }

    viewModel { SpaceReadStatusDetailViewModel(get()) }

    viewModel { MessageViewModel(get()) }

    viewModel { MembershipReadStatusViewModel(get(), get()) }

    single { MessageComposerRepository(get()) }

    viewModel { MessageComposerViewModel(get(), get(), get()) }
}