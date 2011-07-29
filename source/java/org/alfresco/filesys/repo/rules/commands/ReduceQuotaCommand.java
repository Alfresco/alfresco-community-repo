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

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Open File command
 */
public class ReduceQuotaCommand implements Command
{
    private String name;
    private String path;
    private NodeRef rootNode;
  
    private NetworkFile networkFile;
    
    public ReduceQuotaCommand(String name, NetworkFile networkFile, NodeRef rootNode, String path)
    {
        this.name = name;
        this.networkFile = networkFile;
    }

    public String getName()
    {
        return name;
    }
    
    public NetworkFile getNetworkFile()
    {
        return networkFile;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNode;
    }
    
    public String getPath()
    {
        return path;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_NONE;
    }
}
