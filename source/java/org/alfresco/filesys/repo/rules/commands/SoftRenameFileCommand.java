package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.service.cmr.repository.NodeRef;

public class SoftRenameFileCommand extends RenameFileCommand
{

    public SoftRenameFileCommand(String from, String to, NodeRef rootNode,
            String fromPath, String toPath)
    {
        super(from, to, rootNode, fromPath, toPath);
    }
    
    public boolean isSoft()
    {
        return true;
    }

}
