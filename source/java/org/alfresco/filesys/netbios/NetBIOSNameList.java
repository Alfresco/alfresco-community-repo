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
package org.alfresco.filesys.netbios;

import java.util.Vector;

/**
 * NetBIOS Name List Class
 */
public class NetBIOSNameList
{

    // List of NetBIOS names

    private Vector<NetBIOSName> m_nameList;

    /**
     * Class constructor
     */
    public NetBIOSNameList()
    {
        m_nameList = new Vector<NetBIOSName>();
    }

    /**
     * Add a name to the list
     * 
     * @param name NetBIOSName
     */
    public final void addName(NetBIOSName name)
    {
        m_nameList.add(name);
    }

    /**
     * Get a name from the list
     * 
     * @param idx int
     * @return NetBIOSName
     */
    public final NetBIOSName getName(int idx)
    {
        if (idx < m_nameList.size())
            return m_nameList.get(idx);
        return null;
    }

    /**
     * Return the number of names in the list
     * 
     * @return int
     */
    public final int numberOfNames()
    {
        return m_nameList.size();
    }

    /**
     * Find names of the specified name of different types and return a subset of the available
     * names.
     * 
     * @param name String
     * @return NetBIOSNameList
     */
    public final NetBIOSNameList findNames(String name)
    {

        // Allocate the sub list and search for required names

        NetBIOSNameList subList = new NetBIOSNameList();
        for (int i = 0; i < m_nameList.size(); i++)
        {
            NetBIOSName nbName = getName(i);
            if (nbName.getName().compareTo(name) == 0)
                subList.addName(nbName);
        }

        // Return the sub list of names

        return subList;
    }

    /**
     * Find the first name of the specified type
     * 
     * @param typ char
     * @param group boolean
     * @return NetBIOSName
     */
    public final NetBIOSName findName(char typ, boolean group)
    {

        // Search for the first name of the required type

        for (int i = 0; i < m_nameList.size(); i++)
        {
            NetBIOSName name = getName(i);
            if (name.getType() == typ && name.isGroupName() == group)
                return name;
        }

        // Name type not found

        return null;
    }

    /**
     * Find the specified name and type
     * 
     * @param name String
     * @param typ char
     * @param group boolean
     * @return NetBIOSName
     */
    public final NetBIOSName findName(String name, char typ, boolean group)
    {

        // Search for the first name of the required type

        for (int i = 0; i < m_nameList.size(); i++)
        {
            NetBIOSName nbName = getName(i);
            if (nbName.getName().equals(name) && nbName.getType() == typ && nbName.isGroupName() == group)
                return nbName;
        }

        // Name/type not found

        return null;
    }

    /**
     * Find names of the specified type and return a subset of the available names
     * 
     * @param typ char
     * @param group boolean
     * @return NetBIOSNameList
     */
    public final NetBIOSNameList findNames(char typ, boolean group)
    {

        // Allocate the sub list and search for names of the required type

        NetBIOSNameList subList = new NetBIOSNameList();
        for (int i = 0; i < m_nameList.size(); i++)
        {
            NetBIOSName name = getName(i);
            if (name.getType() == typ && name.isGroupName() == group)
                subList.addName(name);
        }

        // Return the sub list of names

        return subList;
    }

    /**
     * Remove a name from the list
     * 
     * @param name NetBIOSName
     * @return NetBIOSName
     */
    public final NetBIOSName removeName(NetBIOSName name)
    {
        for (int i = 0; i < m_nameList.size(); i++)
        {
            NetBIOSName curName = getName(i);
            if (curName.equals(name))
            {
                m_nameList.removeElementAt(i);
                return curName;
            }
        }
        return null;
    }

    /**
     * Delete all names from the list
     */
    public final void removeAllNames()
    {
        m_nameList.removeAllElements();
    }
}
