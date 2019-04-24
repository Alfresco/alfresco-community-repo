/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.web.app.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.EnumSet;
import java.util.Properties;

/**
 * The CORSContextListener adds the Tomcat CORS filter org.apache.catalina.filters.CorsFilter which allows to enable/disable and configure Cross-Origin Resource Sharing (CORS) with alfresco properties.
 */
public class CORSContextListener implements ServletContextListener
{
    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String CORS_ENABLED = "cors.enabled";
    private static final String CORS_ALLOWED_ORIGINS = "cors.allowed.origins";
    private static final String CORS_ALLOWED_METHODS = "cors.allowed.methods";
    private static final String CORS_ALLOWED_HEADERS = "cors.allowed.headers";
    private static final String CORS_EXPOSED_HEADERS = "cors.exposed.headers";
    private static final String CORS_SUPPORT_CREDENTIALS = "cors.support.credentials";
    private static final String CORS_PREFLIGHT_CREDENTIALS = "cors.preflight.maxage";

    private Log logger = LogFactory.getLog(getClass());

    private final EnumSet<DispatcherType> DISPATCHER_TYPE = EnumSet
            .of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ServletContext servletContext = sce.getServletContext();
        initCORS(servletContext);
    }

    /**
     * Initializes CORS filter
     */
    private void initCORS(ServletContext servletContext)
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

        Properties gP = (Properties) wc.getBean(BEAN_GLOBAL_PROPERTIES);
        Boolean corsEnabled = new Boolean(gP.getProperty(CORS_ENABLED));

        if(logger.isDebugEnabled())
        {
            logger.debug("CORS filter is" + (corsEnabled?" ":" not ") + "enabled");
        }
        if (corsEnabled)
        {
            FilterRegistration.Dynamic corsFilter = servletContext.addFilter("CorsFilter", "org.apache.catalina.filters.CorsFilter");
            corsFilter.setInitParameter(CORS_ALLOWED_ORIGINS, gP.getProperty(CORS_ALLOWED_ORIGINS));
            corsFilter.setInitParameter(CORS_ALLOWED_METHODS, gP.getProperty(CORS_ALLOWED_METHODS));
            corsFilter.setInitParameter(CORS_ALLOWED_HEADERS, gP.getProperty(CORS_ALLOWED_HEADERS));
            corsFilter.setInitParameter(CORS_EXPOSED_HEADERS, gP.getProperty(CORS_EXPOSED_HEADERS));
            corsFilter.setInitParameter(CORS_SUPPORT_CREDENTIALS, gP.getProperty(CORS_SUPPORT_CREDENTIALS));
            corsFilter.setInitParameter(CORS_PREFLIGHT_CREDENTIALS, gP.getProperty(CORS_PREFLIGHT_CREDENTIALS));
            corsFilter.addMappingForUrlPatterns(DISPATCHER_TYPE, false, "/*");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {

    }
}
