package com.dohman.holdempucker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.dohman.holdempucker.R
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
        isVertical: Boolean,
        cardSize: Int? = null
    ) {
        val cover = if (isVertical) R.drawable.red_back_vertical else R.drawable.red_back

//        if (cardSize ?: 0 > 50) {
//            front.setImageResource(cover)
//            back.setImageResource(cover)
//        } else {
            if (flipView.isBackSide) {
                back.setImageResource(cover)
                if (isVertical) resId?.let { front.setImageResource(it) } else bitmap?.let { front.setImageBitmap(it) }
            } else {
                front.setImageResource(cover)
                if (isVertical) resId?.let { back.setImageResource(it) } else bitmap?.let { back.setImageBitmap(it) }
            }
//        }

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

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
    }
}