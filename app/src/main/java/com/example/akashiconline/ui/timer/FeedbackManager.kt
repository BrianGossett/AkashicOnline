package com.example.akashiconline.ui.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.akashiconline.R

class FeedbackManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundWork = soundPool.load(context, R.raw.sound_work, 1)
    private val soundRest = soundPool.load(context, R.raw.sound_rest, 1)
    private val soundComplete = soundPool.load(context, R.raw.sound_complete, 1)

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun playWork() {
        soundPool.play(soundWork, 1f, 1f, 0, 0, 1f)
        vibrate()
    }

    fun playRest() {
        soundPool.play(soundRest, 1f, 1f, 0, 0, 1f)
        vibrate()
    }

    fun playComplete() {
        soundPool.play(soundComplete, 1f, 1f, 0, 0, 1f)
        vibrate()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    fun release() = soundPool.release()
}
