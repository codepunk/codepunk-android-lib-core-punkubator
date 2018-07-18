package com.codepunk.codepunklibstaging.preference.old

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.util.AttributeSet
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.preference.old.ExtendedPreferenceFragmentCompat.Companion.DIALOG_FRAGMENT_TAG
import org.apache.commons.codec.digest.MessageDigestAlgorithms

// TODO NEXT:
// [X] onSetInitialValue and such
// [X] Handle "redundant" calls (i.e. Clicked when already developer) [ Start with DeveloperModePreference.onClick and isDeveloper field ]
// [X] Show msg dialog if user tries to cancel when we're stale (i.e. we were already in developer mode) [ Start with onCanceledPasswordDialogWhileStale ]
// [ ] Do something about "setText" because we DON'T want the big huge hash appearing in the edit text dialog
// [X] Change message in dialog when stale
// [ ] Handle "stale" developer password on app open (Needs to happen in app open but also maybe in ViewModel)
// [ ] Handle disabling developer options with an "Are you sure" dialog (See saved screenshot on Desktop)
// [ ] Re-examine this "removeText/removeString" logic below. Don't like it.
// [ ] Respond to "showPasswordDialog" attribute
// [X] Get rid of "DeveloperState" enum?
// [X] Get rid of Classes I no longer need in app

open class DeveloperModePreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.developerModePreferenceStyle,
        defStyleRes: Int = R.style.DeveloperModePreference) :
        EditTextPreference(context, attrs, defStyleAttr, defStyleRes),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    //region Nested classes

    interface OnRemainingClicksChangeListener {
        fun onRemainingClicksChanged(preference: DeveloperModePreference, remainingClicksToUnlock: Int)
    }

    companion object {
        private val TAG = DeveloperModePreference::class.java.simpleName

        private class SavedState: BaseSavedState {

            companion object CREATOR : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }

            var additionalClicksToUnlock = 0

            var cancelStaleDialogTitle: CharSequence? = null

            var cancelStaleDialogMessage: CharSequence? = null

            var cancelStaleDialogPositiveButtonText: CharSequence? = null

            var cancelStaleDialogNegativeButtonText: CharSequence? = null

            var messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256

            var passwordHash: String? = null

            var showPasswordDialog = true

            var staleDialogMessage: CharSequence? = null

            var remainingClicksToUnlock = 0

            constructor(superState: Parcelable): super(superState)

            constructor(source: Parcel): super(source) {
                additionalClicksToUnlock = source.readInt()
                cancelStaleDialogTitle =
                        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
                cancelStaleDialogMessage =
                        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
                cancelStaleDialogPositiveButtonText =
                        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
                cancelStaleDialogNegativeButtonText =
                        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
                messageDigestAlgorithm = source.readString()
                passwordHash = source.readString()
                showPasswordDialog = (source.readInt() != 0)
                staleDialogMessage = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
                remainingClicksToUnlock = source.readInt()
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                super.writeToParcel(dest, flags)
                dest?.apply {
                    writeInt(additionalClicksToUnlock)
                    TextUtils.writeToParcel(cancelStaleDialogTitle, dest, flags)
                    TextUtils.writeToParcel(cancelStaleDialogMessage, dest, flags)
                    TextUtils.writeToParcel(cancelStaleDialogPositiveButtonText, dest, flags)
                    TextUtils.writeToParcel(cancelStaleDialogNegativeButtonText, dest, flags)
                    writeString(messageDigestAlgorithm)
                    writeString(passwordHash)
                    writeInt(if (showPasswordDialog) 1 else 0)
                    TextUtils.writeToParcel(staleDialogMessage, dest, flags)
                    writeInt(remainingClicksToUnlock)
                }
            }

            override fun describeContents(): Int {
                return 0
            }
        }
    }

    //endregion Nested classes

    //region Fields

    val isDeveloper: Boolean
    get() {
        return getPersistedString(null)
                ?.equals(passwordHash, true) ?: false
    }

    val isStale: Boolean
    get() {
        val persistedString = getPersistedString(null)
        return if (persistedString == null) false
        else !persistedString.equals(passwordHash, true)
    }

    var additionalClicksToUnlock = 0

    var staleDialogMessage: CharSequence? = null

    var cancelStaleDialogTitle: CharSequence? = null

    var cancelStaleDialogMessage: CharSequence? = null

    var cancelStaleDialogPositiveButtonText: CharSequence? = null

    var cancelStaleDialogNegativeButtonText: CharSequence? = null

    var messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256

    var passwordHash: String? = null

    var showPasswordDialog = true

    var onRemainingClicksChangeListener: OnRemainingClicksChangeListener? = null

    private var remainingClicksToUnlock = 0

    //endregion Fields

    //region Constructors

    init {
        val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.DeveloperModePreference,
                defStyleAttr,
                defStyleRes)

        additionalClicksToUnlock = a.getInt(
                R.styleable.DeveloperModePreference_additionalClicksToUnlock,
                additionalClicksToUnlock).also {
                    remainingClicksToUnlock = it
                }

        cancelStaleDialogTitle = a.getString(
                R.styleable.DeveloperModePreference_cancelStaleDialogTitle)

        cancelStaleDialogMessage = a.getString(
                R.styleable.DeveloperModePreference_cancelStaleDialogMessage)

        cancelStaleDialogPositiveButtonText = a.getString(
                R.styleable.DeveloperModePreference_cancelStaleDialogPositiveButtonText)

        cancelStaleDialogNegativeButtonText = a.getString(
                R.styleable.DeveloperModePreference_cancelStaleDialogNegativeButtonText)

        messageDigestAlgorithm = a.getString(
                R.styleable.DeveloperModePreference_messageDigestAlgorithm)
                ?: messageDigestAlgorithm

        passwordHash = a.getString(
                R.styleable.DeveloperModePreference_passwordHash)

        showPasswordDialog = a.getBoolean(
                R.styleable.DeveloperModePreference_showPasswordDialog, showPasswordDialog)

        staleDialogMessage = a.getString(R.styleable.DeveloperModePreference_staleDialogMessage)

        a.recycle()
    }

    //endregion Constructors

    //region Inherited methods

    override fun onClick() {
        when {
            isDeveloper -> {
                // No action? TODO Make sure of this
            }
            remainingClicksToUnlock > 0 -> {
                onRemainingClicksChangeListener?.onRemainingClicksChanged(
                        this, remainingClicksToUnlock)
                remainingClicksToUnlock--
            }
            else -> super.onClick()
        }
    }

    /* I don't think I need these 2 since EditTextPreference handles them the same way we want
    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        return super.onGetDefaultValue(a, index)
    }
    */

    /*
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        super.onSetInitialValue(restoreValue, defaultValue)
        if (isDeveloper) {
            // TODO ??
        }
    }
    */

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        // TODO Check isPersistent for the ONE persisted field (which I assume is "validatedPasswordHash")?
        /*
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        */

        val myState = SavedState(superState)
        myState.additionalClicksToUnlock = additionalClicksToUnlock
        myState.cancelStaleDialogTitle = cancelStaleDialogTitle
        myState.cancelStaleDialogMessage = cancelStaleDialogMessage
        myState.cancelStaleDialogPositiveButtonText = cancelStaleDialogPositiveButtonText
        myState.cancelStaleDialogNegativeButtonText = cancelStaleDialogNegativeButtonText
        myState.messageDigestAlgorithm = messageDigestAlgorithm
        myState.passwordHash = passwordHash
        myState.showPasswordDialog = showPasswordDialog
        myState.staleDialogMessage = staleDialogMessage
        myState.remainingClicksToUnlock = remainingClicksToUnlock
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        additionalClicksToUnlock = state.additionalClicksToUnlock
        cancelStaleDialogTitle = state.cancelStaleDialogTitle
        cancelStaleDialogMessage = state.cancelStaleDialogMessage
        cancelStaleDialogPositiveButtonText = state.cancelStaleDialogPositiveButtonText
        cancelStaleDialogNegativeButtonText = state.cancelStaleDialogNegativeButtonText
        messageDigestAlgorithm = state.messageDigestAlgorithm
        passwordHash = state.passwordHash
        showPasswordDialog = state.showPasswordDialog
        staleDialogMessage = state.staleDialogMessage
        remainingClicksToUnlock = state.remainingClicksToUnlock
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
            val fragment = DeveloperModePreferenceDialogFragment.newInstance(key, passwordHash)
            fragment.setTargetFragment(caller, 0)
            fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            return true
        }

        return false
    }

    //endregion Implemented methods

    //region Methods

    fun removeText() {
        val wasBlocking = shouldDisableDependents()
        text = null

        removeString()

        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking)
        }
    }

    //endregion Methods

    //region Protected methods

    protected fun removeString(): Boolean {
        if (!shouldPersist()) {
            return false
        }

        if (preferenceDataStore != null) {
            preferenceDataStore!!.putString(key, null)
        } else {
            sharedPreferences.edit().remove(key).apply()
        }

        return true
    }

    //endregion Protected methods
}