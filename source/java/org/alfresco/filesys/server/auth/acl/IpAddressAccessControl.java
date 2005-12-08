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

import java.net.InetAddress;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.util.IPAddress;

/**
 * Ip Address Access Control Class
 * <p>
 * Allow/disallow access by checking for a particular TCP/IP address or checking that the address is
 * within a specified subnet.
 */
public class IpAddressAccessControl extends AccessControl
{

    // Subnet and network mask if the address specifies the subnet

    private String m_subnet;
    private String m_netMask;

    /**
     * Class constructor
     * 
     * @param address String
     * @param mask String
     * @param type String
     * @param access int
     */
    protected IpAddressAccessControl(String address, String mask, String type, int access)
    {
        super(address, type, access);

        // Save the subnet and network mask, if specified

        m_subnet = address;
        m_netMask = mask;

        // Change the rule name if a network mask has been specified

        if (m_netMask != null)
            setName(m_subnet + "/" + m_netMask);
    }

    /**
     * Check if the TCP/IP address matches the specifed address or is within the subnet.
     * 
     * @param sess SrvSession
     * @param share SharedDevice
     * @param mgr AccessControlManager
     * @return int
     */
    public int allowsAccess(SrvSession sess, SharedDevice share, AccessControlManager mgr)
    {

        // Check if the remote address is set for the session

        InetAddress remoteAddr = sess.getRemoteAddress();

        if (remoteAddr == null)
            return Default;

        // Get the remote address as a numeric IP address string

        String ipAddr = remoteAddr.getHostAddress();

        // Check if the access control is a single TCP/IP address check

        int sts = Default;

        if (m_netMask == null)
        {

            // Check if the TCP/IP address matches the check address

            if (IPAddress.parseNumericAddress(ipAddr) == IPAddress.parseNumericAddress(getName()))
                sts = getAccess();
        }
        else
        {

            // Check if the address is within the subnet range

            if (IPAddress.isInSubnet(ipAddr, m_subnet, m_netMask) == true)
                sts = getAccess();
        }

        // Return the access status

        return sts;
    }
}
