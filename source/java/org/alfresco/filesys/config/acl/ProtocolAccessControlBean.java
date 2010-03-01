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
