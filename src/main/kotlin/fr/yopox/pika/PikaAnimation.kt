package fr.yopox.pika

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

enum class PikaAnimation {
    BLINK
}

internal fun PikaSpriteComponent.playAnimation(scope: CoroutineScope, anim: PikaAnimation): Job {
    return getAnimation(anim).play(scope, this)
}

private data class Frame(val key: Int, val duration: Duration, val randomDuration: Duration = 0.seconds)
private data class Animation(val frames: List<Frame>, val loop: Boolean)

private fun Animation.play(scope: CoroutineScope, component: PikaSpriteComponent): Job = scope.launch {
    var index = 0
    while (true) {
        val frame = frames[index]
        component.frameIndex = frame.key
        withContext(Dispatchers.EDT) { component.repaint() }
        delay(frame.duration + frame.randomDuration * Math.random())
        index = (index + 1) % frames.size
        if (!loop && index == 0) break
    }
}

private val BLINK = Animation(listOf(
    Frame(14, 5.seconds, 10.seconds),
    Frame(15, 200.milliseconds, 200.milliseconds)
), true)

private fun getAnimation(animations: PikaAnimation): Animation = when (animations) {
    PikaAnimation.BLINK -> BLINK
}