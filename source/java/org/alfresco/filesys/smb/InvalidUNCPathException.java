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

/**
 * Invalid UNC path exception class
 * <p>
 * The InvalidUNCPathException indicates that a UNC path has an invalid format.
 * 
 * @see PCShare
 */
public class InvalidUNCPathException extends Exception
{
    private static final long serialVersionUID = 3257567304241066297L;

    /**
     * Default invalid UNC path exception constructor.
     */

    public InvalidUNCPathException()
    {
    }

    /**
     * Invalid UNC path exception constructor, with additional details string.
     */

    public InvalidUNCPathException(String msg)
    {
        super(msg);
    }
}