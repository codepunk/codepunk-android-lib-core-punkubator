package com.codepunk.codepunklibstaging.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.FragmentManager
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.AttributeSet
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.preference.ExtendedPreferenceFragmentCompat.Companion.DIALOG_FRAGMENT_TAG
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms

/* TODO
 * [ ] Need a listener to catch on Dialog closed back in MainPreferenceFragment so it can reset stuff
 */

open class PasswordPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.passwordPreferenceStyle,
        defStyleRes: Int = R.style.PasswordPreference) :
        EditTextPreference(context, attrs, defStyleAttr, defStyleRes),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback,
        Preference.OnPreferenceClickListener {

    //region Nested classes

    interface OnValidatePasswordPreferenceListener {
        fun onValidatePreferencePassword(preference: PasswordPreference, password: String?): String
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

            var suppressDialogOnNextClick: Boolean = false

            //endregion Fields

            constructor(superState: Parcelable) : super(superState)

            constructor(source: Parcel) : super(source) {
                messageDigestAlgorithm = source.readString()
                passwordHash = source.readString()
                suppressDialogOnNextClick = source.readInt() != 0
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                super.writeToParcel(dest, flags)
                dest?.run {
                    writeString(messageDigestAlgorithm)
                    writeString(passwordHash)
                    writeInt(if (suppressDialogOnNextClick) 1 else 0)
                }
            }
        }
    }

    //endregion Nested classes

    //region Fields

    private var onPreferenceClickListener: OnPreferenceClickListener? = null

    var messageDigestAlgorithm: String? = MessageDigestAlgorithms.SHA_256
    set(value) {
        digestUtils = value?.let { DigestUtils(value) }
        field = value
    }

    var passwordHash: String? = null

    var digestUtils: DigestUtils? = null
    private set

    var onValidatePasswordPreferenceListener: OnValidatePasswordPreferenceListener? = null

    private var suppressDialogOnNextClick: Boolean = false

    //endregion Fields

    //region Constructors

    init {
        super.setOnPreferenceClickListener(this)

        val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.PasswordPreference,
                defStyleAttr,
                defStyleRes)

        val index = a.getInt(R.styleable.PasswordPreference_messageDigestAlgorithm, -1)
        messageDigestAlgorithm = MessageDigestAlgorithms.values().elementAtOrNull(index)

        passwordHash = a.getString(R.styleable.PasswordPreference_passwordHash)

        a.recycle()
    }

    //endregion Constructors

    //region Inherited methods

    override fun onClick() {
        // DON'T call on click here. Do it from onPreferenceClick instead. super.onClick()
    }

    override fun getOnPreferenceClickListener(): OnPreferenceClickListener? {
        return onPreferenceClickListener
    }

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
        myState.suppressDialogOnNextClick = suppressDialogOnNextClick
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
        suppressDialogOnNextClick = state.suppressDialogOnNextClick
    }

    override fun setOnPreferenceClickListener(listener: OnPreferenceClickListener?) {
        onPreferenceClickListener = listener
    }

    //endregion Inherited methods

    //region Implemented methods

    override fun onPreferenceClick(preference: Preference?): Boolean {
        val retVal: Boolean =
                onPreferenceClickListener?.onPreferenceClick(preference) ?: false
        if (suppressDialogOnNextClick) {
            suppressDialogOnNextClick = false
        } else {
            super.onClick()
        }
        return retVal
    }

    override fun onPreferenceDisplayDialog(
            caller: PreferenceFragmentCompat,
            pref: Preference?): Boolean {
        val fragmentManager = caller.requireFragmentManager()

        // check if dialog is already showing
        if (getDialogFragment(fragmentManager) != null) {
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

    //region Methods

    fun onPasswordSuccess(caller: PreferenceFragmentCompat, positiveValue: String?) {
        getDialogFragment(caller.requireFragmentManager())
                ?.onPasswordSuccess(positiveValue)
                ?: onPositiveResult(positiveValue)
    }

    fun onPasswordFailure(
            caller: PreferenceFragmentCompat,
            reason: String? = context.getString(R.string.incorrect_password),
            shake: Boolean = false) {
        getDialogFragment(caller.requireFragmentManager())?.onPasswordFailure(reason, shake)
    }

    fun onPositiveResult(text: String?) {
        if (callChangeListener(text)) {
            this.text = text
        }
    }

    fun onNegativeResult() {
    }

    fun suppressDialogOnNextClick() {
        suppressDialogOnNextClick = true
    }

    //endregion Methods

    //region Private methods

    private fun getDialogFragment(
            fragmentManager: FragmentManager):
            PasswordPreferenceDialogFragment? {
        return fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG)
                as PasswordPreferenceDialogFragment?
    }

    //endregion Private methods
}