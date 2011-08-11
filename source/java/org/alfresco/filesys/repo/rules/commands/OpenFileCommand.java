/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.filesys.repo.rules.commands;

import java.util.List;

import org.alfresco.filesys.repo.OpenFileMode;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Open File command
 */
public class OpenFileCommand implements Command
{
    private String name;
    private OpenFileMode mode = OpenFileMode.READ;
    private boolean truncate = false;
    private String path;
    private NodeRef rootNode;
    
    /**
     * 
     * @param name
     * @param writeAccess
     * @param rootNode
     * @param truncate
     * @param path
     */
    public OpenFileCommand(String name, OpenFileMode mode, boolean truncate, NodeRef rootNode, String path)
    {
        this.name = name;
        this.mode = mode;
        this.truncate = truncate;
        this.rootNode = rootNode;
        this.path = path;
    }

    public String getName()
    {
        return name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNode;
    }
    
    public OpenFileMode getMode()
    {
        return mode;
    }
    
    public boolean isTruncate()
    {
        return truncate;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_ONLY;
    }
}
