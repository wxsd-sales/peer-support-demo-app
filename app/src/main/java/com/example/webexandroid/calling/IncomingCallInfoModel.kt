package com.example.webexandroid.calling.calling

import com.ciscowebex.androidsdk.phone.Call

abstract class IncomingCallInfoModel(var call: Call?) {
    var isEnabled: Boolean = true
}