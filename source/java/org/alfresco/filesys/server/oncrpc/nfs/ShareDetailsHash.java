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
package org.alfresco.filesys.server.oncrpc.nfs;

import java.util.*;

/**
 * Share Details Hash Class
 * 
 * <p>Hashtable of ShareDetails for the available disk shared devices. ShareDetails are indexed using the
 * hash of the share name to allow mounts to be persistent across server restarts.
 * 
 * @author GKSpencer
 */
public class ShareDetailsHash {

	//	Share name hash to share details
	
	private Hashtable<Integer, ShareDetails> m_details;
	
	/**
	 * Class constructor
	 */
	public ShareDetailsHash()
	{
		m_details = new Hashtable<Integer, ShareDetails>();
	}

	/**
	 * Add share details to the list of available shares
	 * 
	 * @param details ShareDetails 
	 */
	public final void addDetails(ShareDetails details)
	{
		m_details.put(new Integer(details.getName().hashCode()), details);
	}

	/**
	 * Delete share details from the list
	 *
	 * @param shareName String
	 * @return ShareDetails 
	 */
	public final ShareDetails deleteDetails(String shareName)
	{
		return (ShareDetails) m_details.get(new Integer(shareName.hashCode()));
	}

	/**
	 * Find share details for the specified share name
	 * 
	 * @param shareName String
	 * @return ShareDetails
	 */
	public final ShareDetails findDetails(String shareName)
	{

		//	Get the share details for the associated share name

		ShareDetails details = (ShareDetails) m_details.get(new Integer(shareName.hashCode()));

		//	Return the share details

		return details;
	}

	/**
	 * Find share details for the specified share name hash code
	 *
	 * @param hashCode int
	 * @return ShareDetails 
	 */
	public final ShareDetails findDetails(int hashCode)
	{

		//	Get the share details for the associated share name

		ShareDetails details = (ShareDetails) m_details.get(new Integer(hashCode));

		//	Return the share details

		return details;
	}
}
