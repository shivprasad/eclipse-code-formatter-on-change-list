package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EclipseCodeFormatter implements Runnable {
    private Project project;

    private List<VirtualFile> virtualFileList;

    private ChangelistActionComponent component;

    public EclipseCodeFormatter(Project project, List<VirtualFile> virtualFileList, ChangelistActionComponent component) {
        this.project = project;
        this.virtualFileList = virtualFileList;
        this.component = component;
    }

    public void run() {
        component.invokeAction(project, virtualFileList);
    }
}
