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
package org.alfresco.filesys.smb;

import java.util.BitSet;

/**
 * SMB dialect selector class.
 */
public class DialectSelector
{

    // Bit set of selected SMB dialects.

    private BitSet dialects;

    /**
     * Construct a new SMB dialect selector with the SMB core protocol selected.
     */

    public DialectSelector()
    {
        dialects = new BitSet(Dialect.Max);

        // Select only the core protocol by default

        ClearAll();
        AddDialect(Dialect.Core);
    }

    /**
     * Add a dialect to the list of available SMB dialects.
     * 
     * @param idx Index of the dialect to add to the available dialects.
     * @exception java.lang.ArrayIndexOutOfBoundsException Invalid dialect index.
     */

    public void AddDialect(int d) throws java.lang.ArrayIndexOutOfBoundsException
    {
        dialects.set(d);
    }

    /**
     * Clear all the dialect bits.
     */

    public void ClearAll()
    {
        for (int i = 0; i < dialects.size(); dialects.clear(i++))
            ;
    }

    /**
     * Copy the SMB dialect selector settings.
     * 
     * @param dsel DialectSelector
     */
    public void copyFrom(DialectSelector dsel)
    {

        // Clear all current settings

        ClearAll();

        // Copy the settings

        for (int i = 0; i < Dialect.Max; i++)
        {

            // Check if the dialect is enabled

            if (dsel.hasDialect(i))
                AddDialect(i);
        }
    }

    /**
     * Determine if the specified SMB dialect is selected/available.
     * 
     * @param idx Index of the dialect to test for.
     * @return true if the SMB dialect is available, else false.
     * @exception java.lang.ArrayIndexOutOfBoundsException Invalid dialect index.
     */

    public boolean hasDialect(int d) throws java.lang.ArrayIndexOutOfBoundsException
    {
        return dialects.get(d);
    }

    /**
     * Determine if the core SMB dialect is enabled
     * 
     * @return boolean
     */
    public boolean hasCore()
    {
        if (hasDialect(Dialect.Core) || hasDialect(Dialect.CorePlus))
            return true;
        return false;
    }

    /**
     * Determine if the LanMan SMB dialect is enabled
     * 
     * @return boolean
     */
    public boolean hasLanMan()
    {
        if (hasDialect(Dialect.DOSLanMan1) || hasDialect(Dialect.DOSLanMan2) || hasDialect(Dialect.LanMan1)
                || hasDialect(Dialect.LanMan2) || hasDialect(Dialect.LanMan2_1))
            return true;
        return false;
    }

    /**
     * Determine if the NT SMB dialect is enabled
     * 
     * @return boolean
     */
    public boolean hasNT()
    {
        if (hasDialect(Dialect.NT))
            return true;
        return false;
    }

    /**
     * Remove an SMB dialect from the list of available dialects.
     * 
     * @param idx Index of the dialect to remove.
     * @exception java.lang.ArrayIndexOutOfBoundsException Invalid dialect index.
     */

    public void RemoveDialect(int d) throws java.lang.ArrayIndexOutOfBoundsException
    {
        dialects.clear(d);
    }

    /**
     * Return the dialect selector list as a string.
     * 
     * @return java.lang.String
     */
    public String toString()
    {

        // Create a string buffer to build the return string

        StringBuffer str = new StringBuffer();
        str.append("[");

        for (int i = 0; i < dialects.size(); i++)
        {
            if (hasDialect(i))
            {
                str.append(Dialect.DialectTypeString(i));
                str.append(",");
            }
        }

        // Trim the last comma and return the string

        if (str.length() > 1)
            str.setLength(str.length() - 1);
        str.append("]");
        return str.toString();
    }
}