package com.codepunk.codepunklibstaging.preference

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

abstract class ExtendedPreferenceFragmentCompat: PreferenceFragmentCompat() {

    companion object {
        val DIALOG_FRAGMENT_TAG = ExtendedPreferenceFragmentCompat::class.java.name + ".DIALOG"
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        var handled = false

        if (preference is OnPreferenceDisplayDialogCallback) {
            handled = preference.onPreferenceDisplayDialog(this, preference)
        }

        if (handled) {
            return
        }

        super.onDisplayPreferenceDialog(preference)
    }
}