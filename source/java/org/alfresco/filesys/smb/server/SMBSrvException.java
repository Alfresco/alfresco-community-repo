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
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.smb.SMBErrorText;

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

    /**
     * Construct an SMB exception with the specified error class/error code.
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
     */
    public SMBSrvException(int errclass, int errcode, String msg)
    {
        super(msg);
        m_errorclass = errclass;
        m_errorcode = errcode;
    }

    /**
     * Construct an SMB exception using the error class/error code in the SMB packet
     */
    protected SMBSrvException(SMBSrvPacket pkt)
    {
        super(SMBErrorText.ErrorString(pkt.getErrorClass(), pkt.getErrorCode()));
        m_errorclass = pkt.getErrorClass();
        m_errorcode = pkt.getErrorCode();
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
     * Return the error text for the SMB exception
     * 
     * @return Error text string.
     */
    public String getErrorText()
    {
        return SMBErrorText.ErrorString(m_errorclass, m_errorcode);
    }
}