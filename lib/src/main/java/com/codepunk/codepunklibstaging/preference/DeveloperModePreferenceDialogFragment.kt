package com.codepunk.codepunklibstaging.preference

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.codepunk.codepunklibstaging.R
import com.codepunk.codepunklibstaging.util.shake
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms

private const val ARG_PASSWORD_HASH = "passwordHash"
private const val ARG_MESSAGE_DIGEST_ALGORITHM = "messageDigestAlgorithm"
private const val REQUEST_CODE = 0

class DeveloperModePreferenceDialogFragment: EditTextPreferenceDialogFragmentCompat(),
        DialogInterface.OnKeyListener,
        DialogInterface.OnShowListener,
        View.OnClickListener {

    companion object {
        private val TAG = DeveloperModePreferenceDialogFragment::class.java.simpleName
        private val DIALOG_FRAGMENT_TAG =
                DeveloperModePreferenceDialogFragment::class.java.name + ".DIALOG"

        fun newInstance(
                key: String,
                passwordHash: String? = null,
                messageDigestAlgorithm: String = MessageDigestAlgorithms.SHA_256):
                DeveloperModePreferenceDialogFragment {
            return DeveloperModePreferenceDialogFragment().apply {
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
                        ?: MessageDigestAlgorithms.SHA_256)
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

    private val cancelBtn: Button by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (developerModePreference.callChangeListener(null)) {
                        onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
                        dismiss()
                    }
                }
            }
            else -> { super.onActivityResult(requestCode, resultCode, data) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
            setOnKeyListener(this@DeveloperModePreferenceDialogFragment)
            setOnShowListener(this@DeveloperModePreferenceDialogFragment)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value: String? = passwordHash
            if (developerModePreference.callChangeListener(value)) {
                developerModePreference.text = value
            }
        } else if (developerModePreference.isStale) {
            if (developerModePreference.callChangeListener(null)) {
                Log.d(TAG, "********** in onDialogClosed: About to call removeText! **********")
// TODO TEMP                developerModePreference.removeText()
            }
        } else {
            // TODO Probably no action needed here
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        builder?.setCancelable(false)
        if (developerModePreference.isStale) {
            builder?.setMessage(developerModePreference.staleDialogMessage)
        } else {
            builder?.setMessage(developerModePreference.dialogMessage)
        }
    }

    //region Implemented methods

    // DialogInterface.OnKeyListener
    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event?.action == KeyEvent.ACTION_UP
                && !event.isCanceled
                && developerModePreference.isStale) {
            showConfirmDisableDeveloperModeDialog()
            return true
        }

        return false
    }

    // DialogInterface.OnShowListener
    override fun onShow(dialog: DialogInterface?) {
        okBtn.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)
    }

    // View.OnClickListener
    override fun onClick(view: View?) {
        when (view) {
            okBtn -> {
                editText?.also {
                    val password = it.text.toString()
                    val hash = digestUtils.digestAsHex(password)
                    if (hash.equals(passwordHash, true)) {
                        onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                        dismiss()
                    } else {
                        textInputLayout?.error =
                                resources.getString(R.string.incorrect_password)
                        dialog.window.decorView.shake()
                    }
                }
            }
            cancelBtn -> {
                if (developerModePreference.isStale) {
                    showConfirmDisableDeveloperModeDialog()
                } else {
                    onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
                    dismiss()
                }
            }
        }
    }

    //endregion Implemented methods

    //region Private methods

    private fun showConfirmDisableDeveloperModeDialog() {
        // check if dialog is already showing
        if (requireFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }

        val f = ConfirmDisableDeveloperModeDialogFragment.newInstance(
                developerModePreference.cancelStaleDialogTitle,
                developerModePreference.cancelStaleDialogMessage,
                developerModePreference.cancelStaleDialogPositiveButtonText,
                developerModePreference.cancelStaleDialogNegativeButtonText)
        f.setTargetFragment(this, REQUEST_CODE)
        f.show(requireFragmentManager(), DIALOG_FRAGMENT_TAG)
    }

    //endregion Private methods
}
