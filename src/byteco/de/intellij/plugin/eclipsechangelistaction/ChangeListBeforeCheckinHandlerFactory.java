package byteco.de.intellij.plugin.eclipsechangelistaction;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

public class ChangeListBeforeCheckinHandlerFactory  extends CheckinHandlerFactory {
    @NotNull
    @Override
    public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel, CommitContext commitContext) {
        return new ChangeListBeforeCheckinHandler(checkinProjectPanel.getProject(),checkinProjectPanel);
    }
}
