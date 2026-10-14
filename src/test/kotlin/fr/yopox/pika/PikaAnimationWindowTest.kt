package fr.yopox.pika

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.*
import org.junit.Assume
import org.junit.Test
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.GridBagLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Opens a plain Swing window containing a [PikaSpriteComponent] and plays one animation, so it can be checked visually.
 */
class PikaAnimationWindowTest {
    @Test
    fun playAnimationInWindow(): Unit = runBlocking {
        Assume.assumeFalse("Needs a display", GraphicsEnvironment.isHeadless())

        val animation = PikaAnimation.WAVE
        val duration = 20.seconds

        val sprite = PikaSpriteComponent()
        lateinit var frame: JFrame
        SwingUtilities.invokeAndWait {
            frame = JFrame("Pika Pet – $animation").apply {
                defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
                // GridBagLayout centers the sprite at its preferred size
                contentPane = JPanel(GridBagLayout()).apply {
                    background = Color.WHITE
                    add(sprite)
                }
                setSize(300, 300)
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                isVisible = true
                toFront()
            }
        }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val job = sprite.playAnimation(scope, animation)
            // Play until the duration elapses, the animation ends or the window is closed
            withTimeoutOrNull(duration) {
                while (frame.isDisplayable && !job.isCompleted) delay(50.milliseconds)
            }
        }
        finally {
            scope.cancel()
            withContext(Dispatchers.EDT) {
                frame.dispose()
            }
        }
    }
}
