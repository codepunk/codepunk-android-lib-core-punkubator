package com.codepunk.codepunklibstaging.preference

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment

const val ARG_TITLE = "title"
const val ARG_MESSAGE = "message"
const val ARG_POSITIVE_BUTTON_TEXT = "positiveButtonText"
const val ARG_NEGATIVE_BUTTON_TEXT = "negativeButtonText"

class ConfirmDisableDeveloperModeDialogFragment: AppCompatDialogFragment(),
        DialogInterface.OnClickListener {

    companion object {
        private val TAG = ConfirmDisableDeveloperModeDialogFragment::class.java.simpleName

        fun newInstance(
                title: CharSequence? = null,
                message: CharSequence? = null,
                positiveButtonText: CharSequence? = null,
                negativeButtonText: CharSequence? = null): ConfirmDisableDeveloperModeDialogFragment {
            return ConfirmDisableDeveloperModeDialogFragment().apply {
                arguments = Bundle().apply {
                    putCharSequence(ARG_TITLE, title)
                    putCharSequence(ARG_MESSAGE, message)
                    putCharSequence(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText)
                    putCharSequence(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                }
            }
        }
    }

    private var title: CharSequence? = null

    private var message: CharSequence? = null

    private var positiveButtonText: CharSequence? = null

    private var negativeButtonText: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            title = getCharSequence(ARG_TITLE)
            message = getCharSequence(ARG_MESSAGE)
            positiveButtonText = getCharSequence(ARG_POSITIVE_BUTTON_TEXT)
            negativeButtonText = getCharSequence(ARG_NEGATIVE_BUTTON_TEXT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, this)
                .setNegativeButton(negativeButtonText, this)
                .create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        targetFragment?.onActivityResult(
                targetRequestCode,
                if (which == DialogInterface.BUTTON_NEGATIVE) Activity.RESULT_CANCELED
                    else Activity.RESULT_OK,
                null)
    }
}