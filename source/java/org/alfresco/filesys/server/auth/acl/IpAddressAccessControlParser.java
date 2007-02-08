/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.auth.acl;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.util.IPAddress;

/**
 * Ip Address Access Control Parser Class
 */
public class IpAddressAccessControlParser extends AccessControlParser
{

    /**
     * Default constructor
     */
    public IpAddressAccessControlParser()
    {
    }

    /**
     * Return the parser type
     * 
     * @return String
     */
    public String getType()
    {
        return "address";
    }

    /**
     * Validate the parameters and create an address access control
     * 
     * @param params ConfigElement
     * @return AccessControl
     * @throws ACLParseException
     */
    public AccessControl createAccessControl(ConfigElement params) throws ACLParseException
    {

        // Get the access type

        int access = parseAccessType(params);

        // Check if the single IP address format has been specified

        String ipAddr = params.getAttribute("ip");
        if (ipAddr != null)
        {

            // Validate the parameters

            if (ipAddr.length() == 0 || IPAddress.isNumericAddress(ipAddr) == false)
                throw new ACLParseException("Invalid IP address, " + ipAddr);

            if (params.getAttributeCount() != 2)
                throw new ACLParseException("Invalid parameter(s) specified for address");

            // Create a single TCP/IP address access control rule

            return new IpAddressAccessControl(ipAddr, null, getType(), access);
        }

        // Check if a subnet address and mask have been specified

        String subnet = params.getAttribute("subnet");
        if (subnet != null)
        {

            // Get the network mask parameter

            String netmask = params.getAttribute("mask");

            // Validate the parameters

            if (subnet.length() == 0 || netmask == null || netmask.length() == 0)
                throw new ACLParseException("Invalid subnet/mask parameter");

            if (IPAddress.isNumericAddress(subnet) == false)
                throw new ACLParseException("Invalid subnet parameter, " + subnet);

            if (IPAddress.isNumericAddress(netmask) == false)
                throw new ACLParseException("Invalid mask parameter, " + netmask);

            // Create a subnet address access control rule

            return new IpAddressAccessControl(subnet, netmask, getType(), access);
        }

        // Invalid parameters

        throw new ACLParseException("Unknown address parameter(s)");
    }
}
