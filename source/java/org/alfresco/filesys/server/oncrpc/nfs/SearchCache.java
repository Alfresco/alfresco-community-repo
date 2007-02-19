/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc.nfs;

import org.alfresco.filesys.server.filesys.SearchContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Search Cache Class
 * 
 * <p>Holds the details of the active searches for the NFS server
 * 
 * @author GKSpencer
 */
public class SearchCache {

	// Debug logging

	private static final Log logger = LogFactory.getLog(SearchCache.class);
	
	// Maximum number of active searches

	public static final int MaximumSearches = 255;

	// Default search timeout

	public static final long DefaultSearchTimeout = 30000L; // 30 seconds

	// Array of active searches, last allocated index

	private SearchEntry[] m_searches;
	private int m_lastIdx;

	// Search timeout

	private long m_searchTmo = DefaultSearchTimeout;

	// Debug enable flag

	private boolean m_debug = true;

	/**
	 * Search Entry Class
	 */
	protected class SearchEntry
	{

		// Search context

		private SearchContext m_search;

		// Search timeout

		private long m_timeout;

		/**
		 * Class constructor
		 * 
		 * @param search SearchContext
		 */
		public SearchEntry(SearchContext search)
		{
			m_search = search;
			updateTimeout();
		}

		/**
		 * Return the search timeout
		 * 
		 * @return long
		 */
		public final long getTimeout()
		{
			return m_timeout;
		}

		/**
		 * Return the search context
		 * 
		 * @return SearchContext
		 */
		public final SearchContext getSearch()
		{
			return m_search;
		}

		/**
		 * Update the search timeout
		 */
		public final void updateTimeout()
		{
			m_timeout = System.currentTimeMillis() + m_searchTmo;
		}
	};

	/**
	 * Search Expiry Thread Class
	 */
	protected class SearchExpiry implements Runnable
	{

		// Expiry thread

		private Thread m_thread;

		// Wakeup interval

		private long m_wakeup;

		/**
		 * Class Constructor
		 * 
		 * @param wakeup long
		 */
		public SearchExpiry(long wakeup)
		{

			// Set the wakeup interval

			m_wakeup = wakeup;

			// Create and start the search expiry thread

			m_thread = new Thread(this);
			m_thread.setDaemon(true);
			m_thread.setName("NFSSearchExpiry");
			m_thread.start();
		}

		/**
		 * Main thread method
		 */
		public void run()
		{

			// Loop until shutdown

			while (true)
			{

				// Sleep for a while

				try
				{
					Thread.sleep(m_wakeup);
				} catch (InterruptedException ex)
				{
				}

				// Get the current system time

				long timeNow = System.currentTimeMillis();

				// Check for expired searches

				synchronized (m_searches)
				{

					// Check all allocated slots

					for (int i = 0; i < m_searches.length; i++)
					{

						// Check if the current slot has a valid entry

						if (m_searches[i] != null && m_searches[i].getTimeout() < timeNow)
						{

							// Remove the current search entry

							SearchEntry entry = m_searches[i];
							m_searches[i] = null;

							// Close the search

							entry.getSearch().closeSearch();

							// DEBUG

							if (logger.isDebugEnabled())
								logger.debug("NFSSearchExpiry: Closed search=" + entry.getSearch().getSearchString()
										+ ", id=" + i);
						}
					}
				}
			}
		}
	};

	/**
	 * Default constructor
	 */
	public SearchCache()
	{

		// Create the active search list

		m_searches = new SearchEntry[MaximumSearches];

		// Start the search expiry thread

		new SearchExpiry(DefaultSearchTimeout / 2);
	}

	/**
	 * Determine if debug output is enabled
	 * 
	 * @return boolean
	 */
	public final boolean hasDebug()
	{
		return m_debug;
	}

	/**
	 * Allocate a search slot
	 * 
	 * @param search SearchContext
	 * @return int
	 */
	public final int allocateSearchId(SearchContext search)
	{

		synchronized (m_searches)
		{

			// Search for a free slot in the search list

			int cnt = 0;

			while (cnt < MaximumSearches)
			{

				// Check if the index has wrapped

				if (m_lastIdx >= MaximumSearches)
					m_lastIdx = 0;

				// Check if the current slot is empty

				if (m_searches[m_lastIdx] == null)
				{

					// Use this slot

					SearchEntry entry = new SearchEntry(search);
					m_searches[m_lastIdx] = entry;
					return m_lastIdx++;
				} else
					m_lastIdx++;

				// Update the slot count

				cnt++;
			}
		}

		// No empty search slot found

		return -1;
	}

	/**
	 * Release a search slot
	 * 
	 * @param id int
	 */
	public final void releaseSearchId(int id)
	{

		// Range check the id

		if (id < 0 || id >= MaximumSearches)
			return;

		// Delete the search entry

		synchronized (m_searches)
		{
			m_searches[id] = null;
		}
	}

	/**
	 * Return the required search context
	 * 
	 * @param id int
	 * @return SearchContext
	 */
	public final SearchContext getSearch(int id)
	{

		// Range check the id

		if (id < 0 || id >= MaximumSearches)
			return null;

		// Get the search entry

		SearchEntry entry = null;

		synchronized (m_searches)
		{
			entry = m_searches[id];
		}

		// Return the search context, if valid

		if (entry != null)
		{

			// Update the search timeout and return the search

			entry.updateTimeout();
			return entry.getSearch();
		}

		// Invalid search

		return null;
	}

	/**
	 * Dump the active search list
	 */
	public final void dumpSearches()
	{

		synchronized (m_searches)
		{

			// Find all active searches in the list

			for (int i = 0; i < m_searches.length; i++)
			{

				// Check if the current search slot is active

				if (m_searches[i] != null)
				{

					// Get the search details

					SearchEntry entry = m_searches[i];

					logger.debug("" + i + ": " + entry.getSearch().toString());
				}
			}
		}
	}
}
