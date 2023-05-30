package com.example.flios

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdPlaybackState
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


internal class NativeView(context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger,
    mainActivity: MainActivity
) : PlatformView,
    MethodChannel.MethodCallHandler {
    private val videoList = listOf("https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")
    private var playerView: StyledPlayerView
    private var adsLoader: ImaAdsLoader? = null
    private var eventListener: AdsLoader.EventListener? = null
    var player: ExoPlayer? = null
    var contentUri: Uri? = null
    var adTagUri: Uri? = null
    private val methodChannel: MethodChannel

    private var view: View = LayoutInflater.from(context).inflate(R.layout.activity_video_ad, null)

    override fun getView(): View {
        return view
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
                player?.pause()
            }

            "loadpre" -> {
                val fff = call.arguments.toString()
                println("bvvvvvvvvvvv= $fff")

                player?.seekToPreviousMediaItem()

            }

            "loadnew" -> {
                val fff = call.arguments.toString()
                println("bvvvvvvvvvvv= $fff")

                player?.seekToNextMediaItem()

            }
            "resumeVideo" -> {
                player?.play()
            }
            else -> result.notImplemented()
        }
    }


    init {
        methodChannel = MethodChannel(messenger, "bms_video_player")
        methodChannel.setMethodCallHandler(this)
//        playerView = StyledPlayerView(context)

        playerView = view.findViewById(R.id.player_view)

        val imaSdkSettings: ImaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings()
        imaSdkSettings.language = "he"

        adsLoader = ImaAdsLoader.Builder(context).setImaSdkSettings(imaSdkSettings).build()
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

        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(view.context)

        val mediaSourceFactory: MediaSource.Factory = DefaultMediaSourceFactory(dataSourceFactory)
            .setLocalAdInsertionComponents(
                { unusedAdTagUri: AdsConfiguration? -> adsLoader },
                playerView
            )

        player = ExoPlayer.Builder(view.context).setMediaSourceFactory(mediaSourceFactory).build()
        playerView.player = player
        adsLoader!!.setPlayer(player)
        playerView.isControllerFullyVisible
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.showController()
        playerView.useController = true
        playerView.controllerAutoShow = true

//        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
//        playerView.controllerHideOnTouch = false

        contentUri = Uri.parse(creationParams?.get("videoURL") as String?)
        adTagUri =Uri.parse(creationParams?.get("adURL") as String?)

        println("myddddddddd=${contentUri.toString()}")
        var adPlaybackState = AdPlaybackState(0, 500 * C.MICROS_PER_SECOND)
        adPlaybackState = adPlaybackState.withAdCount(0,4)

        eventListener?.onAdPlaybackState(adPlaybackState);

        for (i in videoList.indices){
            var songPath:String = videoList.get(i)
            val item =Uri.parse(songPath)

            val contentStartmmm = MediaItem.Builder().setUri(item)
                .setAdsConfiguration(
                    AdsConfiguration.Builder(adTagUri!!).build()
                )                .setClipStartPositionMs(0).build()

            player!!.addMediaItem(contentStartmmm)
        }

        player!!.prepare()

//        player!!.playWhenReady = false

        player!!.playWhenReady = true

    }


}