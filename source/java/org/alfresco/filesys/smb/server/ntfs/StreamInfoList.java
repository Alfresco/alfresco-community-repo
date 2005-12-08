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
