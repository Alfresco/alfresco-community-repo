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
 * This error is generated when a tree connection has no free file slots. The new file open request
 * will be rejected by the server.
 */
public class TooManyFilesException extends Exception
{
    private static final long serialVersionUID = 4051332218943060273L;

    /**
     * TooManyFilesException constructor.
     */
    public TooManyFilesException()
    {
        super();
    }

    /**
     * TooManyFilesException constructor.
     * 
     * @param s java.lang.String
     */
    public TooManyFilesException(String s)
    {
        super(s);
    }
}