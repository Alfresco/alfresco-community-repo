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
package org.alfresco.filesys.netbios.win32;

import java.io.IOException;

/**
 * Winsock NetBIOS Exception Class
 * 
 * <p>Contains the Winsock error code from the failed Winsock call.
 * 
 * @author GKSpencer
 */
public class WinsockNetBIOSException extends IOException
{
    private static final long serialVersionUID = 5933702607108016674L;

    // Winsock error code
    
    private int m_errCode;

    /**
     * Default constructor
     */
    public WinsockNetBIOSException()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param msg String
     */
    public WinsockNetBIOSException(String msg)
    {
        super(msg);
        
        // Split out the error code
        
        if ( msg != null)
        {
            int pos = msg.indexOf(":");
            if ( pos != -1)
                m_errCode = Integer.valueOf(msg.substring(0, pos));
        }
    }
    
    /**
     * Class constructor
     * 
     * @param sts int
     */
    public WinsockNetBIOSException(int sts)
    {
        super();
        
        m_errCode = sts;
    }
    
    /**
     * Return the Winsock error code
     * 
     * @return int
     */
    public final int getErrorCode()
    {
        return m_errCode;
    }
    
    /**
     * Set the error code
     * 
     * @param sts int
     */
    public final void setErrorCode(int sts)
    {
        m_errCode = sts;
    }

    /**
     * Return the error message string
     * 
     * @return String
     */
    public String getMessage()
    {
        StringBuilder msg = new StringBuilder();
        
        msg.append( super.getMessage());
        String winsockErr = WinsockError.asString(getErrorCode());
        if ( winsockErr != null)
        {
            msg.append(" - ");
            msg.append(winsockErr);
        }
        
        return msg.toString();
    }
}
