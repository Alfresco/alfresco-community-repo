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
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.smb.SMBErrorText;
import org.alfresco.filesys.smb.SMBStatus;

/**
 * SMB exception class
 * <p>
 * This class holds the detail of an SMB network error. The SMB error class and error code are
 * available to give extra detail about the error condition.
 */
public class SMBSrvException extends Exception
{
    private static final long serialVersionUID = 3976733662123341368L;

    // SMB error class

    protected int m_errorclass;

    // SMB error code

    protected int m_errorcode;
    
    // NT 32-bit error code
    
    protected int m_NTerror = -1;

    /**
     * Construct an SMB exception with the specified error class/error code.
     * 
     * @param errclass int
     * @param errcode int
     */
    public SMBSrvException(int errclass, int errcode)
    {
        super(SMBErrorText.ErrorString(errclass, errcode));
        m_errorclass = errclass;
        m_errorcode = errcode;
    }

    /**
     * Construct an SMB exception with the specified error class/error code and additional text
     * error message.
     * 
     * @param errclass int
     * @param errcode int
     * @param msg String
     */
    public SMBSrvException(int errclass, int errcode, String msg)
    {
        super(msg);
        m_errorclass = errclass;
        m_errorcode = errcode;
    }

    /**
     * Construct an SMB exception using the error class/error code in the SMB packet
     * 
     * @param pkt SMBSrvPacket
     */
    protected SMBSrvException(SMBSrvPacket pkt)
    {
        super(SMBErrorText.ErrorString(pkt.getErrorClass(), pkt.getErrorCode()));
        m_errorclass = pkt.getErrorClass();
        m_errorcode = pkt.getErrorCode();
    }

    /**
     * Construct an SMB exception with the specified error class/error code.
     * 
     * @param nterror int
     * @param errclass int
     * @param errcode int
     */
    public SMBSrvException(int nterror, int errclass, int errcode)
    {
        super(SMBErrorText.ErrorString(errclass, errcode));
        m_errorclass = errclass;
        m_errorcode = errcode;
        m_NTerror = nterror;
    }

    /**
     * Return the error class for this SMB exception.
     * 
     * @return SMB error class.
     */
    public int getErrorClass()
    {
        return m_errorclass;
    }

    /**
     * Return the error code for this SMB exception
     * 
     * @return SMB error code
     */
    public int getErrorCode()
    {
        return m_errorcode;
    }

    /**
     * Check if the NT error code has been set
     * 
     * @return boolean
     */
    public final boolean hasNTErrorCode()
    {
    	return m_NTerror != -1 ? true : false;
    }
    
    /**
     * Return the NT error code
     * 
     * @return int
     */
    public int getNTErrorCode() {
        return m_NTerror;
    }
    
    /**
     * Return the error text for the SMB exception
     * 
     * @return Error text string.
     */
    public String getErrorText()
    {
        if ( getNTErrorCode() != 0)
            return SMBErrorText.ErrorString(SMBStatus.NTErr, getNTErrorCode());
        return SMBErrorText.ErrorString(m_errorclass, m_errorcode);
    }
}