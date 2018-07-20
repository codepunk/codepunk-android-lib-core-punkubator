package com.codepunk.codepunklibstaging.preference

import android.os.Bundle
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat

class PasswordPreferenceDialogFragment: EditTextPreferenceDialogFragmentCompat() {

    // region Nested classes

    companion object {
        private val TAG = PasswordPreferenceDialogFragment::class.java.simpleName

        fun newInstance(key: String): PasswordPreferenceDialogFragment {
            return PasswordPreferenceDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                }
            }
        }
    }

    // endregion Nested classes
}