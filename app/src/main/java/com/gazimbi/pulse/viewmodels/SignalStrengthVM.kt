package com.gazimbi.pulse.viewmodels

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignalStrengthVM : ViewModel() {

    private val _signalStrength = MutableLiveData<Int>()
    val signalStrength: LiveData<Int> get() = _signalStrength

    fun measureSignalStrength(context: Context) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val phoneStateListener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                super.onSignalStrengthsChanged(signalStrength)
                // Update signal strength, this example uses GSM strength as a placeholder
                _signalStrength.postValue(signalStrength.level * 10 - 100) // Example conversion
            }
        }

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }
}