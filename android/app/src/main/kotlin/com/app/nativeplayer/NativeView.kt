package com.app.nativeplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings
import com.google.ads.interactivemedia.v3.internal.id
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ads.AdPlaybackState
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


internal class NativeView(
    context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger,
    mainActivity: com.app.nativeplayer.MainActivity
) : PlatformView,
    MethodChannel.MethodCallHandler {
    val videoList = listOf("https://storage.googleapis.com/gvabox/media/samples/stock.mp4", "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")
    val videoL = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
    private val playerView: PlayerView
    private var adsLoader: ImaAdsLoader? = null
    private var eventListener: AdsLoader.EventListener? = null
    var player: ExoPlayer? = null
    var contentUri: Uri? = null
    var adTagUri: Uri? = null
    private val methodChannel: MethodChannel
    override fun getView(): View {
        return playerView
    }


    override fun dispose() {
        adsLoader!!.setPlayer(null)
        playerView.player = null
        player!!.release()
        player = null
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "loadUrl" -> {
                contentUri = Uri.parse(call.arguments.toString())

            }
            "pauseVideo" -> {
                player!!.pause()
            }

            "loadpre" -> {
                var fff = call.arguments.toString()
                println("bvvvvvvvvvvv= ${fff}")

                player!!.previous()

            }

            "loadnew" -> {
                var fff = call.arguments.toString()
                println("bvvvvvvvvvvv= ${fff}")

                player!!.next()

            }
            "resumeVideo" -> {
                player!!.play()
            }
            else -> result.notImplemented()
        }
    }

    init {
        methodChannel = MethodChannel(messenger, "bms_video_player")
        methodChannel.setMethodCallHandler(this)
        playerView = PlayerView(context)
        val imaSdkSettings: ImaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings()
        imaSdkSettings.setLanguage("he")

        adsLoader = ImaAdsLoader.Builder( /* context= */context).setAdEventListener(object :
            AdEventListener {
            fun onAdError(adErrorEvent: AdErrorEvent?) {
                // Handle ad error event
            }

            override fun onAdEvent(adEvent: AdEvent) {
                if (adEvent.getType() == AdEvent.AdEventType.COMPLETED) {
                    methodChannel.invokeMethod("adCompleted", null)
                }
                if (adEvent.getType() == AdEvent.AdEventType.SKIPPED) {
                    methodChannel.invokeMethod("adSkipped", null)
                }
            }
        }).setImaSdkSettings(imaSdkSettings).build()
        if (Util.SDK_INT > 23) {
            initializePlayer(id, mainActivity, creationParams, methodChannel)
        }

    }



    private fun initializePlayer(
        id: Int,
        mainActivity: MainActivity,
        creationParams: Map<String?, Any?>?,
        methodChannel: MethodChannel
    ) {


        // Set up the factory for media sources, passing the ads loader and ad view providers.
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(view.context, Util.getUserAgent(playerView.context, "nativeplayer"))
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { unusedAdTagUri: AdsConfiguration? -> adsLoader }
            .setAdViewProvider(playerView)

        player = ExoPlayer.Builder(view.context).setMediaSourceFactory(mediaSourceFactory).build()
        player!!.preparePlayer(playerView, true, mainActivity, methodChannel)
        playerView.player = player
        adsLoader!!.setPlayer(player)
        playerView.isControllerVisible
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.showController()
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.controllerHideOnTouch = false


        // Create the MediaItem to play, specifying the content URI and ad tag URI.
//         val contentUri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4")
        val url = creationParams as Map<String?, Any?>?
        contentUri = Uri.parse(url?.get("videoURL") as String?)
        adTagUri =Uri.parse(url?.get("adURL") as String?)
//            Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=&pmad=4&video_duration=9000&vpos=preroll%2Cmidroll%2Cpostroll&preroll=1&postroll=1&pod=enabled&mridx=enabled")

        println("myddddddddd=${contentUri.toString()}")
        var adPlaybackState = AdPlaybackState(0, 500 * C.MICROS_PER_SECOND)
        adPlaybackState = adPlaybackState.withAdCount(0,4)
//        adPlaybackState = adPlaybackState.withAdUri(0, 0, adTagUri)

        eventListener?.onAdPlaybackState(adPlaybackState);


        val mediaItem = MediaItem.Builder().setUri(contentUri).build()


//        val contentStart = MediaItem.Builder().setUri(contentUri)
//            .setAdsConfiguration(
//                AdsConfiguration.Builder(adTagUri).build()
//            )                .setClipStartPositionMs(0).build()

//        player!!.addMediaItem(contentStart)
//
//        player!!.repeatMode = Player.REPEAT_MODE_ALL
//        player!!.prepare()
//
//
//        player!!.setPlayWhenReady(false)
//
//
//        // Set PlayWhenReady. If true, content and ads autoplay.
//        // Set PlayWhenReady. If true, content and ads autoplay.
//        player!!.playWhenReady = true
// A pre-roll ad.



// A pre-roll ad.
        val preRollAd =Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator=&hl=ja")
        val preRollAd_en =Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator=")

// A mid-roll ad.
// A mid-roll ad.
        val midRollAd =
            Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator=&hl=ja")
// The rest of the content
// The rest of the content


        val contentStart = MediaItem.Builder().setUri(contentUri)
            .setAdsConfiguration(
                AdsConfiguration.Builder(adTagUri!!).build()
            )                .setClipStartPositionMs(0).build()

        val contentMid: MediaItem = MediaItem.Builder()
            .setUri(contentUri).setAdsConfiguration(
                AdsConfiguration.Builder(midRollAd).build()
            )                .setClipStartPositionMs(120_000)
            .build()

        val contentEnd: MediaItem = MediaItem.Builder()
            .setUri(contentUri).setAdsConfiguration(
                AdsConfiguration.Builder(midRollAd).build()
            )                .setClipStartPositionMs(240_000)
            .build()

// Build the playlist.

// Build the playlist.
        // Build the playlist.
//        player!!.addMediaItem(mediaItem);
//        player!!.addMediaItem(contentStart);
//        player!!.addMediaItem(contentMid);
//        player!!.addMediaItem(contentEnd);

// Prepare the content and ad to be played with the SimpleExoPlayer.


        for (i in videoList.indices){
            var songPath:String = videoList.get(i)
            val item =Uri.parse(songPath)

            val contentStartmmm = MediaItem.Builder().setUri(item)
                .setAdsConfiguration(
                    AdsConfiguration.Builder(adTagUri!!).build()
                )                .setClipStartPositionMs(0).build()

            player!!.addMediaItem(contentStartmmm)
        }

//        val item =Uri.parse(videoL)
//
//        val contentStartmmm = MediaItem.Builder().setUri(item)
//            .setAdsConfiguration(
//                AdsConfiguration.Builder(adTagUri!!).build()
//            )                .setClipStartPositionMs(0).build()
//
//        player!!.addMediaItem(contentStartmmm)




// Prepare the content and ad to be played with the SimpleExoPlayer.
        player!!.prepare()
//        player!!.play()

        player!!.setPlayWhenReady(false)


        // Set PlayWhenReady. If true, content and ads autoplay.
        // Set PlayWhenReady. If true, content and ads autoplay.
        player!!.playWhenReady = true



    }


}