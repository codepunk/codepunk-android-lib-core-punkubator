package com.codepunk.codepunklibstaging.preference

import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat

class YesNoPreferenceDialogFragment: PreferenceDialogFragmentCompat() {

    //region Nested classes

    companion object {
        fun newInstance(key: String): YesNoPreferenceDialogFragment {
            return YesNoPreferenceDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                }
            }
        }
    }

    //endregion Nested classes

    //region Fields

    private val yesNoPreference: YesNoPreference
    get() = preference as YesNoPreference

    //endregion Fields

    //region Inherited methods

    override fun onDialogClosed(positiveResult: Boolean) {
        if (yesNoPreference.callChangeListener(positiveResult)) {
            yesNoPreference.value = positiveResult
        }
    }

    //endregion Inherited methods
}