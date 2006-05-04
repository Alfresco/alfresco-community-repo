/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
 */
package org.alfresco.filesys.smb.server.repo;

import org.alfresco.filesys.smb.NTIOCtl;

/**
 * Content Disk Driver I/O Control Codes Class
 * 
 * <p>contains I/O control codes and status codes used by the content disk driver I/O control
 * implementation.
 * 
 * @author gkspencer
 */
public class IOControl
{
    // Custom I/O control codes
    
    public static final int CmdProbe      = NTIOCtl.FsCtlCustom;
    public static final int CmdFileStatus = NTIOCtl.FsCtlCustom + 1;
    public static final int CmdCheckOut   = NTIOCtl.FsCtlCustom + 2;
    public static final int CmdCheckIn    = NTIOCtl.FsCtlCustom + 3;

    // I/O control request/response signature
    
    public static final String Signature   = "ALFRESCO";
    
    // I/O control status codes
    
    public static final int StsSuccess          = 0;
    
    public static final int StsError            = 1;
    public static final int StsFileNotFound     = 2;
    public static final int StsAccessDenied     = 3;
    public static final int StsBadParameter     = 4;
    public static final int StsNotWorkingCopy   = 5;
    
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
