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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
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

    public void decorateError(@NotNull Collection<String> errors) {
        if (errors.isEmpty()) {
            Classes.classRemove(source, CLASS_DANGER);
            content.setToolTipText(null);
        } else {
            Classes.classAdd(source, CLASS_DANGER);
            content.setToolTipText(String.join("\n", errors));
        }
    }

    @NotNull
    public List<String> getScript() {
        return Arrays.stream(getText().split("\n"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private String getText() {
        return content.getText();
    }

    private void setText(String text) {
        content.setText(text);
    }

    public void setScript(@NotNull Collection<String> script) {
        String text = script.stream().map(String::trim).collect(Collectors.joining("\n"));
        if (!Objects.equals(getText(), text)) {
            setText(text);
            scrollDown();
        }
    }

    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        SwingUtilities.invokeLater(() -> {
            content = new JTextPane();
            content.setFont(FONT);
            content.setEditorKitForContentType(MIMETYPE, new RPyCGEditorKit(PATTERN_COLORS));
            content.setContentType(MIMETYPE);
            scrollPane = new JScrollPane(this.content,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            source.setContent(scrollPane);
            this.content.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    Creator.this.content.repaint();
                    scrollPane.repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    String text = getText();
                    if (!Objects.equals(text, getPrevText())) {
                        setPrevText(text);
                        changed.setValue(true);
                    }
                }
            });
        });
    }

    private String getPrevText() {
        return prevText.getValue();
    }

    private void setPrevText(String prevText) {
        this.prevText.setValue(prevText);
    }

    public boolean isChanged() {
        return changed.getValue();
    }

    public void setChanged(boolean changed) {
        this.changed.setValue(changed);
    }

    public void onShow() {
        source.requestFocus();
    }

    private SimpleStringProperty prevTextProperty() {
        return prevText;
    }

    public void setScriptUnforced(@NotNull Collection<String> script) {
        if (!source.isFocused()) {
            setScript(script);
        }
    }

    private void scrollDown() {
        Creator.this.content.setCaretPosition(Creator.this.content.getDocument().getLength());
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    private static class RPyCGEditorKit extends StyledEditorKit {
        private static final long serialVersionUID = 2969169649596107757L;
        private final ViewFactory viewFactory;

        public RPyCGEditorKit(Map<Pattern, Color> patternColors) {
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
    }

    private static class RPyCGView extends PlainView {
        private final Map<Pattern, Color> patternColors;

        public RPyCGView(Element element, Map<Pattern, Color> patternColors) {
            super(element);
            getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
            this.patternColors = patternColors;
        }

        @Override
        protected float drawUnselectedText(Graphics2D graphics, float x, float y, int p0, int p1)
                throws BadLocationException {
            Document doc = getDocument();
            String text = doc.getText(p0, p1 - p0);
            Segment segment = getLineBuffer();
            SortedMap<Integer, Integer> startMap = new TreeMap<>();
            SortedMap<Integer, Color> colorMap = new TreeMap<>();
            for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {
                Matcher matcher = entry.getKey().matcher(text);
                while (matcher.find()) {
                    startMap.put(matcher.start(1), matcher.end());
                    colorMap.put(matcher.start(1), entry.getValue());
                }
            }
            // TODO: check the map for overlapping parts
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
                int start = entry.getKey();
                int end = entry.getValue();
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

    private static class RPyCGViewFactory implements ViewFactory {

        private final Map<Pattern, Color> patternColors;

        private RPyCGViewFactory(Map<Pattern, Color> patternColors) {
            this.patternColors = patternColors;
        }

        @Override
        public View create(Element element) {
            return new RPyCGView(element, patternColors);
        }
    }
}
