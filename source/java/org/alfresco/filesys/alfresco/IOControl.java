/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.smb.NTIOCtl;

/**
 * Content Disk Driver I/O Control Codes Class
 * 
 * <p>Contains I/O control codes and status codes used by the content disk driver I/O control
 * implementation.
 * 
 * @author gkspencer
 */
public class IOControl
{
    // Custom I/O control codes
    
    public static final int CmdProbe      		= NTIOCtl.FsCtlCustom;
    public static final int CmdFileStatus 		= NTIOCtl.FsCtlCustom + 1;
    // Version 1 CmdCheckOut = NTIOCtl.FsCtlCustom + 2
    // Version 1 CmdCheckIn  = NTIOCtl.FsCtlCustom + 3
    public static final int CmdGetActionInfo	= NTIOCtl.FsCtlCustom + 4;
    public static final int CmdRunAction   		= NTIOCtl.FsCtlCustom + 5;

    // I/O control request/response signature
    
    public static final String Signature   = "ALFRESCO";
    
    // I/O control interface version id
    
    public static final int Version				= 2;
    
    // Boolean field values
    
    public static final int True                = 1;
    public static final int False               = 0;
    
    // File status field values
    //
    // Node type
    
    public static final int TypeFile            = 0;
    public static final int TypeFolder          = 1;
    
    // Lock status
    
    public static final int LockNone            = 0;
    public static final int LockRead            = 1;
    public static final int LockWrite           = 2;
}
