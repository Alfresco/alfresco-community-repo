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
package org.alfresco.filesys.smb.server;

/**
 * OpenAndX Flags Class
 */
class OpenAndX
{

    // File types, for OpenAndX

    protected static final int FileTypeDisk = 0;
    protected static final int FileTypeBytePipe = 1;
    protected static final int FileTypeMsgPipe = 2;
    protected static final int FileTypePrinter = 3;
    protected static final int FileTypeUnknown = 0xFFFF;
}
