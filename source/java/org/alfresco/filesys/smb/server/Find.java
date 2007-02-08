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
 * Find First Flags Class
 */
class Find
{
    // Find first flags

    protected static final int CloseSearch = 0x01;
    protected static final int CloseSearchAtEnd = 0x02;
    protected static final int ResumeKeysRequired = 0x04;
    protected static final int ContinuePrevious = 0x08;
    protected static final int BackupIntent = 0x10;
}
