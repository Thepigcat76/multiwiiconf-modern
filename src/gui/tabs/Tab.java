package gui.tabs;

import javax.swing.*;
import java.util.function.BiConsumer;

public interface Tab {
    String name();

    void init();

    void addPanel(BiConsumer<String, JComponent> addPanelFunction);

}
