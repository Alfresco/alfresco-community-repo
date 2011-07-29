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

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CopyContent command
 * 
 * Copy just the content from one node to another
 *
 */
public class CopyContentCommand implements Command
{
    
    private String from;
    private String to;
    private NodeRef rootNode;
    private String fromPath;
    private String toPath;
    
    public CopyContentCommand(String from, String to, NodeRef rootNode, String fromPath, String toPath)
    {
        this.from = from;
        this.to = to;
        this.rootNode = rootNode;
        this.fromPath = fromPath;
        this.toPath = toPath;
    }
    
    public String getTo()
    {
        return from;
    }
 
    public String getFrom()
    {
        return from;
    }
    
    public NodeRef getRootNode()
    {
        return rootNode;
    }
    
    public String getFromPath()
    {
        return fromPath;
    }
    
    public String getToPath()
    {
        return toPath;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_WRITE;
    }
}
