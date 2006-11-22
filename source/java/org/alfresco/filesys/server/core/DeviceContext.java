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
package org.alfresco.filesys.server.core;

/**
 * <p>
 * The device context is passed to the methods of a device interface. Each shared device has a
 * device interface and a device context associated with it. The device context allows a single
 * device interface to be used for multiple shared devices.
 */
public class DeviceContext
{

    // Device name that the interface is associated with

    private String m_devName;

    // Filesystem name
    
    private String m_filesysName;
    
    // Flag to indicate if the device is available. Unavailable devices will not be listed by the
    // various protocol servers.

    private boolean m_available = true;

    /**
     * Default constructor
     */
    public DeviceContext()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param filesysName String
     * @param devName String
     */
    public DeviceContext(String filesysName, String devName)
    {
    	m_filesysName = filesysName;
        m_devName     = devName;
    }

    /**
     * Return the device name.
     * 
     * @return String
     */
    public final String getDeviceName()
    {
        return m_devName;
    }

    /**
     * Return the filesystem name
     * 
     * @return String
     */
    public final String getFilesystemName()
    {
    	return m_filesysName;
    }
    
    /**
     * Determine if the filesystem is available
     * 
     * @return boolean
     */
    public final boolean isAvailable()
    {
        return m_available;
    }

    /**
     * Set the filesystem as available, or not
     * 
     * @param avail boolean
     */
    public final void setAvailable(boolean avail)
    {
        m_available = avail;
    }

    /**
     * Set the device name.
     * 
     * @param name String
     */
    public final void setDeviceName(String name)
    {
        m_devName = name;
    }

    /**
     * Set the filesystem name
     * 
     * @param filesysName String
     */
    public final void setFilesystemName( String filesysName)
    {
    	m_filesysName = filesysName;
    }
    
    /**
     * Close the device context, free any resources allocated by the context
     */
    public void CloseContext()
    {
    }

    /**
     * Return the context as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getFilesystemName());
        str.append(",");
        str.append(getDeviceName());
        str.append("]");

        return str.toString();
    }
}