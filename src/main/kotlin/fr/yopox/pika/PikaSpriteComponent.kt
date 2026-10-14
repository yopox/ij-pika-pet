package fr.yopox.pika

import com.intellij.openapi.diagnostic.logger
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent

private const val SPRITE_SIZE = 40
private const val ZOOM = 2
private const val SPRITESHEET_PATH = "/assets/pika.png"

private val LOG = logger<PikaSpriteComponent>()

internal class PikaSpriteComponent(var frameIndex: Int = 14) : JComponent() {
    private val spritesheet: BufferedImage? = loadSpritesheet()

    init {
        isOpaque = false
    }

    override fun getPreferredSize(): Dimension = Dimension(SPRITE_SIZE * ZOOM, SPRITE_SIZE * ZOOM)

    override fun contains(x: Int, y: Int): Boolean = false

    override fun paintComponent(g: Graphics) {
        val sheet = spritesheet ?: return
        val frameCount = sheet.width / SPRITE_SIZE
        if (frameCount == 0) return
        val sourceX = (frameIndex % 8) * SPRITE_SIZE
        val sourceY = (frameIndex / 8) * SPRITE_SIZE

        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
            g2.drawImage(sheet,
                0, 0, SPRITE_SIZE * ZOOM, SPRITE_SIZE * ZOOM,
                sourceX, sourceY, sourceX + SPRITE_SIZE, sourceY + SPRITE_SIZE,
                null)
        }
        finally {
            g2.dispose()
        }
    }
}

private fun loadSpritesheet(): BufferedImage? {
    val stream = PikaSpriteComponent::class.java.getResourceAsStream(SPRITESHEET_PATH)
    if (stream == null) {
        LOG.warn("Cannot find the spritesheet $SPRITESHEET_PATH")
        return null
    }
    return stream.use { ImageIO.read(it) }
}
