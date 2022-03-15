package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.rpycg.util.Classes.classAdd
import gargoyle.rpycg.util.Classes.classRemove
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingNode
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ScrollPane
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.net.URL
import java.util.ResourceBundle
import java.util.TreeMap
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.text.Element
import javax.swing.text.PlainDocument
import javax.swing.text.PlainView
import javax.swing.text.StyledEditorKit
import javax.swing.text.Utilities
import javax.swing.text.View
import javax.swing.text.ViewFactory

class Creator : ScrollPane(), Initializable {
    private val changed = SimpleBooleanProperty(false)
    private val prevText = SimpleStringProperty("")
    private lateinit var content: JTextPane
    private lateinit var scrollPane: JScrollPane

    @FXML
    private lateinit var source: SwingNode

    init {
        FxContext.current.loadComponent(this) ?: error("No view {Creator}")
    }

    fun changedProperty(): SimpleBooleanProperty =
        changed

    fun decorateError(errors: Collection<String?>) {
        if (errors.isEmpty()) {
            classRemove(source, CLASS_DANGER)
            content.toolTipText = null
        } else {
            classAdd(source, CLASS_DANGER)
            content.toolTipText = errors.joinToString("\n")
        }
    }

    var script: List<String>
        get() = text.split("\n").map { it.trim() }.toList()
        set(value) {
            val script = value.joinToString("\n") { it.trim() }
            if (text != script) {
                text = script
                scrollDown()
            }
        }

    override fun initialize(location: URL, resources: ResourceBundle) {
        SwingUtilities.invokeLater {
            content = JTextPane()
            content.font = FONT
            content.setEditorKitForContentType(MIMETYPE, RPyCGEditorKit(PATTERN_COLORS))
            content.contentType = MIMETYPE
            scrollPane = JScrollPane(
                content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            )
            source.content = scrollPane
            this.content.addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent) {
                    content.repaint()
                    scrollPane.repaint()
                }

                override fun focusLost(e: FocusEvent) {
                    if (text != getPrevText()) {
                        setPrevText(text)
                        changed.value = true
                    }
                }
            })
        }
    }

    fun isChanged(): Boolean =
        changed.value

    fun setChanged(changed: Boolean) {
        this.changed.value = changed
    }

    fun onShow(): Unit =
        source.requestFocus()

    fun setScriptUnforced(script: List<String>) {
        if (!source.isFocused) this.script = script
    }

    private fun getPrevText(): String =
        prevText.value

    private fun setPrevText(prevText: String) {
        this.prevText.value = prevText
    }

    private var text: String
        get() = content.text
        private set(text) {
            content.text = text
        }

    fun prevTextProperty(): SimpleStringProperty =
        prevText

    private fun scrollDown() {
        content.caretPosition = content.document.length
        val scrollBar = scrollPane.verticalScrollBar
        scrollBar.value = scrollBar.maximum
    }

    private class RPyCGEditorKit(patternColors: Map<Regex, Color>) : StyledEditorKit() {
        private val viewFactory: ViewFactory = RPyCGViewFactory(patternColors)

        override fun getContentType(): String =
            MIMETYPE

        override fun getViewFactory(): ViewFactory =
            viewFactory
    }

    private class RPyCGView(element: Element, private val patternColors: Map<Regex, Color>) : PlainView(element) {
        init {
            document.putProperty(PlainDocument.tabSizeAttribute, 4)
        }

        override fun drawUnselectedText(graphics: Graphics2D, x: Float, y: Float, p0: Int, p1: Int): Float {
            var xx = x
            val doc = document
            val text = doc.getText(p0, p1 - p0)
            val segment = lineBuffer
            val startMap = TreeMap<Int, Int>()
            val colorMap = TreeMap<Int, Color>()
            for ((key, value) in patternColors) {
                val findAll: Sequence<MatchResult> = key.findAll(text)
                for (matcher in findAll) {
                    startMap[matcher.range.first] = matcher.range.last + 1
                    colorMap[matcher.range.first] = value
                }
            }
            // TODO: check the map for overlapping parts
            var i = 0
            for ((start, end) in startMap) {
                if (i < start) {
                    graphics.color = Color.BLACK
                    doc.getText(p0 + i, start - i, segment)
                    xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
                }
                graphics.color = colorMap[start]
                i = end
                doc.getText(p0 + start, i - start, segment)
                xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, start)
            }
            // Paint possible remaining text black
            if (i < text.length) {
                graphics.color = Color.BLACK
                doc.getText(p0 + i, text.length - i, segment)
                xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
            }
            return xx
        }
    }

    private class RPyCGViewFactory(private val patternColors: Map<Regex, Color>) : ViewFactory {
        override fun create(element: Element): View = RPyCGView(element, patternColors)
    }

    companion object {
        val PATTERN_COLORS: Map<Regex, Color> = mapOf(
            ("([<>])").toRegex() to Color(0x808000),
            ("<([^;]+)").toRegex() to Color(0x008000),
            (";(.*)").toRegex() to Color(0x000080),
            ("(\\([a-zA-Z]+\\))").toRegex() to Color(0x800080),
            ("=([^(\"]+)").toRegex() to Color(0x800000)
        )
        private const val CLASS_DANGER = "danger"
        private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
        private const val MIMETYPE = "text/rpycg"
    }
}
