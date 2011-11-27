package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

@State(
        name = "VCS Changelist - Eclipse Code Formatter",
        storages = {
                @Storage(id = "eclipsechangelist-action-default", file = "$PROJECT_FILE$"),
                @Storage(id = "eclipsechangelist-action-dir", file = "$PROJECT_CONFIG_DIR$/eclipsechangelist-action.xml", scheme = StorageScheme.DIRECTORY_BASED)})

public class ChangelistActionComponent implements Configurable, PersistentStateComponent<ChangelistActionComponent.State> {

    private static final Logger LOG = Logger.getLogger(ChangelistActionComponent.class);

    public static final String COMPONENT_NAME = "VCS Changelist - Eclipse Code Formatter";

    public boolean isSetupComplete(boolean flag) {
        String command = state.command.trim();
        String commandArgs = state.commandArgs.trim();
        if (command.length() == 0 || commandArgs.length() == 0) {
            LOG.error("Eclipse executable/config file path is not set.");
            if (flag)
                JOptionPane.showMessageDialog(null, "Eclipse executable/config file path is not set.", "VCS Changelist - Eclipse Code Formatter", 1, Icons.ERROR_INTRODUCTION_ICON);
            else
                System.err.println("VCS Changelist - Eclipse Code Formatter -> Eclipse executable/config file path is not set.");
            return false;
        }
        return true;
    }

    public void invokeAction(Project project, List<VirtualFile> changes) {

        LinkedHashSet<String> allFiles = ChangelistUtil.createFilenames(changes, project, 0);

        // prepare list
        StringBuilder spaceFiles = new StringBuilder();
        for (String affectedFile : allFiles) {
            spaceFiles.append(affectedFile).append(" ");
        }

        // invoke command
        String command = state.command.trim();
        String commandArgs = state.commandArgs.trim();

        if (!isSetupComplete(true)) {
            return;
        }

        command += " -application org.eclipse.jdt.core.JavaCodeFormatter -nosplash -verbose -config "
                + commandArgs
                + " "
                + spaceFiles.toString();

        if (state.consoleOutput) {
            CmdExecutor.execute(project, command, state.executeInBackground);
        } else {
            try {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
            } catch (IOException ioex) {
                LOG.error("Error invoking command.", ioex);
            }
        }
    }

    // ---------------------------------------------------------------- configurable

    private ChangelistActionConfiguration configurationComponent;
    private Icon pluginIcon;

    /**
     * Returns display name.
     */
    @Nls
    public String getDisplayName() {
        return COMPONENT_NAME;
    }

    /**
     * Returns plugin icon.
     */
    @Nullable
    public Icon getIcon() {
        if (pluginIcon == null) {
            pluginIcon = IconLoader.getIcon("/byteco/de/intellij/plugin/eclipsechangelistaction/icon32.png");
        }
        return pluginIcon;
    }

    /**
     * No help is available.
     */
    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    /**
     * Returns the user interface component for editing the configuration.
     */
    public JComponent createComponent() {
        if (configurationComponent == null) {
            configurationComponent = new ChangelistActionConfiguration();
            configurationComponent.load(state);
        }
        return configurationComponent.getPanel();
    }

    /**
     * Checks if the settings in the configuration panel were modified by the user and
     * need to be saved.
     */
    public boolean isModified() {
        return configurationComponent.isModified();
    }

    /**
     * Store the settings from configurable to other components.
     * Repaints all editors.
     */
    public void apply() throws ConfigurationException {
        configurationComponent.save();
    }

    /**
     * Load settings from other components to configurable.
     */
    public void reset() {
        configurationComponent.load(state);
    }

    /**
     * Disposes the Swing components used for displaying the configuration.
     */
    public void disposeUIResources() {
        configurationComponent = null;
    }

    // ------------------------------------------------------ state

    /**
     * Configuration state.
     */
    public static final class State {
        public String command = "";
        public String commandArgs = "";
        public boolean consoleOutput;
        public boolean executeInBackground;
    }

    private final State state = new State();

    /**
     * Returns plugin state.
     */
    public State getState() {
        return state;
    }

    /**
     * Loads state from configuration file.
     */
    public void loadState(State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

}