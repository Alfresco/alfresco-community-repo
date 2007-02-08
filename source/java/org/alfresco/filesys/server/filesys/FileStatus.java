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
 * File Status Class
 */
public class FileStatus
{

    // File status constants

    public final static int Unknown = -1;
    public final static int NotExist = 0;
    public final static int FileExists = 1;
    public final static int DirectoryExists = 2;

    /**
     * Return the file status as a string
     * 
     * @param sts int
     * @return String
     */
    public final static String asString(int sts)
    {

        // Convert the status to a string

        String ret = "";

        switch (sts)
        {
        case Unknown:
            ret = "Unknown";
            break;
        case NotExist:
            ret = "NotExist";
            break;
        case FileExists:
            ret = "FileExists";
            break;
        case DirectoryExists:
            ret = "DirExists";
            break;
        }

        return ret;
    }
}
