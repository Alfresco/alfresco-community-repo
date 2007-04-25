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

/**
 * Domain Mapping Class
 *
 * @author gkspencer
 */
public abstract class DomainMapping {

	// Domain name
	
	private String m_domain;
	
	/**
	 * Class consructor
	 * 
	 * @param domain String
	 */
	public DomainMapping( String domain)
	{
		m_domain = domain;
	}
	
	/**
	 * Return the domain name
	 * 
	 * @return String
	 */
	public final String getDomain()
	{
		return m_domain;
	}
	
	/**
	 * Check if the client address is a member of this domain
	 * 
	 * @param clientIP int
	 * @return boolean
	 */
	public abstract boolean isMemberOfDomain( int clientIP);
}
