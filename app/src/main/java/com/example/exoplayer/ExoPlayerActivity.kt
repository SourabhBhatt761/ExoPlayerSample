package com.example.exoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityExoPlayerBinding

class ExoPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L


    //new method to inflate
    //LazyThreadSafetyMode.NONE -> when we are aware that the initialization will happen in same thread
    private val viewBinding by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ActivityExoPlayerBinding.inflate(layoutInflater)
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

        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer

                //audio
//                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))

                //video
                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
                exoPlayer.setMediaItem(mediaItem)

                //A playlist can be modified by calling
                // addMediaItem(MediaItem item),
                // moveMediaItem(int fromIndex, int toIndex),
                // removeMediaItem(int index) directly on the player.
                // Playlist modifications can be performed when the player is in any state, including before the player is prepared and while media is currently playing.

                //adding second video as a playlist
                val secondMediaItem = MediaItem.fromUri(getString(R.string.second_media_url_mp4))
                exoPlayer.addMediaItem(secondMediaItem)



                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.prepare()

                //read somewhere-> The setPlayWhenReady(true) is required when you are playing a video from URL.
                //but without this I was able to play the video
                exoPlayer.playWhenReady
            }
    }

    private fun hideSystemUi() {

        //hides the buttons
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
            exoPlayer.release()
        }
        player = null
    }
}