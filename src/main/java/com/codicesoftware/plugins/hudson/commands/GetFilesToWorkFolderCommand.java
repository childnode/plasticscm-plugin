package com.codicesoftware.plugins.hudson.commands;

import com.codicesoftware.plugins.hudson.util.MaskedArgumentListBuilder;

public class GetFilesToWorkFolderCommand extends AbstractCommand {
    private String workFolder = ".";

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider configurationProvider) {
        super(configurationProvider);
    }

    public GetFilesToWorkFolderCommand(ServerConfigurationProvider configurationProvider, String workFolder) {
        this(configurationProvider);
        this.workFolder = workFolder;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();

        arguments.add("update");
        arguments.add(workFolder);
        
        return arguments;
    }
}