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

/**
 * <p>
 * This exception may be thrown by a SharedDevice when the device interface has not been specified,
 * the device interface does not match the shared device type, or the device interface driver class
 * cannot be loaded.
 */
public class InvalidDeviceInterfaceException extends Exception
{
    private static final long serialVersionUID = 3834029177581222198L;

    /**
     * InvalidDeviceInterfaceException constructor.
     */
    public InvalidDeviceInterfaceException()
    {
        super();
    }

    /**
     * InvalidDeviceInterfaceException constructor.
     * 
     * @param s java.lang.String
     */
    public InvalidDeviceInterfaceException(String s)
    {
        super(s);
    }
}