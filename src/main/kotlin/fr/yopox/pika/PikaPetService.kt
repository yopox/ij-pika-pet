package fr.yopox.pika

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

private const val RIGHT_TOOLBAR_GAP = 20

/**
 * Adds a [PikaSpriteComponent] to the layered pane of the project frame.
 * The sprite stays in the bottom right corner, above the status bar.
 * It uses [JLayeredPane.PALETTE_LAYER], so it is above the frame content and below the popups.
 */
@Service(Service.Level.PROJECT)
internal class PikaPetService(private val project: Project) : Disposable {
    private var uninstall: (() -> Unit)? = null

    @RequiresEdt
    fun install() {
        if (uninstall != null || project.isDisposed) return
        val frame = WindowManager.getInstance().getFrame(project) ?: return
        val layeredPane = frame.layeredPane ?: return
        val statusBar = WindowManager.getInstance().getStatusBar(project)?.component

        val sprite = PikaSpriteComponent()
        layeredPane.add(sprite, JLayeredPane.DRAG_LAYER)

        val relayout = { updateBounds(sprite, layeredPane, statusBar) }
        val listener: ComponentListener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = relayout()
            override fun componentMoved(e: ComponentEvent) = relayout()
            override fun componentShown(e: ComponentEvent) = relayout()
            override fun componentHidden(e: ComponentEvent) = relayout()
        }
        layeredPane.addComponentListener(listener)
        statusBar?.addComponentListener(listener)
        relayout()

        uninstall = {
            layeredPane.removeComponentListener(listener)
            statusBar?.removeComponentListener(listener)
            layeredPane.remove(sprite)
            layeredPane.repaint(sprite.bounds)
        }
    }

    override fun dispose() {
        uninstall?.invoke()
        uninstall = null
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
