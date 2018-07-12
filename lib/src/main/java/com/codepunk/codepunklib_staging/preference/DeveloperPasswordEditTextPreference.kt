package com.codepunk.codepunklib_staging.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.EditTextPreference
import android.util.AttributeSet
import android.util.Log

class DeveloperPasswordEditTextPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.yesNoPreferenceStyle,
        defStyleRes: Int = 0) :
        EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    //region Nested classes

    companion object {
        private val TAG = DeveloperPasswordEditTextPreference::class.java.simpleName

        private class SavedState: BaseSavedState {

            companion object CREATOR : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }

            var requestCount: Int = 0

            constructor(superState: Parcelable): super(superState)

            constructor(source: Parcel): super(source) {
                requestCount = source.readInt()
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                super.writeToParcel(dest, flags)
                dest?.writeInt(requestCount)
            }

            override fun describeContents(): Int {
                return 0
            }
        }
    }

    //endregion Nested classes

    //region Fields

    private var requestCount = 0

    //endregion Fields

    //region Constructors

    init {

    }

    //endregion Constructors

    //region Inherited methods

    override fun onClick() {
        Log.d(TAG, "onClick!")
        super.onClick()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState;
        }

        val myState = SavedState(superState)
        myState.requestCount = requestCount
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        requestCount = state.requestCount
    }

    //endregion Inherited methods
}