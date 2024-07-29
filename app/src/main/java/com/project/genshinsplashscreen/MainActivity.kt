package com.project.genshinsplashscreen

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainView()
        }
    }
}

@Composable
fun MainView() {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenWidthDp

    val imgBitmap = ImageBitmap.imageResource(R.drawable.illustration_genshin)

    val imgHeight = height * 2.1
    val imgWidth = width * 2.1


    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayer({} , {})
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f))
            .drawWithContent {

                val canvasWidth = size.width
                val canvasHeight = size.height

                val translateX = (canvasWidth - imgWidth) / 2
                val translateY = (canvasHeight - imgHeight) / 2

                translate(
                    left= translateX.toFloat(), top = translateY.toFloat()
                ){
                    drawImage(
                        image = imgBitmap,
                        dstSize = IntSize(
                            width = imgWidth.toInt(),
                            height = imgHeight.toInt()
                        ),
                        blendMode = BlendMode.DstOut
                    )
                }
            })
    }
}

@Composable
fun VideoPlayer(
    onVideoChange: (Int) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaItem = MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/${R.raw.genshin_impact}"))

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            this.repeatMode = REPEAT_MODE_ONE
            this.setMediaItem(mediaItem)
            this.prepare()
            this.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    onVideoChange(this@apply.currentPeriodIndex)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        isVideoEnded.invoke(true)
                    }
                }
            })
        }
    }

    exoPlayer.playWhenReady = true

    LocalLifecycleOwner.current.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (exoPlayer.isPlaying.not()) {
                        exoPlayer.play()
                    }
                }
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                else -> {}
            }
        }
    })

    Column(modifier = modifier.background(Color.Black)) {
        DisposableEffect(
            key1 = null,
            key2 = ExoPlayer(Modifier, context, exoPlayer),
        ) {
            onDispose { exoPlayer.release() }
        }
    }
}

@Composable
private fun ExoPlayer(modifier: Modifier, context: Context, exoPlayer: ExoPlayer) {
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                setShowNextButton(false)
                setShowPreviousButton(false)
                setShowFastForwardButton(false)
                setShowRewindButton(false)
            }
        }
    )
}