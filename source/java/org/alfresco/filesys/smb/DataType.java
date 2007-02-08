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
 * SMB data type class.
 * <p>
 * This class contains the data types that are used within an SMB protocol packet.
 */

public class DataType
{

    // SMB data types

    public static final char DataBlock = (char) 0x01;
    public static final char Dialect = (char) 0x02;
    public static final char Pathname = (char) 0x03;
    public static final char ASCII = (char) 0x04;
    public static final char VariableBlock = (char) 0x05;
}