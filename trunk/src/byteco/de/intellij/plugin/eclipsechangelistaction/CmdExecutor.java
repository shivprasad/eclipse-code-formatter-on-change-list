package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.concurrency.SwingWorker;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class CmdExecutor {

    private static final Logger LOG = Logger.getLogger(CmdExecutor.class);

    private static final String ID = "Eclipse Code Formatter Console";

    private static ToolWindow toolWindow;

    private static ConsoleView consoleView;

    private static String lastCommand;

    private static boolean lastInBackground;

    /**
     * Returns console view. Makes sure its never <code>null</code>.
     */
    private static ConsoleView getConsoleView(Project project) {
        if (consoleView == null) {
            TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
            consoleView = builder.getConsole();
        }
        return consoleView;
    }

    // ---------------------------------------------------------------- execution

    /**
     * Executes command and shows it in the console.
     */
    public static void execute(final Project project, final String command, boolean inBackground) {

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindow = toolWindowManager.getToolWindow(ID);

        if (toolWindow == null) {
            toolWindow = createToolWindow(toolWindowManager, project);
        }

        lastCommand = command;
        lastInBackground = inBackground;

        if (inBackground) {
            LOG.info("Invoking command in background: " + command);
            SwingWorker worker = new SwingWorker() {
                @Override
                public Object construct() {
                    executeCommand(command, project);
                    return null;
                }
            };
            worker.start();
        } else {
            LOG.info("Invoking command: " + command);
            executeCommand(command, project);
        }
    }

    private static int executeCommand(String command, Project project) {

        try {
            Runtime rt = Runtime.getRuntime();

            Process proc = rt.exec(command);

            if (project != null) {
                OSProcessHandler handler = new OSProcessHandler(proc, command);
                getConsoleView(project).attachToProcess(handler);
                handler.startNotify();
            }

            // any error???
            int exitValue = proc.waitFor();
            LOG.debug("Exit value: " + exitValue);
            return exitValue;
        } catch (Exception ex) {
            LOG.warn("Error executing command.", ex);
        }
        return 0;
    }

    // ---------------------------------------------------------------- tool window

    /**
     * Creates tool window.
     */
    private static ToolWindow createToolWindow(final ToolWindowManager toolWindowManager, final Project project) {

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnAction("Repeat last changelist action",
                "Invoke preivous user action on VCS changelist.",
                IconLoader.getIcon
                        ("/byteco/de/intellij/plugin/eclipsechangelistaction/icon16.png")) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                execute(project, lastCommand, lastInBackground);
            }
        });
        actionGroup.add(new AnAction("Clear console",
                "Clear console window.",
                IconLoader.getIcon
                        ("/byteco/de/intellij/plugin/eclipsechangelistaction/clear.png")) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                getConsoleView(project).clear();
            }
        });
        actionGroup.add(new AnAction("Close",
                "Close Changelist Action window.",
                IconLoader.getIcon("/actions/cancel.png")) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                getConsoleView(project).clear();
                toolWindowManager.unregisterToolWindow(ID);
            }
        });

        ActionManager actionManager = ActionManager.getInstance();
        JComponent toolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false).getComponent();

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolbar, BorderLayout.WEST);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(getConsoleView(project).getComponent(), "", false);

        panel.add(content.getComponent(), BorderLayout.CENTER);

        ToolWindow window = registerToolWindow(toolWindowManager, panel);

        window.show(
                new Runnable() {
                    public void run() {
                        // System.out.println("Do something here");
                    }
                });

        return window;
    }

    private static ToolWindow registerToolWindow(final ToolWindowManager toolWindowManager, final JPanel panel) {

        final ToolWindow window = toolWindowManager.registerToolWindow(ID, true, ToolWindowAnchor.BOTTOM);
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final Content content = contentFactory.createContent(panel, "", false);

        content.setCloseable(false);
        window.getContentManager().addContent(content);
        window.setIcon(IconLoader.getIcon("/byteco/de/intellij/plugin/eclipsechangelistaction/icon16.png"));

        return window;
    }

}
