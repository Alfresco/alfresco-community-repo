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
package org.alfresco.filesys.smb.server.ntfs;

import java.util.Vector;

/**
 * Stream Information List Class
 */
public class StreamInfoList
{

    // List of stream information objects

    private Vector<StreamInfo> m_list;

    /**
     * Default constructor
     */
    public StreamInfoList()
    {
        m_list = new Vector<StreamInfo>();
    }

    /**
     * Add an item to the list
     * 
     * @param info StreamInfo
     */
    public final void addStream(StreamInfo info)
    {
        m_list.add(info);
    }

    /**
     * Return the stream details at the specified index
     * 
     * @param idx int
     * @return StreamInfo
     */
    public final StreamInfo getStreamAt(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_list.size())
            return null;

        // Return the required stream information

        return m_list.get(idx);
    }

    /**
     * Find a stream by name
     * 
     * @param name String
     * @return StreamInfo
     */
    public final StreamInfo findStream(String name)
    {

        // Search for the required stream

        for (int i = 0; i < m_list.size(); i++)
        {

            // Get the current stream information

            StreamInfo sinfo = m_list.get(i);

            // Check if the stream name matches

            if (sinfo.getName().equals(name))
                return sinfo;
        }

        // Stream not found

        return null;
    }

    /**
     * Return the count of streams in the list
     * 
     * @return int
     */
    public final int numberOfStreams()
    {
        return m_list.size();
    }

    /**
     * Remove the specified stream from the list
     * 
     * @param idx int
     * @return StreamInfo
     */
    public final StreamInfo removeStream(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_list.size())
            return null;

        // Remove the required stream

        return m_list.remove(idx);
    }

    /**
     * Remove the specified stream from the list
     * 
     * @param name String
     * @return StreamInfo
     */
    public final StreamInfo removeStream(String name)
    {

        // Search for the required stream

        for (int i = 0; i < m_list.size(); i++)
        {

            // Get the current stream information

            StreamInfo sinfo = m_list.get(i);

            // Check if the stream name matches

            if (sinfo.getName().equals(name))
            {

                // Remove the stream from the list

                m_list.removeElementAt(i);
                return sinfo;
            }
        }

        // Stream not found

        return null;
    }

    /**
     * Remove all streams from the list
     */
    public final void removeAllStreams()
    {
        m_list.removeAllElements();
    }
}
