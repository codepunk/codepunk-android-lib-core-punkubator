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

    private var authToken: String? = INVALID

    //endregion Fields

    //region Inherited methods

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener(this@PasswordPreferenceDialogFragment)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (passwordPreference.callChangeListener(authToken)) {
                passwordPreference.text = authToken
            }
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
                authToken = when {
                    passwordPreference.validationListener != null ->
                            passwordPreference.validationListener!!.onValidatePassword(password)
                    passwordPreference.passwordHash == null -> INVALID
                    else -> {
                        val other = passwordPreference.digestUtils?.digestAsHex(password) ?: password
                        if (passwordPreference.passwordHash.equals(other, true))
                            passwordPreference.authToken ?: other
                        else INVALID
                    }
                }
                when (authToken) {
                    INVALID -> {
                        layout?.error = resources.getString(R.string.incorrect_password)
                        dialog.window.decorView.shake()
                    }
                    else -> {
                        onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                        dismiss()
                    }
                }
            }
        }
    }

    //endregion Implemented methods
}