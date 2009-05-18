/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.ProtocolAccessControl;

/**
 * Simple description of a JLAN Protocol Access control that can be configured via JMX or a Spring bean definition.
 */
public class ProtocolAccessControlBean extends AccessControlBean
{
    /** The list of protocol types. */
    private String checkList;

    /**
     * Sets the list of protocol types.
     * 
     * @param protList
     *            the list of protocol types
     */
    public void setCheckList(String protList)
    {
        this.checkList = protList;
    }

    /**
     * Gets the list of protocol types
     * 
     * @return the list of protocol types
     */
    public String getCheckList()
    {
        return this.checkList;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new ProtocolAccessControl(getCheckList(), "protocol", getAccessLevel());
    }
}
