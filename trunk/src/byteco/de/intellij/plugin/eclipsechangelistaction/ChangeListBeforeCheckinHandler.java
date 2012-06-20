package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressBar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinMetaHandler;
import com.intellij.openapi.vcs.checkin.OptimizeImportsBeforeCheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class ChangeListBeforeCheckinHandler extends CheckinHandler
        implements CheckinMetaHandler {

    protected final Project myProject;
    private final CheckinProjectPanel myPanel;
    private BeforeCheckinConfiguration beforeCheckinConfiguration = new BeforeCheckinConfiguration();

    public ChangeListBeforeCheckinHandler(Project project, CheckinProjectPanel panel)
    {
        this.myProject = project;
        this.myPanel = panel;
    }


    private class BeforeCheckinConfiguration implements RefreshableOnComponent {
        private boolean runEclipseCodeFormatter = false;
        JCheckBox optimizeBox = new JCheckBox("Run Eclipse Code Formatter");

        public JComponent getComponent() {
            JPanel panel = new JPanel(new GridLayout(1, 0));
            panel.add(optimizeBox);
            return panel;
        }

        public void refresh() {
        }

        public void saveState() {
            runEclipseCodeFormatter = optimizeBox.isSelected();
        }

        public void restoreState() {
            //optimizeBox.setSelected(runEclipseCodeFormatter);
        }
    }

    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        return beforeCheckinConfiguration;
    }

    public void runCheckinHandlers(Runnable runnable) {
        if(beforeCheckinConfiguration.runEclipseCodeFormatter){
            java.util.List changes = new ArrayList(this.myPanel.getVirtualFiles());
            ChangelistActionComponent component = myProject.getComponent(ChangelistActionComponent.class);
            if (component.isSetupComplete(false)) {
                component.formatCodeWithProgressBar(myProject,changes);
                runnable.run();
            }
        } else{
            runnable.run();
        }
    }


}
