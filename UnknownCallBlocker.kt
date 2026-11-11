package com.example.callblocker

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

class UnknownCallBlocker : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        // শুধু ইনকামিং কল চেক করুন
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val number = callDetails.handle?.schemeSpecificPart

        val response = if (isInContacts(number)) {
            // পরিচিত নাম্বার: স্বাভাবিকভাবে ঢুকবে
            CallResponse.Builder().build()
        } else {
            // অপরিচিত নাম্বার: ব্লক + কল লগ ও নোটিফিকেশন থেকে হাইড
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)        // কল হিস্ট্রিতে দেখাবে না
                .setSkipNotification(true)   // মিসড কল নোটিফিকেশন আসবে না
                .build()
        }
        respondToCall(callDetails, response)
    }

    private fun isInContacts(number: String?): Boolean {
        // প্রাইভেট/হিডেন নাম্বার অপরিচিত ধরা হবে
        if (number.isNullOrBlank()) return false

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null, null, null
        )?.use { return it.count > 0 }
        return false
    }
}
