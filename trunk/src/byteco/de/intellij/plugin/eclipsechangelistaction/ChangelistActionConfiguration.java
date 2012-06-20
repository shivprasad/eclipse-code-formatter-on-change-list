package byteco.de.intellij.plugin.eclipsechangelistaction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChangelistActionConfiguration {

    private JTextField command;

    private JTextField commandArgs;

    private JPanel panel;

    private JCheckBox consoleOutput;

    private JCheckBox executeInBackground;

    private JButton toolPathSelector;

    private JButton configSelector;

    public ChangelistActionConfiguration() {
        toolPathSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    command.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        configSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    commandArgs.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private ChangelistActionComponent.State state = new ChangelistActionComponent.State();

    public void load(ChangelistActionComponent.State state) {
        this.state = state;
        command.setText(state.command);
        commandArgs.setText(state.commandArgs);
        consoleOutput.setSelected(state.consoleOutput);
        executeInBackground.setSelected(state.executeInBackground);
    }

    public ChangelistActionComponent.State save() {
        state.command = command.getText();
        state.commandArgs = commandArgs.getText();
        state.consoleOutput = consoleOutput.isSelected();
        state.executeInBackground = executeInBackground.isSelected();
        return state;
    }

    public boolean isModified() {
        return !state.command.equals(command.getText()) ||
                !state.commandArgs.equals(commandArgs.getText()) ||
                state.consoleOutput != consoleOutput.isSelected() ||
                state.executeInBackground != executeInBackground.isSelected();
    }
}
