/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.filesys.server.auth.passthru;

import org.alfresco.filesys.util.IPAddress;

/**
 * Subnet Domain Mapping Class
 *
 * @author gkspencer
 */
public class SubnetDomainMapping extends DomainMapping {

	// Subnet and mask for the domain
	
	private int m_subnet;
	private int m_mask;
	
	/**
	 * class constructor
	 * 
	 * @param domain String
	 * @param subnet int
	 * @param mask int
	 */
	public SubnetDomainMapping( String domain, int subnet, int mask)
	{
		super( domain);
		
		m_subnet = subnet;
		m_mask   = mask;
	}
	
	/**
	 * Return the subnet
	 * 
	 * @return int
	 */
	public final int getSubnet()
	{
		return m_subnet;
	}
	
	/**
	 * Return the subnet mask
	 * 
	 * @return int
	 */
	public final int getSubnetMask()
	{
		return m_mask;
	}
	
	/**
	 * Check if the client address is a member of this domain
	 * 
	 * @param clientIP int
	 * @return boolean
	 */
	public boolean isMemberOfDomain( int clientIP)
	{
		if (( clientIP & m_mask) == m_subnet)
			return true;
		return false;
	}
	
	/**
	 * Return the domain mapping as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		str.append(getDomain());
		str.append(",");
		str.append(IPAddress.asString( getSubnet()));
		str.append(":");
		str.append(IPAddress.asString( getSubnetMask()));
		str.append("]");
		
		return str.toString();
	}
}
