package com.example.antithefttask.data

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


fun Context.loadAdMobBanner(frameLayout: FrameLayout) {
    val bannerAd = AdView(this)
    bannerAd.setAdSize(AdSize.BANNER)
    bannerAd.adUnitId = "ca-app-pub-3940256099942544/2014213617"
    val adRequest = AdRequest.Builder().build()
    bannerAd.loadAd(adRequest)
    bannerAd.adListener = object : AdListener() {
        override fun onAdLoaded() {
            frameLayout.removeAllViews()
            frameLayout.addView(bannerAd)
        }
    }
}

fun Activity.loadCollapsable(frameLayout: FrameLayout) {
    val adViewCollapsable = AdView(this)
    adViewCollapsable?.adUnitId = "ca-app-pub-3940256099942544/2014213617"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        adViewCollapsable?.setAdSize(adSize(this, frameLayout))
    else adViewCollapsable?.setAdSize(getAdSize(this, frameLayout))
    val extras = Bundle()
    extras.putString("collapsible", "bottom")
    val adRequest = AdRequest.Builder().addNetworkExtrasBundle(
        com.google.ads.mediation.admob.AdMobAdapter::class.java, extras
    ).build()
    adViewCollapsable?.loadAd(adRequest)
    adViewCollapsable?.adListener = object : AdListener() {
        override fun onAdLoaded() {
            val params = LinearLayout.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
            )
            if (adViewCollapsable?.parent != null) (adViewCollapsable?.parent as ViewGroup).removeView(
                adViewCollapsable
            )
            frameLayout.addView(adViewCollapsable, params)

        }
    }

}

fun adSize(activity: Activity, adContainer: FrameLayout): AdSize {
    var adWidth = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val bounds = windowMetrics.bounds
        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = bounds.width().toFloat()
        }
        val density = activity.resources.displayMetrics.density
        adWidth = (adWidthPixels / density).toInt()

    }
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)

}

fun getAdSize(mActivity: Activity, adContainer: FrameLayout): AdSize {
    val display = mActivity.windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)

    val density = outMetrics.density

    var adWidthPixels = adContainer.width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()

    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
}

