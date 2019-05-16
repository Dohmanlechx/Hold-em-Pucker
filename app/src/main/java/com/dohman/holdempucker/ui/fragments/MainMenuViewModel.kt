package com.dohman.holdempucker.ui.fragments

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModel;
import com.dohman.holdempucker.dagger.RepositoryComponent
import com.dohman.holdempucker.repositories.ResourceRepository
import javax.inject.Inject

class MainMenuViewModel : ViewModel() {
    @Inject
    lateinit var appRepo: ResourceRepository

    init {
        RepositoryComponent.inject(this)
    }

    fun getLinearGradient(tv: AppCompatTextView): LinearGradient = LinearGradient(
        0f, 0f, tv.paint.measureText(tv.text.toString()), tv.textSize,
        intArrayOf(
            Color.parseColor("#FF0000"),
            Color.parseColor("#FF6464")
//            Color.parseColor("#F97C3C"),
//            Color.parseColor("#FDB54E"),
//            Color.parseColor("#64B678"),
//            Color.parseColor("#478AEA"),
//            Color.parseColor("#8446CC")
        ),
        null, Shader.TileMode.MIRROR
    )
}
