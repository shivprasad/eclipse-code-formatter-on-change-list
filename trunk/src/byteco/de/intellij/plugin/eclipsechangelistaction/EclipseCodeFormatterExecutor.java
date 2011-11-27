package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * User: shiv
 * Date: 27/11/2011
 * Time: 13:02
 */
public class EclipseCodeFormatterExecutor implements CommitExecutor {
    private Project project;

    public EclipseCodeFormatterExecutor(Project project) {
        this.project = project;
    }

    @Nls
    public String getActionText() {
        return "Eclipse Code Formatter";
    }

    @NotNull
    public CommitSession createCommitSession() {
        return new EclipseCodeFormatterCommitSession(project);
    }
}
