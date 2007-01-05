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
 * NFS Server Session Table Class
 * 
 * @author GKSpencer
 */
public class NFSSessionTable {

	//	Session list
	
	private Hashtable<Object, NFSSrvSession> m_sessions;
	
	/**
	 * Class constructor
	 */
	public NFSSessionTable()
	{
		m_sessions = new Hashtable<Object, NFSSrvSession>();
	}

	/**
	 * Return the number of sessions in the list
	 * 
	 * @return int
	 */
	public final int numberOfSessions()
	{
		return m_sessions.size();
	}

	/**
	 * Add a session to the list
	 * 
	 * @param sess NFSSrvSession
	 */
	public final void addSession(NFSSrvSession sess)
	{
		m_sessions.put(sess.getAuthIdentifier(), sess);
	}

	/**
	 * Find the session using the authentication identifier
	 * 
	 * @param authIdent Object
	 * @return NFSSrvSession
	 */
	public final NFSSrvSession findSession(Object authIdent)
	{
		return (NFSSrvSession) m_sessions.get(authIdent);
	}

	/**
	 * Remove a session from the list
	 * 
	 * @param sess NFSSrvSession
	 * @return NFSSrvSession
	 */
	public final NFSSrvSession removeSession(NFSSrvSession sess)
	{
		return removeSession(sess.getAuthIdentifier());
	}

	/**
	 * Remove a session from the list
	 * 
	 * @param authIdent Object
	 * @return NFSSrvSession
	 */
	public final NFSSrvSession removeSession(Object authIdent)
	{

		//	Find the required session

		NFSSrvSession sess = findSession(authIdent);

		//	Remove the session and return the removed session

		m_sessions.remove(authIdent);
		return sess;
	}

	/**
	 * Enumerate the session ids
	 * 
	 * @return Enumeration
	 */
	public final Enumeration<Object> enumerate()
	{
		return m_sessions.keys();
	}
}
