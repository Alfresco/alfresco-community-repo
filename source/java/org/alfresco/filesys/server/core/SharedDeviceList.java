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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.core;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <p>
 * List of shared devices.
 */
public class SharedDeviceList
{

    // Shared device list

    private Hashtable<String, SharedDevice> m_shares;

    /**
     * SharedDeviceList constructor.
     */
    public SharedDeviceList()
    {

        // Allocate the shared device list

        m_shares = new Hashtable<String, SharedDevice>();
    }

    /**
     * Copy constructor
     * 
     * @param shrList SharedDeviceList
     */
    public SharedDeviceList(SharedDeviceList shrList)
    {

        // Allocate the shared device list

        m_shares = new Hashtable<String, SharedDevice>();

        // Copy the shares from the original list, shallow copy

        addShares(shrList);
    }

    /**
     * Add a shared device to the list.
     * 
     * @param shr Shared device to be added to the list.
     * @return True if the share was added successfully, else false.
     */
    public final boolean addShare(SharedDevice shr)
    {

        // Check if a share with the specified name already exists

        if (m_shares.containsKey(shr.getName()))
            return false;

        // Add the shared device

        m_shares.put(shr.getName(), shr);
        return true;
    }

    /**
     * Add shares from the specified list to this list, using a shallow copy
     * 
     * @param shrList SharedDeviceList
     */
    public final void addShares(SharedDeviceList shrList)
    {

        // Copy the shares to this list

        Enumeration<SharedDevice> enm = shrList.enumerateShares();

        while (enm.hasMoreElements())
            addShare(enm.nextElement());
    }

    /**
     * Delete the specified shared device from the list.
     * 
     * @param name String Name of the shared resource to remove from the list.
     * @return SharedDevice that has been removed from the list, else null.
     */
    public final SharedDevice deleteShare(String name)
    {

        // Remove the shared device from the list

        return (SharedDevice) m_shares.remove(name);
    }

    /**
     * Return an enumeration to allow the shared devices to be listed.
     * 
     * @return Enumeration<SharedDevice>
     */
    public final Enumeration<SharedDevice> enumerateShares()
    {
        return m_shares.elements();
    }

    /**
     * Find the shared device with the specified name.
     * 
     * @param name Name of the shared device to find.
     * @return SharedDevice with the specified name, else null.
     */
    public final SharedDevice findShare(String name)
    {
        return m_shares.get(name);
    }

    /**
     * Find the shared device with the specified name and type
     * 
     * @param name Name of shared device to find
     * @param typ Type of shared device (see ShareType)
     * @param nocase Case sensitive search if false, else case insensitive search
     * @return SharedDevice with the specified name and type, else null
     */
    public final SharedDevice findShare(String name, int typ, boolean nocase)
    {

        // Enumerate the share list

        Enumeration<String> keys = m_shares.keys();

        while (keys.hasMoreElements())
        {

            // Get the current share name

            String curName = keys.nextElement();

            if ((nocase == false && curName.equals(name)) || (nocase == true && curName.equalsIgnoreCase(name)))
            {

                // Get the shared device and check if the share is of the required type

                SharedDevice share = (SharedDevice) m_shares.get(curName);
                if (share.getType() == typ || typ == ShareType.UNKNOWN)
                    return share;
            }
        }

        // Required share not found

        return null;
    }

    /**
     * Return the number of shared devices in the list.
     * 
     * @return int
     */
    public final int numberOfShares()
    {
        return m_shares.size();
    }

    /**
     * Remove shares that have an unavailable status from the list
     * 
     * @return int
     */
    public final int removeUnavailableShares()
    {

        // Check if any shares are unavailable

        Enumeration<SharedDevice> shrEnum = enumerateShares();
        int remCnt = 0;

        while (shrEnum.hasMoreElements())
        {

            // Check if the current share is unavailable

            SharedDevice shr = shrEnum.nextElement();
            if (shr.getContext() != null && shr.getContext().isAvailable() == false)
            {
                deleteShare(shr.getName());
                remCnt++;
            }
        }

        // Return the count of shares removed

        return remCnt;
    }

    /**
     * Remove all shared devices from the share list
     */
    public final void removeAllShares()
    {
        m_shares.clear();
    }
    
    /**
     * Return the share list as a string
     * 
     * @return String
     */
    public String toString()
    {

        // Create a buffer to build the string

        StringBuffer str = new StringBuffer();
        str.append("[");

        // Enumerate the shares

        Enumeration<String> enm = m_shares.keys();

        while (enm.hasMoreElements())
        {
            String name = enm.nextElement();
            str.append(name);
            str.append(",");
        }

        // Remove the trailing comma

        if (str.length() > 1)
            str.setLength(str.length() - 1);
        str.append("]");

        // Return the string

        return str.toString();
    }
}