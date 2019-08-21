package com.dohman.holdempucker.util

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

fun Context.buildSimpleDialog(
    title: Int,
    message: Int,
    positiveText: Int,
    negativeText: Int,
    positiveListener: DialogInterface.OnClickListener? = null,
    negativeListener: DialogInterface.OnClickListener? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText, positiveListener)
        .setNegativeButton(negativeText, negativeListener)
        .setCancelable(false)
        .create()
        .show()
}