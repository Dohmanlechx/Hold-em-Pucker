package com.dohman.holdempucker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.wajahatkarim3.easyflipview.EasyFlipView

object ViewUtil {

    fun setScaleOnRotatedView(fromView: View, toView: View) =
        toView.apply {
            layoutParams.width = fromView.height
            layoutParams.height = fromView.width
        }

    fun setImagesOnFlipView(
        flipView: EasyFlipView,
        front: AppCompatImageView,
        back: AppCompatImageView,
        resId: Int?,
        bitmap: Bitmap?,
        isVertical: Boolean
    ) {
        val cover = if (isVertical) R.drawable.red_back_vertical else R.drawable.red_back

        if (flipView.isBackSide) {
            back.setImageResource(cover)
            if (isVertical) resId?.let { front.setImageResource(it) } else bitmap?.let {
                front.setImageBitmap(
                    it
                )
            }
        } else {
            front.setImageResource(cover)
            if (isVertical) resId?.let { back.setImageResource(it) } else bitmap?.let {
                back.setImageBitmap(
                    it
                )
            }
        }

        flipView.visibility = View.VISIBLE
    }

    fun getRotatedBitmap(context: Context, resId: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)

        val scaledBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(context.resources, resId),
            173,
            264,
            true
        )

        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    fun buildLobbyNameDialog(context: Context, fGoToGameFragment: (String, String?) -> Unit) =
        AlertDialog.Builder(context).apply {
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL

            this.setTitle(context.getString(R.string.dialog_lobby_header))

            val lobbyNameEditText = EditText(context)
            val lobbyPasswordEditText = EditText(context)

            val maxLength = 15
            val filterArray = arrayOfNulls<InputFilter>(1)
            filterArray[0] = InputFilter.LengthFilter(maxLength)

            lobbyNameEditText.apply {
                filters = filterArray
                hint = "(unnamed)"
                isSingleLine = true
            }

            lobbyPasswordEditText.apply {
                filters = filterArray
                hint = "password (optional)"
                isSingleLine = true
            }

            layout.apply {
                addView(lobbyNameEditText)
                addView(lobbyPasswordEditText)
            }

            setPositiveButton(context.getString(R.string.dialog_lobby_positive)) { _, _ ->
                var name: String = lobbyNameEditText.text.toString().trim()
                var password: String? = lobbyPasswordEditText.text.toString().trim()
                if (password?.isBlank() == true) password = null

                if (name.isBlank()) name = "(unnamed)"
                fGoToGameFragment.invoke(name, password)
            }

            setView(layout)
            this.create()
            this.show()
        }


    fun buildLobbyPasswordInput(context: Context, fGoToGameFragment: (String) -> Unit) =
        AlertDialog.Builder(context).apply {
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL

            this.setTitle(context.getString(R.string.dialog_lobby_password_required))

            val lobbyPasswordEditText = EditText(context)

            val maxLength = 15
            val filterArray = arrayOfNulls<InputFilter>(1)
            filterArray[0] = InputFilter.LengthFilter(maxLength)

            lobbyPasswordEditText.apply {
                filters = filterArray
                setSingleLine(true)
            }

            layout.addView(lobbyPasswordEditText)

            setPositiveButton(context.getString(R.string.dialog_lobby_positive)) { _, _ ->
                fGoToGameFragment.invoke(lobbyPasswordEditText.text.toString().trim())
            }

            setView(layout)
            this.create()
            this.show()
        }
}