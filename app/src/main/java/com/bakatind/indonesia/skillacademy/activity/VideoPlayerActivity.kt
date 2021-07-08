package com.bakatind.indonesia.skillacademy.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import com.bakatind.indonesia.skillacademy.R
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.VIDEO_URL
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.YOUTUBE_API_KEY
import kotlinx.android.synthetic.main.activity_video_player.*

class VideoPlayerActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    private var url: String? = null
    private var mHandler: Handler? = null
    private var mPlayer: YouTubePlayer? = null

    private var mVideoSeekBarChangeListener: OnSeekBarChangeListener =
        object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val lengthPlayed: Long = ((seekBar.progress).times(100)).toLong()
                lengthPlayed.toInt().let { mPlayer?.seekToMillis(it) }
            }
        }

    private var mPlaybackEventListener: PlaybackEventListener = object : PlaybackEventListener {
        override fun onBuffering(arg0: Boolean) {}
        override fun onPaused() {
            mHandler?.removeCallbacks(runnable)
            play_video.setImageResource(R.drawable.ic_play)
        }

        override fun onPlaying() {
            mHandler?.postDelayed(runnable, 100)
            displayCurrentTime()
            play_video.setImageResource(R.drawable.ic_pause)
        }

        override fun onSeekTo(arg0: Int) {
            mHandler?.postDelayed(runnable, 100)
        }

        override fun onStopped() {
            mHandler?.removeCallbacks(runnable)
        }
    }


    private var mPlayerStateChangeListener: PlayerStateChangeListener =
        object : PlayerStateChangeListener {
            override fun onAdStarted() {}
            override fun onError(arg0: YouTubePlayer.ErrorReason?) {}
            override fun onLoaded(arg0: String) {}
            override fun onLoading() {}
            override fun onVideoEnded() {}
            override fun onVideoStarted() {
                seekBar.max = mPlayer?.durationMillis!! / 100
                displayCurrentTime()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        init()
    }

    private fun init() {
        mHandler = Handler(Looper.getMainLooper())
        url = intent?.getStringExtra(VIDEO_URL).toString()
        youtube_view.initialize(YOUTUBE_API_KEY, this)
        seekBar.setOnSeekBarChangeListener(mVideoSeekBarChangeListener)
        play_video.setOnClickListener {
            if (!mPlayer?.isPlaying!!) {
                mPlayer?.play()
            } else {
                mPlayer?.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        mPlayer = player

        displayCurrentTime()

        if (!wasRestored) {
            val urlBuilder = url?.replace("https://youtu.be/", "")
            mPlayer?.loadVideo(urlBuilder)
        }

        video_control.visibility = VISIBLE

        mPlayer?.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
        mPlayer?.setPlaybackEventListener(mPlaybackEventListener)
        mPlayer?.setPlayerStateChangeListener(mPlayerStateChangeListener)
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show()
        } else {
            @SuppressLint("StringFormatInvalid", "LocalSuppress") val error =
                String.format(getString(R.string.player_error), errorReason.toString())
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        runOnUiThread {
            if (requestCode == RECOVERY_REQUEST) {
                youTubePlayerProvider?.initialize(YOUTUBE_API_KEY, this)
            } else {
                finish()
            }
        }
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            displayCurrentTime()
            mHandler?.postDelayed(this, 1000)
        }
    }

    private fun displayCurrentTime() {
        try {
            val formattedTime = mPlayer?.currentTimeMillis?.let {
                mPlayer?.durationMillis?.minus(it)?.let { it1 ->
                    formatTime(
                        it1
                    )
                }
            }
            play_time.text = formattedTime

            seekBar.progress = mPlayer?.currentTimeMillis?.div(100) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatTime(millis: Int): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return (if (hours == 0) "--:" else "$hours:") + String.format(
            "%02d:%02d",
            minutes % 60,
            seconds % 60
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.release()
    }

    private val youTubePlayerProvider: YouTubePlayer.Provider?
        get() = youtube_view

    companion object {
        private const val RECOVERY_REQUEST = 1
    }
}