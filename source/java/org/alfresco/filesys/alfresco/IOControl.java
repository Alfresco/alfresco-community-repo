package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.smb.nt.NTIOCtl;

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
    public static final int CmdGetAuthTicket	= NTIOCtl.FsCtlCustom + 6;

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
