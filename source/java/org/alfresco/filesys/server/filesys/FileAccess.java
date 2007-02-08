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
package org.alfresco.filesys.server.filesys;

/**
 * File Access Class
 * <p>
 * Contains a list of the available file permissions that may be applied to a share, directory or
 * file.
 */
public final class FileAccess
{
    // Permissions

    public static final int NoAccess = 0;
    public static final int ReadOnly = 1;
    public static final int Writeable = 2;

    /**
     * Return the file permission as a string.
     * 
     * @param perm int
     * @return java.lang.String
     */
    public final static String asString(int perm)
    {
        String permStr = "";

        switch (perm)
        {
        case NoAccess:
            permStr = "NoAccess";
            break;
        case ReadOnly:
            permStr = "ReadOnly";
            break;
        case Writeable:
            permStr = "Writeable";
            break;
        }
        return permStr;
    }
}