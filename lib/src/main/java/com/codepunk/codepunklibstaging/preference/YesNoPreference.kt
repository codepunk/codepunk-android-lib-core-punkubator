package com.codepunk.codepunklibstaging.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.AttributeSet
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.preference.old.ExtendedPreferenceFragmentCompat.Companion.DIALOG_FRAGMENT_TAG

open class YesNoPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.yesNoPreferenceStyle,
        defStyleRes: Int = R.style.YesNoPreference):
        DialogPreference(context, attrs, defStyleAttr, defStyleRes),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    //region Nested classes

    private class SavedState: BaseSavedState {

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        var wasPositiveResult: Boolean = false

        public constructor(superState: Parcelable): super(superState)

        public constructor(source: Parcel): super(source) {
            wasPositiveResult = source.readInt() != 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            super.writeToParcel(dest, flags)
            dest?.writeInt(if (wasPositiveResult) 1 else 0)
        }
    }

    //endregion Nested classes

    //region Fields

    private var wasPositiveResult: Boolean = false
    var value: Boolean
    get() = wasPositiveResult
    set(value) {
        wasPositiveResult = value
        persistBoolean(value)
        notifyDependencyChange(!value)
    }

    //endregion Fields

    //region Inherited methods

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getBoolean(index, false) ?: false
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) getPersistedBoolean(wasPositiveResult)
                else defaultValue as Boolean
    }

    override fun shouldDisableDependents(): Boolean {
        return !wasPositiveResult || super.shouldDisableDependents()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState = SavedState(superState)
        myState.wasPositiveResult = value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state)
        value = state.wasPositiveResult
    }

    //endregion Inherited methods

    //region Implemented methods

    override fun onPreferenceDisplayDialog(
            caller: PreferenceFragmentCompat,
            pref: Preference?): Boolean {
        val fragmentManager = caller.requireFragmentManager()

        // check if dialog is already showing
        if (fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return true
        }

        pref?.apply {
            val fragment = YesNoPreferenceDialogFragment.newInstance(key)
            fragment.setTargetFragment(caller, 0)
            fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            return true
        }

        return false
    }

    //endregion Implemented methods
}