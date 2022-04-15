package com.example.webexandroid.search

import com.ciscowebex.androidsdk.space.Space
import java.util.*

data class SpaceDetails(val id: String="", val title: String="", val spaceType: Space.SpaceType?, val isLocked: Boolean=false, val lastActivity: Date, val created: Date, val teamId: String="", val sipAddress: String="")
