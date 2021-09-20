package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.util.Classes;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Creator extends ScrollPane implements Initializable {
    public static final Map<Pattern, Color> PATTERN_COLORS = Map.of(
            Pattern.compile("([<>])"), new Color(0x808000),
            Pattern.compile("<([^;]+)"), new Color(0x008000),
            Pattern.compile(";(.*)"), new Color(0x000080),
            Pattern.compile("(\\([a-zA-Z]+\\))"), new Color(0x800080),
            Pattern.compile("=([^(\"]+)"), new Color(0x800000)
    );
    private static final String CLASS_DANGER = "danger";
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final String MIMETYPE = "text/rpycg";
    private final SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
    private final SimpleStringProperty prevText = new SimpleStringProperty("");
    private JTextPane content;
    private JScrollPane scrollPane;
    @FXML
    private SwingNode source;

    public Creator() {
        FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, Creator.class.getName()));
    }

    public SimpleBooleanProperty changedProperty() {
        return changed;
    }

    public void decorateError(final Collection<String> errors) {
        if (errors.isEmpty()) {
            Classes.classRemove(source, CLASS_DANGER);
            content.setToolTipText(null);
        } else {
            Classes.classAdd(source, CLASS_DANGER);
            content.setToolTipText(String.join("\n", errors));
        }
    }

    public List<String> getScript() {
        return Arrays.stream(getText().split("\n"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public void setScript(final Collection<String> script) {
        final String text = script.stream().map(String::trim).collect(Collectors.joining("\n"));
        if (!Objects.equals(getText(), text)) {
            setText(text);
            scrollDown();
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        SwingUtilities.invokeLater(() -> {
            content = new JTextPane();
            content.setFont(FONT);
            content.setEditorKitForContentType(MIMETYPE, new RPyCGEditorKit(PATTERN_COLORS));
            content.setContentType(MIMETYPE);
            scrollPane = new JScrollPane(this.content,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            source.setContent(scrollPane);
            this.content.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(final FocusEvent e) {
                    Creator.this.content.repaint();
                    scrollPane.repaint();
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    final String text = getText();
                    if (!Objects.equals(text, getPrevText())) {
                        setPrevText(text);
                        changed.setValue(true);
                    }
                }
            });
        });
    }

    public boolean isChanged() {
        return changed.getValue();
    }

    public void setChanged(final boolean changed) {
        this.changed.setValue(changed);
    }

    public void onShow() {
        source.requestFocus();
    }

    public void setScriptUnforced(final Collection<String> script) {
        if (!source.isFocused()) {
            setScript(script);
        }
    }

    private String getPrevText() {
        return prevText.getValue();
    }

    private void setPrevText(final String prevText) {
        this.prevText.setValue(prevText);
    }

    private String getText() {
        return content.getText();
    }

    private void setText(final String text) {
        content.setText(text);
    }

    private SimpleStringProperty prevTextProperty() {
        return prevText;
    }

    private void scrollDown() {
        Creator.this.content.setCaretPosition(Creator.this.content.getDocument().getLength());
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    private static final class RPyCGEditorKit extends StyledEditorKit {
        private static final long serialVersionUID = 2969169649596107757L;
        private final ViewFactory viewFactory;

        public RPyCGEditorKit(final Map<Pattern, Color> patternColors) {
            viewFactory = new RPyCGViewFactory(patternColors);
        }

        @Override
        public String getContentType() {
            return MIMETYPE;
        }

        @Override
        public ViewFactory getViewFactory() {
            return viewFactory;
        }

        @Override
        public Object clone() {
            throw new IllegalStateException();
        }
    }

    private static final class RPyCGView extends PlainView {
        private final Map<Pattern, Color> patternColors;

        public RPyCGView(final Element element, final Map<Pattern, Color> patternColors) {
            super(element);
            getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
            this.patternColors = patternColors;
        }

        @Override
        protected float drawUnselectedText(final Graphics2D graphics, float x, final float y, final int p0, final int p1)
                throws BadLocationException {
            final Document doc = getDocument();
            final String text = doc.getText(p0, p1 - p0);
            final Segment segment = getLineBuffer();
            final SortedMap<Integer, Integer> startMap = new TreeMap<>();
            final SortedMap<Integer, Color> colorMap = new TreeMap<>();
            for (final Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {
                final Matcher matcher = entry.getKey().matcher(text);
                while (matcher.find()) {
                    startMap.put(matcher.start(1), matcher.end());
                    colorMap.put(matcher.start(1), entry.getValue());
                }
            }
            // TODO: check the map for overlapping parts
            int i = 0;
            for (final Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
                final int start = entry.getKey();
                final int end = entry.getValue();
                if (i < start) {
                    graphics.setColor(Color.BLACK);
                    doc.getText(p0 + i, start - i, segment);
                    x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
                }
                graphics.setColor(colorMap.get(start));
                i = end;
                doc.getText(p0 + start, i - start, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
            }
            // Paint possible remaining text black
            if (i < text.length()) {
                graphics.setColor(Color.BLACK);
                doc.getText(p0 + i, text.length() - i, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
            }
            return x;
        }
    }

    private static final class RPyCGViewFactory implements ViewFactory {
        private final Map<Pattern, Color> patternColors;

        private RPyCGViewFactory(final Map<Pattern, Color> patternColors) {
            this.patternColors = patternColors;
        }

        @Override
        public View create(final Element element) {
            return new RPyCGView(element, patternColors);
        }
    }
}
