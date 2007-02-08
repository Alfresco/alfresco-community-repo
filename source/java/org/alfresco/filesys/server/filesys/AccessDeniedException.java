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
 * <p>
 * Thrown when an attempt is made to write to a file that is read-only or the user only has read
 * access to, or open a file that is actually a directory.
 */
public class AccessDeniedException extends java.io.IOException
{
    private static final long serialVersionUID = 3688785881968293433L;

    /**
     * AccessDeniedException constructor
     */
    public AccessDeniedException()
    {
        super();
    }

    /**
     * AccessDeniedException constructor.
     * 
     * @param s java.lang.String
     */
    public AccessDeniedException(String s)
    {
        super(s);
    }
}