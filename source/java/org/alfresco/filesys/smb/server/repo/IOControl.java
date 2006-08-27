/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.smb.server.repo;

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
