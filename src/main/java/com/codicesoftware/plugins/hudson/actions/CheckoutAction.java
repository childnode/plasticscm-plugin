package com.codicesoftware.plugins.hudson.actions;

import com.codicesoftware.plugins.hudson.model.ChangeSet;
import com.codicesoftware.plugins.hudson.model.Server;
import com.codicesoftware.plugins.hudson.model.Workspace;
import com.codicesoftware.plugins.hudson.model.Workspaces;
import hudson.FilePath;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CheckoutAction {
    private final String workspaceName;
    private final String selector;
    private final String workfolder;
    private final boolean useUpdate;

    public CheckoutAction(String workspaceName, String selector, String workfolder, boolean useUpdate) {
        this.workspaceName = workspaceName;
        this.selector = selector;
        this.workfolder = workfolder;
        this.useUpdate = useUpdate;
    }

    public List<ChangeSet> checkout(Server server, FilePath workspacePath, Calendar lastBuildTimestamp, Calendar currentBuildTimestamp)
        throws IOException, InterruptedException, ParseException {
        
        Workspaces workspaces = server.getWorkspaces();

        if (workspaces.exists(workspaceName) && !useUpdate) {
            Workspace workspace = workspaces.getWorkspace(workspaceName);
            workspaces.deleteWorkspace(workspace);
        }

        Workspace workspace;
        if (!workspaces.exists(workspaceName)) {
            if (!useUpdate && workspacePath.exists()) {
                workspacePath.deleteContents();
            }
            workspace = workspaces.newWorkspace(workspacePath, workspaceName, workfolder, selector);
            workspace.getFiles(workfolder);
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
            if (!workspace.getSelector().equals(selector)) {
                workspace.setSelector(selector);
                workspaces.setWorkspaceSelector(workspacePath, workspace);
            }
            else {
                workspace.getFiles(workfolder);
            }
        }


        if (lastBuildTimestamp != null) {
            return workspace.getDetailedHistory(lastBuildTimestamp, currentBuildTimestamp);
        }
        return new ArrayList<ChangeSet>();
    }
}
