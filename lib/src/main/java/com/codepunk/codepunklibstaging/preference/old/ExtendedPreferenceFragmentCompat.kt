package com.codepunk.codepunklibstaging.preference.old

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

abstract class ExtendedPreferenceFragmentCompat: PreferenceFragmentCompat() {

    companion object {
        val DIALOG_FRAGMENT_TAG = ExtendedPreferenceFragmentCompat::class.java.name + ".DIALOG"
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is OnPreferenceDisplayDialogCallback &&
                preference.onPreferenceDisplayDialog(this, preference)) {
            return
        }

        super.onDisplayPreferenceDialog(preference)
    }
}