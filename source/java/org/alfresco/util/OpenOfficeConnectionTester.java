/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.util;

import java.net.ConnectException;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple class that checks for the presence of a valid <b>OpenOffice</b>
 * connection, as provided by the
 * <code>net.sf.jooreports.openoffice.connection.OpenOfficeConnection</code> implementations.
 * 
 * @author Derek Hulley
 */
public class OpenOfficeConnectionTester
{
    private static final String INFO_CONNECTION_VERIFIED = "system.openoffice.info.connection_verified";
    private static final String ERR_CONNECTION_FAILED = "system.openoffice.err.connection_failed";
    
    private static Log logger = LogFactory.getLog(OpenOfficeConnectionTester.class);
    
    private OpenOfficeConnection connection;
    private boolean strict;

    public OpenOfficeConnectionTester()
    {
        this.strict = false;
    }
    
    /**
     * @param connection the <b>OpenOffice</b> connection.
     */
    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }
    
    /**
     * @param strict set to <tt>true</tt> to generate a failure if the connection is not connected
     *      during the {@link #checkConnection() connection check}, or false to just issue
     *      a warning.  The default is <tt>false</tt>.
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

    /**
     * Perform the actual connection check.  If this component is {@link #setStrict(boolean) strict},
     * then a disconnected {@link #setConnection(OpenOfficeConnection) connection} will result in a
     * runtime exception being generated.
     */
    public synchronized void checkConnection()
    {
        PropertyCheck.mandatory(this, "connection", connection);
        String connectedMessage = I18NUtil.getMessage(INFO_CONNECTION_VERIFIED);
        if (connection.isConnected())
        {
            // the connection is fine
            logger.info(connectedMessage);
            return;
        }
        // attempt to make the connection
        try
        {
            connection.connect();
            // that worked
            logger.info(connectedMessage);
            return;
        }
        catch (ConnectException e)
        {
            // no luck
        }
        // now we have to either fail or report the connection
        String msg = I18NUtil.getMessage(ERR_CONNECTION_FAILED);
        if (strict)
        {
            throw new AlfrescoRuntimeException(msg);
        }
        else
        {
            logger.warn(msg);
        }
    }
}
