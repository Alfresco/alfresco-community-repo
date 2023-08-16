/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.app;

import jakarta.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jndi.JndiTemplate;

/**
 * A specialised {@link org.springframework.web.context.ContextLoaderListener} that can be disabled by a boolean
 * <code>java:comp/env/properties/startup.enable</code> JNDI entry. If <code>startup.enable</code> is configured as
 * false then the Spring Application Context is not created, allowing further configuration changes to be made after
 * initial deployment.
 * 
 * @author dward
 */
public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener
{
    /**
     * The JNDI environment entry that controls global startup
     */
    private static final String PROPERTY_ENABLE_STARTUP = "java:comp/env/properties/startup.enable";

    protected final static Log log = LogFactory.getLog(ContextLoaderListener.class);
    private boolean enableStartup;

    public ContextLoaderListener()
    {
        try
        {
            this.enableStartup = (Boolean) new JndiTemplate().lookup(ContextLoaderListener.PROPERTY_ENABLE_STARTUP,
                    Boolean.class);
        }
        catch (Exception e)
        {
            this.enableStartup = true;
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        if (this.enableStartup)
        {
            super.contextInitialized(event);
        }
        else
        {
            ContextLoaderListener.log
                    .warn("The "
                            + ContextLoaderListener.PROPERTY_ENABLE_STARTUP
                            + " environment entry is false. Please configure the environment entries for this application and then restart the server.");
        }
    }
}
