package fr.yopox.pika

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JComponent
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

private const val RIGHT_TOOLBAR_GAP = 20

/**
 * Adds a [PikaSpriteComponent] to the layered pane of the project frame.
 * The sprite stays in the bottom right corner, above the status bar.
 * It uses [JLayeredPane.PALETTE_LAYER], so it is above the frame content and below the popups.
 */
@Service(Service.Level.PROJECT)
class PikaPetService(internal val project: Project, private val scope: CoroutineScope) : Disposable {
    private val IDLE = PikaAnimation.BLINK

    private var sprite: PikaSpriteComponent = PikaSpriteComponent()
    private var animation: PikaAnimation = IDLE
    private var animationJob: Job = Job()

    private var listener: ComponentListener? = null

    init {
        playAnimation(animation)
    }

    @RequiresEdt
    fun install() {
        if (project.isDisposed) return

        registerAnimationTriggers(this)

        val layeredPane = getWindowComponents().first ?: return
        layeredPane.add(sprite, JLayeredPane.DRAG_LAYER)
        registerComponentListeners(layeredPane)
    }

    /**
     * Plays [anim] [loops] times, then goes back to [PikaAnimation.BLINK].
     * A newer animation cancels this one and keeps its own animation.
     */
    internal fun playOneShot(anim: PikaAnimation, loops: Int = 1) {
        playAnimation(anim, loops = loops).invokeOnCompletion { cause ->
            if (cause == null) playAnimation(IDLE)
        }
    }

    internal fun playAnimation(anim: PikaAnimation, looped: Boolean = true, loops: Int = -1): Job {
        animationJob.cancel()
        animation = anim
        animationJob = sprite.playAnimation(scope, anim, looped, loops)
        return animationJob
    }

    private fun getWindowComponents(): Pair<JLayeredPane?, JComponent?> {
        val frame = WindowManager.getInstance().getFrame(project) ?: return null to null
        val layeredPane = frame.layeredPane ?: return null to null
        val statusBar = WindowManager.getInstance().getStatusBar(project)?.component
        return layeredPane to statusBar
    }

    private fun registerComponentListeners(layeredPane: JLayeredPane) {
        val (layeredPane, statusBar) = getWindowComponents()
        if (layeredPane == null) return
        val listener: ComponentListener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = relayout(layeredPane, statusBar)
            override fun componentMoved(e: ComponentEvent) = relayout(layeredPane, statusBar)
            override fun componentShown(e: ComponentEvent) = relayout(layeredPane, statusBar)
            override fun componentHidden(e: ComponentEvent) = relayout(layeredPane, statusBar)
        }
        layeredPane.addComponentListener(listener)
        statusBar?.addComponentListener(listener)
        relayout(layeredPane, statusBar)
        this.listener = listener
    }

    private fun relayout(layeredPane: JLayeredPane, statusBar: JComponent?) {
        updateBounds(sprite, layeredPane, statusBar)
    }

    override fun dispose() {
        val (layeredPane, statusBar) = getWindowComponents()
        layeredPane?.removeComponentListener(listener)
        statusBar?.removeComponentListener(listener)
        layeredPane?.remove(sprite)
        layeredPane?.repaint(sprite.bounds)
    }

    private fun updateBounds(sprite: Component, layeredPane: JLayeredPane, statusBar: Component?) {
        val size = sprite.preferredSize
        val bottom = if (statusBar != null && statusBar.isShowing) {
            SwingUtilities.convertPoint(statusBar.parent, statusBar.location, layeredPane).y
        }
        else {
            layeredPane.height
        }
        val dx = JBUI.CurrentTheme.Toolbar.stripeToolbarButtonSize().width + JBUI.scale(RIGHT_TOOLBAR_GAP)
        val dy = JBUI.insets("Island.Editor.insets", JBUI.insets(2)).height() - 1
        sprite.setBounds(layeredPane.width - size.width - dx, bottom - size.height - dy, size.width, size.height)
        layeredPane.repaint()
    }
}