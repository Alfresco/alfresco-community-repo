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
