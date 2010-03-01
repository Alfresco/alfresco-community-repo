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
package org.alfresco.wcm.sandbox;

import java.util.Date;

import org.alfresco.service.cmr.avm.VersionDescriptor;

/**
*  Provides information about a WCM sandbox version (snapshot).
*/
public class SandboxVersionImpl implements SandboxVersion
{
    private VersionDescriptor vDesc;
    
    /* package */ SandboxVersionImpl(VersionDescriptor vDesc)
    {
        this.vDesc = vDesc;
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getSandboxId()
     */
    public String getSandboxId()
    {
        return vDesc.getAVMStoreName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getVersion()
     */
    public int getVersion()
    {
        return vDesc.getVersionID();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getCreator()
     */
    public String getCreator()
    {
        return vDesc.getCreator();
    }
 
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getCreatedDate()
     */
    public Date getCreatedDate()
    {
        return new Date(vDesc.getCreateDate());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getLabel()
     */
    public String getLabel()
    {
        return vDesc.getTag();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#getDescription()
     */
    public String getDescription()
    {
        return vDesc.getDescription();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxVersion#isSystemGenerated()
     */
    public boolean isSystemGenerated()
    {
        return ((vDesc.getTag() == null) || (vDesc.getVersionID() == 0));
    }
}
