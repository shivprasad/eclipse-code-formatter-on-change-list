package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SequentialModalProgressTask;
import com.intellij.util.SequentialTask;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.FutureTask;

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

        if(allFiles.isEmpty()){
            return;
        }

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

    public void formatCodeWithProgressBar(Project project, List<VirtualFile> changes) {
        // invoke command
        String command = state.command.trim();
        String commandArgs = state.commandArgs.trim();

        if (!isSetupComplete(true)) {
            return;
        }

        LinkedHashSet<String> allFiles = ChangelistUtil.createFilenames(changes, project, 0);

        if(allFiles.isEmpty()){
            return;
        }

        ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
        String oldText = null;
        double oldFraction = 0.0D;
        if (progress != null) {
            oldText = progress.getText();
            oldFraction = progress.getFraction();
            progress.setText("Formatting");
        }

        final List tasks = new ArrayList(allFiles.size());
        List allFilesForFormatting = new ArrayList(allFiles);
        for (int i = 0; i < allFilesForFormatting.size(); i++) {
            String file = (String) allFilesForFormatting.get(i);
            if (progress != null) {
                if (progress.isCanceled()) return;
                progress.setFraction(i / allFilesForFormatting.size());
            }
            tasks.add(preprocessFile(file, true,command,commandArgs,project));
        }

        if (progress != null) {
            progress.setText(oldText);
            progress.setFraction(oldFraction);
        }

        SequentialModalProgressTask progressTask = new SequentialModalProgressTask(project, "Eclipse Code Formatter");
        ReformatFilesTask reformatFilesTask = new ReformatFilesTask(tasks);
        reformatFilesTask.setCompositeTask(progressTask);
        progressTask.setTask(reformatFilesTask);
        ProgressManager.getInstance().run(progressTask);
    }

    private class ReformatFilesTask implements SequentialTask {
        private final List<FutureTask<Boolean>> myTasks;
        private final int myTotalTasksNumber;
        private SequentialModalProgressTask myCompositeTask;

        ReformatFilesTask(List tasks) {
            this.myTasks = tasks;
            this.myTotalTasksNumber = this.myTasks.size();
        }

        public void prepare()
        {
        }

        public boolean isDone()
        {
            return this.myTasks.isEmpty();
        }

        public boolean iteration()
        {
            if (this.myTasks.isEmpty()) {
                return true;
            }
            FutureTask task = (FutureTask)this.myTasks.remove(this.myTasks.size() - 1);
            if (task == null) {
                return this.myTasks.isEmpty();
            }
            task.run();
            try {
                try {
                    if ((!((Boolean)task.get()).booleanValue()) || (task.isCancelled())) {
                        this.myTasks.clear();
                        return true;
                    }
                } catch (java.util.concurrent.ExecutionException e) {
                    return true;
                }
            }
            catch (InterruptedException e) {
                return true;
            }
            if (this.myCompositeTask != null) {
                ProgressIndicator indicator = this.myCompositeTask.getIndicator();
                if (indicator != null) {
                    indicator.setText("Formatting Java file " + (this.myTotalTasksNumber - this.myTasks.size()) + " of " + this.myTotalTasksNumber);
                    indicator.setFraction((this.myTotalTasksNumber - this.myTasks.size()) / this.myTotalTasksNumber);
                }
            }
            return this.myTasks.isEmpty();
        }

        public void stop()
        {
            this.myTasks.clear();
        }

        public void setCompositeTask(@Nullable SequentialModalProgressTask compositeTask) {
            this.myCompositeTask = compositeTask;
        }
    }



    protected FutureTask<Boolean> preprocessFile(final String file, boolean processChangedTextOnly, final String command, final String commandArgs, final Project project) throws IncorrectOperationException {
        final List<Runnable> runnables = new ArrayList<Runnable>();
        runnables.add(new Runnable() {
            public void run() {
                String cmd = command + " -application org.eclipse.jdt.core.JavaCodeFormatter -nosplash -verbose -config "
                        + commandArgs
                        + " "
                        + file;
                CmdExecutor.execute(project, cmd, state.executeInBackground);
            }
        });
        Runnable runnable = runnables.isEmpty() ? EmptyRunnable.getInstance() : new Runnable()
        {
            public void run() {
                for (Runnable runnable : runnables)
                    runnable.run();
            }
        };
        return new FutureTask(runnable, Boolean.valueOf(true));
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