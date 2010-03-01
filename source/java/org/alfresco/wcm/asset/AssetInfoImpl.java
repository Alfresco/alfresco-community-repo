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
package org.alfresco.wcm.asset;

import java.util.Date;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.wcm.util.WCMUtil;

/**
 * Provides information about a WCM asset
 */
public class AssetInfoImpl implements AssetInfo
{
    private AVMNodeDescriptor node;
    private String lockOwner = null; // null if not locked
    private int sandboxVersion = -1;
    private int diffCode = -1; // if applicable, eg. when getting list of modified assets
    
    /* package */ AssetInfoImpl(int sandboxVersion, AVMNodeDescriptor node, String lockOwner)
    {
        this.sandboxVersion = sandboxVersion;
        this.node = node;
        this.lockOwner = lockOwner;
    }

    public String getName()
    {
        return node.getName();
    }
    
    public String getSandboxId()
    {
        return WCMUtil.getSandboxStoreId(node.getPath());
    }
    
    public String getPath()
    {
        return WCMUtil.getStoreRelativePath(node.getPath());
    }
    
    public boolean isFile()
    {
    	return (node.isFile() || node.isDeletedFile());
    }
    
    public boolean isFolder()
    {
    	return (node.isDirectory() || node.isDeletedDirectory());
    }
    
    public boolean isDeleted()
    {
        return node.isDeleted();
    }
    
    public String getCreator()
    {
        return node.getCreator();
    }
    
    public Date getCreatedDate()
    {
        return new Date(node.getCreateDate());
    }
    
    public String getModifier()
    {
        return node.getLastModifier(); 
    }
    
    public Date getModifiedDate()
    {
        return new Date(node.getModDate());
    }
    
    public int getSandboxVersion()
    {
        return sandboxVersion;
    }
    
    public boolean isLocked()
    {
    	return (lockOwner != null);
    }
    
    public long getFileSize()
    {
        return node.getLength();
    }
    
    public String getLockOwner()
    {
        return lockOwner;
    }
    
    public AVMNodeDescriptor getAVMNodeDescriptor()
    {
        return node;
    }
    
    public String getAvmPath()
    {
        return node.getPath();
    }
    
    public int getDiffCode()
    {
       return diffCode;
    }
    
    public void setDiffCode(int diffCode)
    {
        this.diffCode = diffCode;
    }
}
