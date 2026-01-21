import module java.base;
import config.Config;
import config.ConfigHelper;
import gui.Window;
import settings.Settings;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class Main {
    public static final String DEFAULT_CONFIG_PATH = "./defaults.mwi";

    public static final ConfigHelper CONFIG_HELPER = new ConfigHelper();

    void main() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            Window.FRAME = frame;
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(screenSize.width, screenSize.height);
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);

            Settings settings = new Settings();
            // Initialize default values for the settings
            settings.init();

            Window window = new Window(settings);
            // Create tabs...
            window.init();
            // Add the actual panel of the window
            // and set it to be the content pane
            window.addPanel(frame::setContentPane);

            JMenuBar menuBar = new JMenuBar();

            // Create the "File" menu
            JMenu fileMenu = new JMenu("File");
            JMenu newFile = new JMenu("New");
            JMenuItem newEmpty = newFile.add(new JMenuItem("New empty"));
            newEmpty.addActionListener(_ -> this.setNewConfig(Config.EMPTY, window));
            JMenuItem newDefault = newFile.add(new JMenuItem("New with defaults"));
            newDefault.addActionListener(_ -> this.setNewConfig(CONFIG_HELPER.parse(new File(DEFAULT_CONFIG_PATH)), window));
            JMenuItem importFile = new JMenuItem("Import Config");
            importFile.addActionListener(_ -> this.importConfig(window));
            JMenuItem exportFile = new JMenuItem("Export Config");
            exportFile.addActionListener(_ -> this.exportConfig(window));

            // Add items to the "File" menu
            fileMenu.add(newFile);
            fileMenu.add(importFile);
            fileMenu.add(exportFile);
            //fileMenu.addSeparator(); // Add a separator line

            JMenu editMenu = new JMenu("Edit");
            JMenuItem cut = editMenu.add(new JMenuItem("Cut"));
            JMenuItem copy = editMenu.add(new JMenuItem("Copy"));
            JMenuItem paste = editMenu.add(new JMenuItem("Paste"));

            cut.addActionListener(_ -> JOptionPane.showMessageDialog(frame, "Cut clicked"));
            copy.addActionListener(_ -> JOptionPane.showMessageDialog(frame, "Copy clicked"));
            paste.addActionListener(_ -> JOptionPane.showMessageDialog(frame, "Paste clicked"));

            // Create the "View" menu
            JMenu viewMenu = new JMenu("View");
            viewMenu.add(new JCheckBoxMenuItem("Show Status Bar", true));

            menuBar.add(fileMenu);
            menuBar.add(editMenu);
            menuBar.add(viewMenu);

            // Add the menu bar to the frame
            frame.setJMenuBar(menuBar);
        });
    }

    private void exportConfig(Window window) {
        try {
            CONFIG_HELPER.createFile(window.getSettings().getConfig(), new File("exported.mwi"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importConfig(Window window) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Multiwii config file to import");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Multiwii configuration file (*.mwi)", "mwi");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(Window.FRAME);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(Window.FRAME,
                    "Successfully imported: " + selectedFile.getAbsolutePath());
            Config config = CONFIG_HELPER.parse(selectedFile);
            this.setNewConfig(config, window);
            try {
                System.out.println(Files.readString(Path.of(selectedFile.getAbsolutePath())));
            } catch (IOException e) {
                System.err.println("Failed to open file: " + e.getMessage());
            }
        }
    }

    private void setNewConfig(Config config, Window window) {
        window.getSettings().setConfig(config);
        window.updateConfig();

    }
}
