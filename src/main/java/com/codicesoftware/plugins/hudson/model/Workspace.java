package com.codicesoftware.plugins.hudson.model;

import com.codicesoftware.plugins.hudson.commands.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Workspace {
    private final Server server;
    private final String name;
    private final String path;
    private String selector;

    public Workspace (Server server, String name, String path, String selector) {
        this.server = server;
        this.name = name;
        this.path = path;
        this.selector = selector;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSelector() {
        if (selector == null) {
            // Get the selector from the server
            GetSelectorCommand command = new GetSelectorCommand(server, name);
            Reader reader = null;
            try {
                reader = server.execute(command.getArguments());
                selector = command.parse(reader);
            } catch (IOException e) {
            } catch (InterruptedException e) {
            } catch (ParseException e) {
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public List<ChangeSet> getDetailedHistory(Calendar fromTimestamp, Calendar toTimestamp)
            throws IOException, InterruptedException, ParseException {
        List<ChangeSet> list = getChangesets(fromTimestamp, toTimestamp);
        String workspaceDir;

        Reader reader = null;
        GetWorkspaceFromPathCommand gwpCommand = new GetWorkspaceFromPathCommand(server, getPath());
        try {
            reader = server.execute(gwpCommand.getArguments());
            workspaceDir = gwpCommand.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        for(ChangeSet cs : list) {
            cs.setWorkspaceDir(workspaceDir);

            GetChangesetRevisionsCommand revs = new GetChangesetRevisionsCommand(server, cs.getVersion(), cs.getRepository());

            try {
                reader = server.execute(revs.getArguments());
                revs.parse(reader, cs);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }

        return list;
    }

    private List<ChangeSet> getChangesets(Calendar fromTimestamp, Calendar toTimestamp)
            throws IOException, InterruptedException, ParseException {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        Reader reader = null;

        WorkspaceInfo wi;
        GetWorkspaceInfoCommand wiCommand = new GetWorkspaceInfoCommand(server, getPath());
        try {
            reader = server.execute(wiCommand.getArguments());
            wi = wiCommand.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        String branch;

        if (wi.getBranch().equals("Multiple")) {
            List<ChangesetID> cslist;
            GetWorkspaceStatusCommand statusCommand = new GetWorkspaceStatusCommand(server, getPath());
            try {
                reader = server.execute(statusCommand.getArguments());
                cslist = statusCommand.parse(reader);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            for (ChangesetID cs : cslist) {
                branch = GetBranchFromChangeset(cs.getId(), cs.getRepoName());

                DetailedHistoryCommand histCommand = new DetailedHistoryCommand(server, fromTimestamp, toTimestamp, branch, cs.getRepository());
                try {
                    reader = server.execute(histCommand.getArguments());
                    list.addAll(histCommand.parse(reader));
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            }
        } else {
            branch = GetBranchFromWorkspaceInfo(wi);
            DetailedHistoryCommand histCommand = new DetailedHistoryCommand(server, fromTimestamp, toTimestamp, branch, wi.getRepoName());
            try {
                reader = server.execute(histCommand.getArguments());
                list = histCommand.parse(reader);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }

        return list;
    }

    /*
     * Same as getDetailedHistory, but doesn't fill in the list of revisions in each changeset
     */
    public List<ChangeSet> getBriefHistory(Calendar fromTimestamp, Calendar toTimestamp)
            throws IOException, InterruptedException, ParseException {
        List<ChangeSet> list = getChangesets(fromTimestamp, toTimestamp);

        return list;
    }

    public void getFiles(String localPath) throws IOException, InterruptedException {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(server, localPath);
        server.execute(command.getArguments()).close();
    }

    private String GetBranchFromWorkspaceInfo(WorkspaceInfo wi) throws InterruptedException, ParseException, IOException {
        String branch = wi.getBranch();
        if (branch != null && !branch.isEmpty())
            return branch;

        String label = wi.getLabel();
        if (label != null && !label.isEmpty())
            return GetBranchFromLabel(label, wi.getRepoName());

        String changeset = wi.getChangeset();
        if (changeset != null && !changeset.isEmpty())
            return GetBranchFromChangeset(changeset, wi.getRepoName());

        return "";
    }

    private String GetBranchFromLabel(String label, String repositoryName) throws InterruptedException, ParseException, IOException {
        GetBranchForLabelCommand brCommand = new GetBranchForLabelCommand(server, label, repositoryName);
        Reader reader = null;
        try {
            reader = server.execute(brCommand.getArguments());
            return brCommand.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private String GetBranchFromChangeset(String id, String repositoryName) throws InterruptedException, ParseException, IOException {
        GetBranchForChangesetCommand brCommand = new GetBranchForChangesetCommand(server, id, repositoryName);
        Reader reader = null;
        try {
            reader = server.execute(brCommand.getArguments());
            return brCommand.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27).append(name).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final Workspace other = (Workspace)obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.name, other.name);
        builder.append(this.path, other.path);
        return builder.isEquals();
    }
}