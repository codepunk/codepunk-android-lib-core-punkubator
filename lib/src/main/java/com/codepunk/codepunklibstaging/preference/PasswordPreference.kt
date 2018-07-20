package com.codepunk.codepunklibstaging.preference

import android.content.Context
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.codepunk.codepunklib.preference.DialogDelegatePreferenceFragment.Companion.DIALOG_FRAGMENT_TAG

import android.util.AttributeSet
import com.codepunk.codepunklibstaging.R
import kotlin.jvm.JvmOverloads

class PasswordPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.passwordPreferenceStyle,
        defStyleRes: Int = R.style.PasswordPreference) :
        EditTextPreference(context, attrs, defStyleAttr, defStyleRes),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback{

    // region Lifecycle methods

    init {
        widgetLayoutResource = R.layout.android_preference_widget_master_switch
    }

    // endregion Lifecycle methods

    // region Implemented methods

    override fun onPreferenceDisplayDialog(
            caller: PreferenceFragmentCompat,
            pref: Preference?): Boolean {
        with (caller.requireFragmentManager()) {
            if (findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return true
            }

            pref?.also {
                val fragment = PasswordPreferenceDialogFragment.newInstance(it.key)
                fragment.setTargetFragment(caller, 0)
                fragment.show(this, DIALOG_FRAGMENT_TAG)
                return true
            }
        }
        return false
    }

    // endregion Implemented methods

}