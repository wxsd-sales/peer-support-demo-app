package com.example.webexandroid.calling.calling

import com.ciscowebex.androidsdk.phone.Call

data class OneToOneIncomingCallModel(val _call: Call?): IncomingCallInfoModel(_call)