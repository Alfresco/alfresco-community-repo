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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
