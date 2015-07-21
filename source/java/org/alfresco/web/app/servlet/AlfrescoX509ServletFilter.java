/*
* Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.web.app.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.alfresco.web.scripts.servlet.X509ServletFilterBase;

import javax.servlet.*;
import java.io.IOException;
import java.util.Properties;

/**
 * The AlfrescoX509ServletFilter implements the checkEnforce method of the X509ServletFilterBase.
 * This allows the configuration of X509 authentication to be toggled on/off through a
 * configuration outside of the web.xml.
 **/

public class AlfrescoX509ServletFilter extends X509ServletFilterBase
{
    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String SECURE_COMMS = "solr.secureComms";

    private static Log logger = LogFactory.getLog(AlfrescoX509ServletFilter.class);

    @Override
    protected boolean checkEnforce(ServletContext servletContext) throws IOException
    {
        /*
        * Get the secureComms setting from the global properties bean.
        */

        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        Properties globalProperties = (Properties) wc.getBean(BEAN_GLOBAL_PROPERTIES);
        String prop = globalProperties.getProperty(SECURE_COMMS);

        if(logger.isDebugEnabled())
        {
            logger.debug("secureComms:"+prop);
        }

        /*
         * Return true or false based on the property. This will switch on/off X509 enforcement in the X509ServletFilterBase.
         */

        if (prop == null || "none".equals(prop))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}