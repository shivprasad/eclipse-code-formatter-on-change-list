package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: shiv
 * Date: 27/11/2011
 * Time: 13:06
 */
public class EclipseCodeFormatterCommitSession implements CommitSession {
    private Project project;

    public EclipseCodeFormatterCommitSession(Project project) {
        this.project = project;
    }

    public JComponent getAdditionalConfigurationUI() {
        return null;
    }

    public JComponent getAdditionalConfigurationUI(Collection<Change> changes, String s) {
        return null;
    }

    public boolean canExecute(Collection<Change> changes, String s) {
        return changes.size()>0;
    }

    public void execute(Collection<Change> changes, String s) {
        ChangelistActionComponent component = project.getComponent(ChangelistActionComponent.class);
        if(component.isSetupComplete(false)){
            ApplicationManager.getApplication().invokeAndWait(
                    new EclipseCodeFormatter(project,
                            changes, component),
                    ModalityState.defaultModalityState());
        }
    }

    public void executionCanceled() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHelpId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private class EclipseCodeFormatter implements Runnable {
        private Project project;
        private Collection<Change> changes;
        private ChangelistActionComponent component;

        public EclipseCodeFormatter(Project project, Collection<Change> changes, ChangelistActionComponent component) {
            this.project = project;
            this.changes = changes;
            this.component = component;
        }

        public void run() {
            List<VirtualFile> virtualFileList = new ArrayList<VirtualFile>();
            for (Change change : changes) {
                virtualFileList.add(change.getVirtualFile());
            }

            component.invokeAction(project, virtualFileList);

        }
    }
}
