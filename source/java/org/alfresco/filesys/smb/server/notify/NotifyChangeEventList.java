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
package org.alfresco.filesys.smb.server.notify;

import java.util.Vector;

/**
 * Notify Change Event List Class
 */
public class NotifyChangeEventList
{

    // List of notify events

    private Vector<NotifyChangeEvent> m_list;

    /**
     * Default constructor
     */
    public NotifyChangeEventList()
    {
        m_list = new Vector<NotifyChangeEvent>();
    }

    /**
     * Return the count of notify events
     * 
     * @return int
     */
    public final int numberOfEvents()
    {
        return m_list.size();
    }

    /**
     * Return the specified change event
     * 
     * @param idx int
     * @return NotifyChangeEvent
     */
    public final NotifyChangeEvent getEventAt(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_list.size())
            return null;

        // Return the required notify event

        return m_list.get(idx);
    }

    /**
     * Add a change event to the list
     * 
     * @param evt NotifyChangeEvent
     */
    public final void addEvent(NotifyChangeEvent evt)
    {
        m_list.add(evt);
    }

    /**
     * Remove the specified change event
     * 
     * @param idx int
     * @return NotifyChangeEvent
     */
    public final NotifyChangeEvent removeEventAt(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_list.size())
            return null;

        // Return the required notify event

        return m_list.remove(idx);
    }

    /**
     * Remove all events from the list
     */
    public final void removeAllEvents()
    {
        m_list.removeAllElements();
    }
}
