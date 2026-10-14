package fr.yopox.pika

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

enum class PikaAnimation {
    BLINK, CELEBRATE, WAIT, WAVE, SAD, QUESTION,
}

internal fun PikaSpriteComponent.playAnimation(
    scope: CoroutineScope,
    anim: PikaAnimation,
    looped: Boolean = true,
    loops: Int = -1,
): Job {
    val animation = getAnimation(anim)
    return scope.launch {
        var index = 0
        var loopCount = 0
        while (true) {
            val frame = animation.frames[index]
            this@playAnimation.frameIndex = frame.key
            withContext<Unit>(Dispatchers.EDT) { this@playAnimation.repaint() }
            delay(frame.duration + frame.randomDuration * Math.random())
            index = (index + 1) % animation.frames.size
            if (index == 0) {
                if (!looped) break
                loopCount += 1
                if (loops != -1 && loopCount >= loops) break
            }
        }
    }
}

private data class Frame(val key: Int, val duration: Duration, val randomDuration: Duration = 0.seconds)
private data class Animation(val frames: List<Frame>)

private fun getAnimation(animations: PikaAnimation): Animation = when (animations) {
    PikaAnimation.BLINK -> Animation(listOf(
        Frame(14, 5.seconds, 10.seconds),
        Frame(15, 200.milliseconds, 200.milliseconds),
    ))
    PikaAnimation.CELEBRATE -> Animation(listOf(
        Frame(27, 400.milliseconds, 300.milliseconds),
        Frame(26, 400.milliseconds, 300.milliseconds),
    ))
    PikaAnimation.WAIT -> Animation(listOf(
        Frame(6, 600.milliseconds, 300.milliseconds),
        Frame(7, 600.milliseconds, 300.milliseconds),
    ))
    PikaAnimation.WAVE -> Animation(listOf(
        Frame(20, 600.milliseconds, 300.milliseconds),
        Frame(21, 600.milliseconds, 300.milliseconds),
    ))
    PikaAnimation.SAD -> Animation(listOf(
        Frame(46, 2000.milliseconds, 500.milliseconds),
    ))
    PikaAnimation.QUESTION -> Animation(listOf(
        Frame(61, 600.milliseconds, 300.milliseconds),
        Frame(62, 600.milliseconds, 300.milliseconds),
    ))
}