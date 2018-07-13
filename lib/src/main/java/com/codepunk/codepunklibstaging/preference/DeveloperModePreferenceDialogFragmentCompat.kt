package com.codepunk.codepunklibstaging.preference

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.util.shake
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms

private const val ARG_PASSWORD_HASH = "passwordHash"
private const val ARG_MESSAGE_DIGEST_ALGORITHM = "messageDigestAlgorithm"

class DeveloperModePreferenceDialogFragmentCompat: EditTextPreferenceDialogFragmentCompat(),
        DialogInterface.OnShowListener,
        View.OnClickListener {

    companion object {
        private val TAG = DeveloperModePreferenceDialogFragmentCompat::class.java.simpleName

        fun newInstance(
                key: String,
                passwordHash: String? = null,
                messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256):
                DeveloperModePreferenceDialogFragmentCompat {
            return DeveloperModePreferenceDialogFragmentCompat().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEY, key)
                    putString(ARG_PASSWORD_HASH, passwordHash)
                    putString(ARG_MESSAGE_DIGEST_ALGORITHM, messageDigestAlgorithm)
                }
            }
        }
    }

    private val developerModePreference: DeveloperModePreference
    get() = preference as DeveloperModePreference

    private val passwordHash: String? by lazy {
        arguments?.getString(ARG_PASSWORD_HASH)
    }

    private val digestUtils: DigestUtils by lazy {
        DigestUtils(
                arguments?.getString(ARG_MESSAGE_DIGEST_ALGORITHM)
                        ?: MessageDigestAlgorithms.SHA3_256)
    }

    private val textInputLayout: TextInputLayout? by lazy {
        dialog.findViewById<TextInputLayout>(R.id.layout)
    }

    private val editText: EditText? by lazy {
        dialog.findViewById<TextInputEditText>(android.R.id.edit)
    }

    private val okBtn: Button by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener(this@DeveloperModePreferenceDialogFragmentCompat)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value: String? = passwordHash
            if (developerModePreference.callChangeListener(value)) {
                developerModePreference.text = value
            }
        } else {
            if (developerModePreference.callChangeListener(null)) {
                developerModePreference.removeText()
            }
        }
    }

    //region Implemented methods

    /* DialogInterface.OnShowListener */
    override fun onShow(dialog: DialogInterface?) {
        okBtn.setOnClickListener(this)
    }

    /* View.OnClickListener */
    override fun onClick(view: View?) {
        when (view) {
            okBtn -> {
                editText?.also {
                    val password = it.text.toString()
                    val hash = digestUtils.digestAsHex(password)
                    if (hash.equals(passwordHash, true)) {
                        dialog.dismiss()
                        onDialogClosed(true)
                    } else {
                        textInputLayout?.error = resources.getString(
                                R.string.pref_developer_mode_incorrect_password)
                        dialog.window.decorView.shake()
                    }
                }
            }
        }
    }

    //endregion Implemented methods
}
