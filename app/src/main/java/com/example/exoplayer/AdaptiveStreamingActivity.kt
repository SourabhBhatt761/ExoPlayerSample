package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.exoplayer.databinding.ActivityAdaptiveStreamingBinding
import com.example.exoplayer.databinding.ActivityExoPlayerBinding

class AdaptiveStreamingActivity : AppCompatActivity(), AnalyticsListener {
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = playbackStateListener()

    companion object{
        private const val TAG = "AdaptiveStreamingAct"
    }

    //new method to inflate
    //LazyThreadSafetyMode.NONE -> when we are aware that the initialization will happen in same thread
    private val viewBinding by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ActivityAdaptiveStreamingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()

        //initializing exp player
        initializePlayer()
        if ((Util.SDK_INT <= 23)) {
            initializePlayer()
        }
    }

    private fun initializePlayer() {

        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer

                //audio
//                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))

                //video
//                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))

                val mediaItem = MediaItem.Builder()
                    .setUri(getString(R.string.media_url_dash))
                    .setMimeType(MimeTypes.APPLICATION_MPD)         //DASH URI
                    .build()
                exoPlayer.setMediaItem(mediaItem)

                //A playlist can be modified by calling
                // addMediaItem(MediaItem item),
                // moveMediaItem(int fromIndex, int toIndex),
                // removeMediaItem(int index) directly on the player.
                // Playlist modifications can be performed when the player is in any state, including before the player is prepared and while media is currently playing.

                //adding second video as a playlist
//                val secondMediaItem = MediaItem.fromUri(getString(R.string.second_media_url_mp4))
//                exoPlayer.addMediaItem(secondMediaItem)



                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()

                //read somewhere-> The setPlayWhenReady(true) is required when you are playing a video from URL.
                //but without this I was able to play the video
                exoPlayer.playWhenReady = true
            }
    }


    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

        }
    }

    private fun analyticsListener() = object : AnalyticsListener{
        override fun onRenderedFirstFrame(
            eventTime: AnalyticsListener.EventTime,
            output: Any,
            renderTimeMs: Long
        ) {
            super.onRenderedFirstFrame(eventTime, output, renderTimeMs)
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long
        ) {
            super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
        }

        override fun onAudioUnderrun(
            eventTime: AnalyticsListener.EventTime,
            bufferSize: Int,
            bufferSizeMs: Long,
            elapsedSinceLastFeedMs: Long
        ) {
            super.onAudioUnderrun(eventTime, bufferSize, bufferSizeMs, elapsedSinceLastFeedMs)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }



    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    private fun hideSystemUi() {

        //hides the buttons
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}