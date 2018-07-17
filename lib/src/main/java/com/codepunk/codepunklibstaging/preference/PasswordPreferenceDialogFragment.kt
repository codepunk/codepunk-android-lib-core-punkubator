package com.codepunk.codepunklibstaging.preference

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat
import android.view.View
import android.widget.EditText
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.util.shake

// TODO NEXT How to validate authToken in future visits? I think this will be a function of the client and NOT this generic class

class PasswordPreferenceDialogFragment: EditTextPreferenceDialogFragmentCompat(),
        DialogInterface.OnShowListener,
        View.OnClickListener {

    //region Nested objects

    companion object {
        private val TAG = PasswordPreferenceDialogFragment::class.java.simpleName

        fun newInstance(key: String):
                PasswordPreferenceDialogFragment {
            return PasswordPreferenceDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                }
            }
        }
    }

    //endregion Nested objects

    //region Fields

    private val passwordPreference: PasswordPreference
        get() = preference as PasswordPreference

    private val edit by lazy {
        dialog.findViewById<EditText>(android.R.id.edit)
                ?: throw IllegalStateException("Dialog view must contain an EditText with id" +
                " @android:id/edit")
    }

    private val layout by lazy {
        dialog.findViewById<TextInputLayout>(R.id.layout)
    }

    private val okBtn by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    private var positiveValue: String? = null

    //endregion Fields

    //region Inherited methods

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener(this@PasswordPreferenceDialogFragment)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            passwordPreference.onPositiveResult(positiveValue)
        } else {
            passwordPreference.onNegativeResult()
        }
    }

    //endregion Inherited methods

    //region Implemented methods

    // DialogInterface.OnShowListener
    override fun onShow(dialog: DialogInterface?) {
        okBtn.setOnClickListener(this)
    }

    // View.OnClickListener
    override fun onClick(view: View?) {
        when (view) {
            okBtn -> {
                val password = edit.text.toString()
                passwordPreference.apply {
                    when {
                        onValidatePasswordPreferenceListener != null ->
                            onValidatePasswordPreferenceListener!!
                                    .onValidatePreferencePassword(this, password)
                        passwordHash != null -> {
                            val success = when (digestUtils) {
                                null -> passwordHash.equals(password, false)
                                else -> passwordHash.equals(
                                        digestUtils!!.digestAsHex(password), true)
                            }
                            if (success) {
                                this@PasswordPreferenceDialogFragment
                                        .onPasswordSuccess(passwordHash!!)
                            } else {
                                this@PasswordPreferenceDialogFragment.onPasswordFailure()
                            }
                        }
                        else -> throw IllegalStateException(
                                this::class.java.simpleName + " requires either " +
                                        "onValidatePasswordPreferenceListener or " +
                                        "passwordHash to be non-null.")
                    }
                }
            }
        }
    }

    //endregion Implemented methods

    //region Methods

    fun onPasswordSuccess(positiveValue: String?) {
        this.positiveValue = positiveValue
        onClick(dialog, DialogInterface.BUTTON_POSITIVE)
        dismiss()
    }

    fun onPasswordFailure(
            errorMessage: String? = getString(R.string.incorrect_password),
            shake: Boolean = true) {
        positiveValue = null
        errorMessage?.run { layout?.error = this }
        if (shake) {
            dialog.window.decorView.shake()
        }
    }

    //endregion Methods
}