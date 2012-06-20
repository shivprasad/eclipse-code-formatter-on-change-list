package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class SelectedChangelistPopupAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();

        Project project = DataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }

        ChangelistActionComponent clActionComponent = project.getComponent(ChangelistActionComponent.class);

        ChangeList[] selectedChangeLists = e.getData(VcsDataKeys.CHANGE_LISTS);
        if (selectedChangeLists == null) {
            return;
        }

        for (ChangeList iChangeList : selectedChangeLists) {
            String changelistName = iChangeList.getName();
            List<Change> changes = new ArrayList<Change>(iChangeList.getChanges());

            List<VirtualFile> changeFiles = new ArrayList<VirtualFile>(changes.size());
            for (Change change : changes) {
                changeFiles.add(change.getVirtualFile());
            }
            clActionComponent.invokeAction(project, changeFiles);
            // TODO: force opening the console (depending on a flag)?
        }
    }
}
