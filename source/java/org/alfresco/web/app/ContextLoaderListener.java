/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app;

import javax.servlet.ServletContextEvent;

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
