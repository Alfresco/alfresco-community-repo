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
import org.alfresco.jlan.server.auth.acl.GidAccessControl;

/**
 * Simple description of a JLAN Group ID Access control that can be configured via JMX or a Spring bean definition.
 */
public class GidAccessControlBean extends AccessControlBean
{
    /** The group id to check for. */
    private int gid;

    /**
     * Sets the group id to check for
     * 
     * @param gid
     *            the group id to check for
     */
    public final void setGid(int gid)
    {
        this.gid = gid;
    }

    /**
     * Gets the group id to check for.
     * 
     * @return the group id to check for
     */
    public final int getGid()
    {
        return this.gid;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new GidAccessControl(getName(), getGid(), "gid", getAccessLevel());
    }
}
