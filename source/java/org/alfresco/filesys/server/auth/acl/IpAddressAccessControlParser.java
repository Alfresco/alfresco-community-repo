/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
