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
