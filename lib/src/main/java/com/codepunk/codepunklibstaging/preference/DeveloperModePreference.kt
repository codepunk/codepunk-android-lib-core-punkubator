package com.codepunk.codepunklibstaging.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.AttributeSet
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.preference.ExtendedPreferenceFragmentCompat.Companion.DIALOG_FRAGMENT_TAG
import org.apache.commons.codec.digest.MessageDigestAlgorithms

// TODO onSetInitialValue and such

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

            var messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256

            var passwordHash: String? = null

            var showPasswordDialog = true

            var remainingClicksToUnlock = 0

            constructor(superState: Parcelable): super(superState)

            constructor(source: Parcel): super(source) {
                additionalClicksToUnlock = source.readInt()
                messageDigestAlgorithm = source.readString()
                passwordHash = source.readString()
                showPasswordDialog = (source.readInt() != 0)
                remainingClicksToUnlock = source.readInt()
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                super.writeToParcel(dest, flags)
                dest?.apply {
                    writeInt(additionalClicksToUnlock)
                    writeString(messageDigestAlgorithm)
                    writeString(passwordHash)
                    writeInt(if (showPasswordDialog) 1 else 0)
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

    private var additionalClicksToUnlock = 0

    private var messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256

    private var passwordHash: String? = null

    private var showPasswordDialog = true

    private var remainingClicksToUnlock = 0

    var onRemainingClicksChangeListener: OnRemainingClicksChangeListener? = null

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

        messageDigestAlgorithm = a.getString(
                    R.styleable.DeveloperModePreference_messageDigestAlgorithm) ?: messageDigestAlgorithm

        passwordHash = a.getString(
                R.styleable.DeveloperModePreference_passwordHash)

        showPasswordDialog = a.getBoolean(
                R.styleable.DeveloperModePreference_showPasswordDialog, showPasswordDialog)

        a.recycle()
    }

    //endregion Constructors

    //region Inherited methods

    override fun onClick() {
        if (remainingClicksToUnlock > 0) {
            onRemainingClicksChangeListener?.onRemainingClicksChanged(
                    this, remainingClicksToUnlock)
            remainingClicksToUnlock--
        } else {
            super.onClick()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        // TODO Check isPersistent for the ONE persisted field (which I assume is "validatedPasswordHash")?
        /*
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState;
        }
        */

        val myState = SavedState(superState)
        myState.additionalClicksToUnlock = additionalClicksToUnlock
        myState.messageDigestAlgorithm = messageDigestAlgorithm
        myState.passwordHash = passwordHash
        myState.showPasswordDialog = showPasswordDialog
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
        messageDigestAlgorithm = state.messageDigestAlgorithm
        passwordHash = state.passwordHash
        showPasswordDialog = state.showPasswordDialog
        remainingClicksToUnlock = state.remainingClicksToUnlock
    }

    /*
    override fun setText(text: String?) {
        sharedPreferences.edit().apply {
            if (text == null) remove(key) else putString(key, text)
        }.apply()

        if (text == null) {

        } else {
            super.setText(text)
        }
    }
    */

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
            val fragment = DeveloperModePreferenceDialogFragmentCompat
                    .newInstance(key, passwordHash)
            fragment.setTargetFragment(caller, 0)
            fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            return true
        }

        return false
    }

    //endregion Implemented methods

    //region Methods

    fun removeText() {
        val wasBlocking = shouldDisableDependents();
        text = null

        removeString()

        val isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
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