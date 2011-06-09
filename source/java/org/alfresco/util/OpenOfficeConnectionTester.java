/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Map;
import java.util.TreeMap;

import net.sf.jooreports.openoffice.connection.AbstractOpenOfficeConnection;
import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

import com.sun.star.registry.RegistryValueType;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.registry.XSimpleRegistry;
import com.sun.star.uno.UnoRuntime;

/**
 * A bootstrap class that checks for the presence of a valid <b>OpenOffice</b> connection, as provided by the
 * <code>net.sf.jooreports.openoffice.connection.OpenOfficeConnection</code> implementations.
 * 
 * @author Derek Hulley
 */
public class OpenOfficeConnectionTester extends AbstractLifecycleBean
{
    private static final String ATTRIBUTE_AVAILABLE = "available";
    private static final String INFO_CONNECTION_VERIFIED = "system.openoffice.info.connection_verified";
    private static final String ERR_CONNECTION_FAILED = "system.openoffice.err.connection_failed";
    private static final String ERR_CONNECTION_LOST = "system.openoffice.err.connection_lost";
    private static final String ERR_CONNECTION_REMADE = "system.openoffice.err.connection_remade";
    
    private static Log logger = LogFactory.getLog(OpenOfficeConnectionTester.class);
    
    private OpenOfficeConnection connection;
    private Map<String, Object> openOfficeMetadata = new TreeMap<String, Object>();
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
     * @see #checkConnection()
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        checkConnection();
        ((ApplicationContext) event.getSource()).publishEvent(new OpenOfficeConnectionEvent(this.openOfficeMetadata));
    }

    /**
     * Disconnect
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        if(connection != null)
        {
            if(connection.isConnected())
            {
                connection.disconnect();
            }
        }
    }

    /**
     * Perform the actual connection check.  If this component is {@link #setStrict(boolean) strict},
     * then a disconnected {@link #setConnection(OpenOfficeConnection) connection} will result in a
     * runtime exception being generated.
     */
    private void checkConnection()
    {
        String connectedMessage = I18NUtil.getMessage(INFO_CONNECTION_VERIFIED);
        boolean connected = testAndConnect();
        OpenOfficeConnectionTesterJob.wasConnected = Boolean.valueOf(connected);
        if (connected)
        {
            // the connection is fine
            logger.debug(connectedMessage);
            return;
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
    
    public boolean testAndConnect()
    {
        synchronized (this.openOfficeMetadata)
        {
            PropertyCheck.mandatory(this, "connection", connection);
            if (!connection.isConnected())
            {
                try
                {
                    connection.connect();
                }
                catch (ConnectException e)
                {
                    // No luck
                    this.openOfficeMetadata.clear();
                    this.openOfficeMetadata.put(ATTRIBUTE_AVAILABLE, Boolean.FALSE);
                    return false;
                }
            }

            // Let's try to get at the version metadata
            Boolean lastAvailability = (Boolean)this.openOfficeMetadata.get(ATTRIBUTE_AVAILABLE);
            if (lastAvailability == null || !lastAvailability.booleanValue())
            {
                this.openOfficeMetadata.put(ATTRIBUTE_AVAILABLE, Boolean.TRUE);
                try
                {
                    // We have to peak inside the connection class to get the service we want!
                    Method getServiceMethod = AbstractOpenOfficeConnection.class.getDeclaredMethod("getService",
                            String.class);
                    getServiceMethod.setAccessible(true);
                    Object configurationRegistry = getServiceMethod.invoke(connection,
                            "com.sun.star.configuration.ConfigurationRegistry");
                    XSimpleRegistry registry = (XSimpleRegistry) UnoRuntime.queryInterface(
                            com.sun.star.registry.XSimpleRegistry.class, configurationRegistry);
                    registry.open("org.openoffice.Setup", true, false);
                    XRegistryKey root = registry.getRootKey();
                    XRegistryKey product = root.openKey("Product");
                    for (XRegistryKey key : product.openKeys())
                    {
                        switch (key.getValueType().getValue())
                        {
                        case RegistryValueType.LONG_value:
                            openOfficeMetadata.put(key.getKeyName(), key.getLongValue());
                            break;
                        case RegistryValueType.ASCII_value:
                            openOfficeMetadata.put(key.getKeyName(), key.getAsciiValue());
                            break;
                        case RegistryValueType.STRING_value:
                            openOfficeMetadata.put(key.getKeyName(), key.getStringValue());
                            break;
                        }
                    }
                    registry.close();
                }
                catch (com.sun.star.uno.RuntimeException oooRuntimeException)
                {
                    // ALF-5747 discusses an OOo problem whereby an interface component was not implemented & therefore version
                    // information cannot be retrieved. This does not seem to affect the operation of the OOo process.
                    // If we see this exception, which occurs in OOo 3.3.0, we'll shorten the exception log & make it friendlier.

                    final String exceptionMessage = oooRuntimeException.getMessage();
                    if (exceptionMessage != null && exceptionMessage.contains("com.sun.star.configuration.ConfigurationRegistry: not implemented"))
                    {
                        logger.warn("Error trying to query Open Office version information. " +
                                    "OpenOffice.org's ConfigurationRegistry not implemented in this version of OOo. This should not affect the operation of OOo.");
                        // We have intentionally not logged the exception object here.
                    }
                    else
                    {
                        logger.warn("Error trying to query Open Office version information", oooRuntimeException);
                    }
                }
                catch (Exception e)
                {
                    logger.warn("Error trying to query Open Office version information", e);
                }
            }
        }
        return true;
    }

    /**
     * Quartz job that checks an OpenOffice connection.
     * 
     * @author Derek Hulley
     * @since 2.1.2
     */
    public static class OpenOfficeConnectionTesterJob implements Job
    {
        private static volatile Boolean wasConnected;
        
        public OpenOfficeConnectionTesterJob()
        {
        }

        /**
         * Check the connection.
         * @see OpenOfficeConnectionTester#checkConnection()
         */
        public synchronized void execute(JobExecutionContext context) throws JobExecutionException
        {
            /*
             * Synchronized just in case of overzelous triggering.
             */
            
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            // Get the connecion tester
            Object openOfficeConnectionTesterObj = jobData.get("openOfficeConnectionTester");
            if (openOfficeConnectionTesterObj == null || !(openOfficeConnectionTesterObj instanceof OpenOfficeConnectionTester))
            {
                throw new AlfrescoRuntimeException("OpenOfficeConnectionJob data must contain valid 'openOfficeConnectionTester' reference");
            }
            OpenOfficeConnectionTester openOfficeConnectionTester = (OpenOfficeConnectionTester) openOfficeConnectionTesterObj;
            
            // Get the extractor and transformer registries.  These are not mandatory.
            Object metadataExractorRegistryObj = jobData.get("metadataExractorRegistry");
            MetadataExtracterRegistry metadataExtracterRegistry = null;
            if (metadataExractorRegistryObj != null && (metadataExractorRegistryObj instanceof MetadataExtracterRegistry))
            {
                metadataExtracterRegistry = (MetadataExtracterRegistry) metadataExractorRegistryObj;
            }            
            
            // Now ping the connection.  It doesn't matter if it fails or not.
            boolean connected = openOfficeConnectionTester.testAndConnect();
            // Now log, if necessary
            if (OpenOfficeConnectionTesterJob.wasConnected == null)
            {
                // This is the first pass
            }
            else if (OpenOfficeConnectionTesterJob.wasConnected.booleanValue() == connected)
            {
                // Nothing changed since last time
            }
            else
            {
                if (connected)
                {
                    // This is reported as a warning as admins must be aware that it is bouncing
                    logger.info(I18NUtil.getMessage(ERR_CONNECTION_REMADE));
                }
                else
                {
                    logger.error(I18NUtil.getMessage(ERR_CONNECTION_LOST));
                }
                // The value changed so ensure that the registries are bounced
                if (metadataExtracterRegistry != null)
                {
                    metadataExtracterRegistry.resetCache();
                }
            }
            // Record the state
            OpenOfficeConnectionTesterJob.wasConnected = Boolean.valueOf(connected);
        }
    }
}
