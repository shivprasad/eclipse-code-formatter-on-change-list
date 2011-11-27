package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import org.jetbrains.annotations.NotNull;

/**
 * User: shiv
 * Date: 27/11/2011
 * Time: 12:16
 */
public class ThePluginProjectComponent implements ProjectComponent
{

    private final Project project;
    private boolean created;

    public ThePluginProjectComponent(Project project) {
        this.project = project;
        created=false;
        StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
            public void run() {
                initializePlugin();
            }
        });

    }

    private void initializePlugin() {
        if (!created) {
			ChangeListManager.getInstance(project).registerCommitExecutor(
					new EclipseCodeFormatterExecutor(project));
            created = true;
        }
    }


    public void projectOpened() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void projectClosed() {
        if (created) {
            created = false;
        }
    }

    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public String getComponentName() {
        return "TheChangeListPluginProjectComponent";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
