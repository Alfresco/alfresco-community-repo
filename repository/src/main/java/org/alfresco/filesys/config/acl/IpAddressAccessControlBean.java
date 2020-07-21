/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.IpAddressAccessControl;

/**
 * Simple description of a JLAN IP Address Access control that can be configured via JMX or a Spring bean definition.
 */
public class IpAddressAccessControlBean extends AccessControlBean
{
    /** The subnet. */
    private String subnet;

    /** The network mask. */
    private String netMask;

    /**
     * Sets the subnet.
     * 
     * @param subnet
     *            the subnet
     */
    public void setSubnet(String subnet)
    {
        this.subnet = subnet;
    }

    /**
     * Sets the network mask.
     * 
     * @param mask
     *            the network mask
     */
    public void setNetMask(String mask)
    {
        this.netMask = mask;
    }

    /**
     * Gets the subnet.
     * 
     * @return the subnet
     */
    public String getSubnet()
    {
        return this.subnet;
    }

    /**
     * Gets the network mask.
     * 
     * @return the network mask
     */
    public String getNetMask()
    {
        return this.netMask;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.filesys.config.acl.AccessControlBean#toAccessControl()
     */
    @Override
    public AccessControl toAccessControl()
    {
        return new IpAddressAccessControl(getSubnet(), getNetMask(), "address", getAccessLevel());
    }

}
