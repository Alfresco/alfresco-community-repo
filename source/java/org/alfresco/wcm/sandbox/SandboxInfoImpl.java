/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.sandbox;

import java.util.Date;

import org.alfresco.config.JNDIConstants;
import org.alfresco.service.namespace.QName;

/**
 * Provides information about a WCM sandbox created by Sandbox Service/Factory
 */
public class SandboxInfoImpl implements SandboxInfo
{
    private String wpStoreId;
    private String sbStoreId;
    private QName sandboxType;
    private String name;
    private String[] storeNames;
    private Date createdDate;
    private String creator;

    /* package */ SandboxInfoImpl(String wpStoreId, String sbStoreId, QName sandboxType, String name, String[] storeNames, Date createdDate, String creator)
    {
        this.wpStoreId = wpStoreId;
        this.sbStoreId = sbStoreId;
        this.sandboxType = sandboxType;
        this.name = name;
        this.storeNames = storeNames;
        this.createdDate = createdDate;
        this.creator = creator;
    }
    
    // note: currently derived - for author sandbox this is the username, for staging sandbox this is the sandbox id
    public String getName()
    {
        return this.name;
    }
    
    public String getWebProjectId()
    {
        return this.wpStoreId;
    }
    
    public String getSandboxId()
    {
        return this.sbStoreId;
    }
    
    public QName getSandboxType()
    {
        return this.sandboxType;
    }
    
    public Date getCreatedDate()
    {
        return this.createdDate;
    }
    
    public String getCreator()
    {
        return this.creator;
    }
    
    public String getSandboxRootPath()
    {
        return JNDIConstants.DIR_DEFAULT_WWW_APPBASE;
    }

    /**
    *  A list of names of the stores within this sandbox.
    *  The "main" store should come first in this list;
    *  any other stores should appear in the order that 
    *  they are overlaid on "main" (e.g.: any "preview" 
    *  layers should come afterward, in "lowest first" order).
    *  <p>
    *  Note: all sandboxes must have a "main" layer.
    */
    public String [] getStoreNames() { return storeNames; }

    /**
    *  The name of the "main" store within this sandbox.
    */
    public String getMainStoreName() 
    { 
        return storeNames[0];
    }
}
