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
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms

val INVALID: String? = null

open class PasswordPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.passwordPreferenceStyle,
        defStyleRes: Int = R.style.PasswordPreference) :
        EditTextPreference(context, attrs, defStyleAttr, defStyleRes),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    //region Nested classes

    interface OnValidationListener {
        fun onValidatePassword(password: String?): String
    }

    companion object {
        class SavedState: BaseSavedState {

            //region Nested classes

            companion object CREATOR : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }

            //endregion Nested classes

            //region Fields

            var messageDigestAlgorithm: String? = MessageDigestAlgorithms.SHA_256

            var passwordHash: String? = null

            //endregion Fields

            constructor(superState: Parcelable) : super(superState)

            constructor(source: Parcel) : super(source) {
                messageDigestAlgorithm = source.readString()
                passwordHash = source.readString()
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                super.writeToParcel(dest, flags)
                dest?.run {
                    writeString(messageDigestAlgorithm)
                    writeString(passwordHash)
                }
            }
        }
    }

    //endregion Nested classes

    //region Fields

    var authToken: String? = null

    var messageDigestAlgorithm: String? = MessageDigestAlgorithms.SHA_256
    set(value) {
        digestUtils = value?.let { DigestUtils(value) }
        field = value
    }

    var passwordHash: String? = null

    var digestUtils: DigestUtils? = null
    private set

    var validationListener: OnValidationListener? = null

    //endregion Fields

    //region Constructors

    init {
        val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.PasswordPreference,
                defStyleAttr,
                defStyleRes)

        authToken = a.getString(R.styleable.PasswordPreference_authToken)

        val index = a.getInt(R.styleable.PasswordPreference_messageDigestAlgorithm, -1)
        messageDigestAlgorithm = MessageDigestAlgorithms.values().elementAtOrNull(index)

        passwordHash = a.getString(R.styleable.PasswordPreference_passwordHash)

        a.recycle()
    }

    //endregion Constructors

    //region Inherited methods

    override fun getText(): String? {
        // Prevent any persisted value from populating the password dialog
        return null
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState = SavedState(superState)
        myState.messageDigestAlgorithm = messageDigestAlgorithm
        myState.passwordHash = passwordHash
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        messageDigestAlgorithm = state.messageDigestAlgorithm
        passwordHash = state.passwordHash
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
            val fragment = PasswordPreferenceDialogFragment.newInstance(key)
            fragment.setTargetFragment(caller, 0)
            fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            return true
        }

        return false
    }

    //endregion Implemented methods
}