package com.codicesoftware.plugins.hudson.model;

import com.codicesoftware.plugins.hudson.PlasticTool;
import com.codicesoftware.plugins.hudson.commands.ServerConfigurationProvider;
import com.codicesoftware.plugins.hudson.util.MaskedArgumentListBuilder;

import java.io.IOException;
import java.io.Reader;

public class Server implements ServerConfigurationProvider {
    private Workspaces workspaces;
    private final PlasticTool tool;

    public Server(PlasticTool tool) {
        this.tool = tool;
    }

    public Workspaces getWorkspaces() {
        if (workspaces == null) {
            workspaces = new Workspaces(this);
        }

        return workspaces;
    }

    public Reader execute(MaskedArgumentListBuilder arguments) throws IOException, InterruptedException {
        return tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
    }
}