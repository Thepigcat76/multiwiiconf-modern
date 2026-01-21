package gui.tabs;

import settings.Settings;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;

public class SettingsTab implements Tab {
    public static final String[] INFO_TEXTS = new String[]{
            "Green Values Can Be Changed Press Write To Save",
            "Gray Values Are Set Using #define In Config.h"
    };
    private static final String NAME = "Settings";
    private final JList<Integer> baudRates;
    private final Settings settings;

    private JPanel body;

    public SettingsTab(Settings settings) {
        this.settings = settings;
        this.body = new JPanel();
        this.baudRates = new JList<>(Settings.BAUD_RATES);
        this.baudRates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void init() {
        this.baudRates.setSelectedIndex(0);
        this.baudRates.addListSelectionListener(this::onBaudRateSelected);
        JButton resetButton = new JButton("Reset Settings to Default");
        resetButton.addActionListener(this::onResetButtonPressed);

        this.body.add(this.baudRates);
        this.body.add(resetButton);
    }

    private void onBaudRateSelected(ListSelectionEvent event) {
        this.settings.setSerialPort(this.baudRates.getSelectedValue());
    }

    private void onResetButtonPressed(ActionEvent event) {
        this.settings.init();
        this.baudRates.setSelectedIndex(0);
    }

    @Override
    public void addPanel(BiConsumer<String, JComponent> addPanelFunction) {
        addPanelFunction.accept(NAME, body);
    }

}
