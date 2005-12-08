/*
 * Copyright (C) 2005 Alfresco, Inc.
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
