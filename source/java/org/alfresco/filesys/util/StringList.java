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
package org.alfresco.filesys.util;

import java.util.Vector;

/**
 * String List Class
 */
public class StringList
{

    // List of strings

    private Vector<String> m_list;

    /**
     * Default constructor
     */
    public StringList()
    {
        m_list = new Vector<String>();
    }

    /**
     * Return the number of strings in the list
     * 
     * @return int
     */
    public final int numberOfStrings()
    {
        return m_list.size();
    }

    /**
     * Add a string to the list
     * 
     * @param str String
     */
    public final void addString(String str)
    {
        m_list.add(str);
    }

    /**
     * Add a list of strings to this list
     * 
     * @param list StringList
     */
    public final void addStrings(StringList list)
    {
        if (list != null && list.numberOfStrings() > 0)
            for (int i = 0; i < list.numberOfStrings(); m_list.add(list.getStringAt(i++)))
                ;
    }

    /**
     * Return the string at the specified index
     * 
     * @param idx int
     * @return String
     */
    public final String getStringAt(int idx)
    {
        if (idx < 0 || idx >= m_list.size())
            return null;
        return (String) m_list.elementAt(idx);
    }

    /**
     * Check if the list contains the specified string
     * 
     * @param str String
     * @return boolean
     */
    public final boolean containsString(String str)
    {
        return m_list.contains(str);
    }

    /**
     * Return the index of the specified string, or -1 if not in the list
     * 
     * @param str String
     * @return int
     */
    public final int findString(String str)
    {
        return m_list.indexOf(str);
    }

    /**
     * Remove the specified string from the list
     * 
     * @param str String
     * @return boolean
     */
    public final boolean removeString(String str)
    {
        return m_list.removeElement(str);
    }

    /**
     * Remove the string at the specified index within the list
     * 
     * @param idx int
     * @return String
     */
    public final String removeStringAt(int idx)
    {
        if (idx < 0 || idx >= m_list.size())
            return null;
        String ret = (String) m_list.elementAt(idx);
        m_list.removeElementAt(idx);
        return ret;
    }

    /**
     * Clear the strings from the list
     */
    public final void remoteAllStrings()
    {
        m_list.removeAllElements();
    }

    /**
     * Return the string list as a string
     * 
     * @return String
     */
    public String toString()
    {

        // Check if the list is empty

        if (numberOfStrings() == 0)
            return "";

        // Build the string

        StringBuffer str = new StringBuffer();

        for (int i = 0; i < m_list.size(); i++)
        {
            str.append(getStringAt(i));
            str.append(",");
        }

        // Remove the trailing comma

        if (str.length() > 0)
            str.setLength(str.length() - 1);

        // Return the string

        return str.toString();
    }
}
