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

/**
*  Provides basic information about a WCM asset
*/
public interface AssetInfo
{
    public String getName();
    
    public String getSandboxId();
    
    public int getSandboxVersion();
    
    public String getPath(); // full path, eg. include /www/avm_webapps/...
    
    public boolean isFile();
    
    public boolean isFolder();
    
    public boolean isDeleted();
    
    public boolean isLocked(); // files only, false for folder
    
    public long getFileSize(); // files only, -1 for folder
    
    public String getLockOwner(); // files only, null if no lock (or folder)
    
    public String getCreator();
    
    public Date getCreatedDate();
    
    public String getModifier();
    
    public Date getModifiedDate();
    
    public String getAvmPath(); // absolute AVM path, eg. <avmStore>:<path>
    
    public int getDiffCode(); // if applicable, eg. when getting list of changed assets (see AVMDifference for diff codes), else -1
}
