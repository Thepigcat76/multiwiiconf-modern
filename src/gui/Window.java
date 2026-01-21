package gui;

import gui.tabs.DefaultTab;
import gui.tabs.SettingsTab;
import gui.tabs.Tab;
import settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;

public class Window {
    public static JFrame FRAME;

    private final Map<String, Tab> tabs;
    private final JPanel windowPanel;
    private final JTabbedPane tabsPane;
    private final Settings settings;
    private DefaultTab defaultTab;

    public Window(Settings settings) {
        this.tabs = new HashMap<>();
        this.settings = settings;

        this.windowPanel = new JPanel(new BorderLayout());
        Font font = new Font("Monospace", Font.PLAIN, 16);
        this.tabsPane = new JTabbedPane();
        this.tabsPane.setFont(font);
    }

    public Settings getSettings() {
        return settings;
    }

    public void init() {
        this.defaultTab = this.addTab(new DefaultTab(this.settings));
        this.addTab(new SettingsTab(this.settings));
    }

    private <T extends Tab> T addTab(T tab) {
        this.tabs.put(tab.name(), tab);
        tab.init();
        tab.addPanel(this.tabsPane::addTab);
        return tab;
    }

    public void addPanel(Consumer<JComponent> addFunction) {
        this.windowPanel.add(this.tabsPane);
        addFunction.accept(this.windowPanel);
    }

    public void updateConfig() {
        this.defaultTab.updateConfig();
    }
}
