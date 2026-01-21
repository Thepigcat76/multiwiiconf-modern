package gui.tabs;

import gui.Window;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import settings.Settings;
import utils.PIDTable;
import utils.SerialPortListener;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import sun.swing.table.DefaultTableCellHeaderRenderer;

public class DefaultTab implements Tab {
    private static final String NAME = "Default";
    private final JToggleButton exampleToggleButton;
    private final Settings settings;
    private JPanel body;
    private final DefaultListModel<String> commPorts;
    private final Timer serialCommPortRefreshListener;
    private PIDTable pidTable;
    private Object[][] pidTableData;
    private JTable pidTableComp;

    public DefaultTab(Settings settings) {
        this.settings = settings;
        this.body = new JPanel(new BorderLayout());
        this.exampleToggleButton = new JToggleButton("Motors: On");
        this.exampleToggleButton.addActionListener(_ -> this.exampleToggleButton.setText(!this.exampleToggleButton.isSelected() ? "Motors: On" : "Motors: Off"));
        this.commPorts = new DefaultListModel<>();
        this.serialCommPortRefreshListener = new Timer(100, this::refreshSerialCommPorts);
        this.pidTable = new PIDTable();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void init() {
        JPanel leftPanel = this.createLeftPanel();
        Component center0Panel = this.createCenter0Panel();
        JPanel center1Panel = this.createCenter1Panel();

        MultiSplitLayout.Node topLayout = MultiSplitLayout.parseModel(
                "(ROW (LEAF name=left weight=0.2) (LEAF name=center0 weight=0.4) (LEAF name=center1 weight=0.2) (LEAF name=right weight=0.2))"
        );

        JXMultiSplitPane topMsp = new JXMultiSplitPane();
        topMsp.getMultiSplitLayout().setModel(topLayout);

        topMsp.add(leftPanel, "left");
        topMsp.add(center0Panel, "center0");
        topMsp.add(center1Panel, "center1");
        topMsp.add(new JButton("Right Panel"), "right");

        MultiSplitLayout.Node bottomLayout = MultiSplitLayout.parseModel(
                "(COLUMN (LEAF name=top weight=0.5) (LEAF name=bottom weight=0.5))"
        );

        JXMultiSplitPane bottomMsp = new JXMultiSplitPane();
        bottomMsp.getMultiSplitLayout().setModel(bottomLayout);

        bottomMsp.add(topMsp, "top");
        bottomMsp.add(new JButton("BOTTOM"), "bottom");

        this.body.add(bottomMsp, BorderLayout.CENTER);

    }

    private JPanel createCenter1Panel() {
        JPanel panel = withBoxLayout(JPanel::new, Axis.VERTICAL);
        addSlider(panel, "Throt");
        addSlider(panel, "Roll");
        addSlider(panel, "Pitch");
        addSlider(panel, "Yaw");
        addSlider(panel, "Aux1");
        addSlider(panel, "Aux2");
        addSlider(panel, "Aux3");
        addSlider(panel, "Aux4");
        return panel;
    }

    private static final String PID = " PID";
    public static final String[] PIDS = {"ROLL", "PITCH", "YAW", "ALT", "Pos", "PosR", "NavR", "LEVEL", "MAG", "MID", "EXPO", "RATE", "EXPO"};

    private Component createCenter0Panel() {
        // Correct the table data to match the columns
        this.pidTableData = new Object[PIDS.length][3];
        for (int y = 0; y < PIDS.length; y++) {
            for (int x = 0; x < 3; x++) {
                this.pidTableData[y][x] = 0;
            }
        }

        // Column names should match the inner data length
        String[] columnNames = {"P", "I", "D"}; // 4 columns!

        // Create JTable with data and column names
        this.pidTableComp = new JTable(this.pidTableData, columnNames);
        this.pidTableComp.setRowHeight(40); // Set uniform row height
        this.pidTableComp.setEnabled(false); // Make the table non-editable
        this.pidTableComp.setFocusable(false);

        // Add row names using a row header JTable
        // (Assumes 3 rows in the data array)

        JTable rowHeader = new JTable(PIDS.length, 1);
        rowHeader.setDefaultRenderer(Object.class, new DefaultTableCellHeaderRenderer());
        rowHeader.setRowHeight(this.pidTableComp.getRowHeight()); // Match row heights
        for (int y = 0; y < PIDS.length; y++) {
            rowHeader.setValueAt(PIDS[y], y, 0);
        }
        rowHeader.setEnabled(false); // Make row header non-editable
        rowHeader.setPreferredScrollableViewportSize(new Dimension(60, 0)); // Fixed width
        rowHeader.setTableHeader(null); // Remove table header

        // Attach the row header to the scroll pane
        JScrollPane scrollPane = new JScrollPane(this.pidTableComp);
        scrollPane.setRowHeaderView(rowHeader);

        return scrollPane;
    }

    private JPanel createLeftPanel() {
        JButton configLoadButton = new JButton("Load Config");
        configLoadButton.addActionListener(this::onConfigLoadButtonPressed);
        JButton configSaveButton = new JButton("Save Config");
        configSaveButton.addActionListener(this::onConfigSaveButtonPressed);
        this.serialCommPortRefreshListener.restart();

        JPanel leftPanel = withBoxLayout(JPanel::new, Axis.VERTICAL);

        //leftPanel.add(configButtons);
        /*
        JPanel configButtons = new JPanel(new GridBagLayout());
        configButtons.setAlignmentX(Component.LEFT_ALIGNMENT); // important if inside BoxLayout

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0); // optional padding

// Left button
        gbc.gridx = 0;
        gbc.weightx = 0.5; // shares width evenly
        gbc.anchor = GridBagConstraints.WEST;
        configButtons.add(configSaveButton, gbc);

// Right button
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.CENTER;
        configButtons.add(configLoadButton, gbc);
        leftPanel.add(configButtons);
         */

        addLabel(leftPanel, "Serial Comm Port", true);
        JPanel serialPort = withBoxLayout(JPanel::new, Axis.VERTICAL);
        addLabel(serialPort, "Selected Port: (null)", false);
        serialPort.add(new JScrollPane(new JList<>(this.commPorts)));
        leftPanel.add(serialPort);
        return leftPanel;
    }

    private static JLabel addLabel(JPanel panel, String label, boolean headline) {
        Font font = new Font("Monospace", Font.PLAIN, 16);
        Box hBox = Box.createHorizontalBox();
        JLabel labelComp = new JLabel(label);
        if (headline) {
            labelComp.setFont(font);
        }
        hBox.add(labelComp);
        hBox.add(Box.createHorizontalGlue()); // pushes content left
        panel.add(hBox);
        return labelComp;
    }

    private static void addSlider(Container container, String text) {
        JPanel panel = withBoxLayout(JPanel::new, Axis.HORIZONTAL);
        JSlider slider = new JSlider();
        panel.add(slider);
        panel.add(new JLabel(text));
        JLabel label = new JLabel(String.valueOf(slider.getValue()));
        slider.addChangeListener(_ -> {
            label.setText(String.valueOf(slider.getValue()));
        });
        container.add(label);
        container.add(panel);
    }

    private static <T extends Container> T withBoxLayout(Supplier<T> panelConstructor, Axis axis) {
        T panel = panelConstructor.get();
        panel.setLayout(new BoxLayout(panel, axis == Axis.HORIZONTAL ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
        return panel;
    }

    private void onConfigLoadButtonPressed(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to open");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Multiwii configuration file (*.mwi)", "mwi");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(Window.FRAME);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(Window.FRAME,
                    "You selected: " + selectedFile.getAbsolutePath());
            try {
                System.out.println(Files.readString(Path.of(selectedFile.getAbsolutePath())));
            } catch (IOException e) {
                System.err.println("Failed to open file: " + e.getMessage());
            }
        }

    }

    private void onConfigSaveButtonPressed(ActionEvent actionEvent) {

    }

    @Override
    public void addPanel(BiConsumer<String, JComponent> addPanelFunction) {
        addPanelFunction.accept(NAME, body);
    }

    private void refreshSerialCommPorts(ActionEvent event) {
        String[] commPorts = SerialPortListener.refreshPorts(this.settings);
        if (!Arrays.equals(commPorts, this.settings.getSerialCommPorts())) {
            this.settings.setSerialCommPorts(commPorts);
            this.commPorts.clear();
            this.commPorts.addAll(Arrays.asList(this.settings.getSerialCommPorts()));
        }
    }

    public void updateConfig() {
        this.pidTable = PIDTable.fromConfig(this.settings.getConfig());
        java.util.List<PIDTable.PIDOption> options = this.pidTable.options();
        for (int i = 0; i < options.size(); i++) {
            PIDTable.PIDOption option = options.get(i);
            this.pidTableData[i][0] = option.getP();
            this.pidTableData[i][1] = option.getI();
            this.pidTableData[i][2] = option.getD();
        }
        this.pidTableData[9][0] = this.pidTable.getThrottleMid();
        this.pidTableData[10][0] = this.pidTable.getThrottleExpo();
        this.pidTableData[11][0] = this.pidTable.getRate();
        this.pidTableData[12][0] = this.pidTable.getExpo();
        this.pidTableComp.repaint();
    }

    public enum Axis {
        HORIZONTAL,
        VERTICAL,
    }

}
