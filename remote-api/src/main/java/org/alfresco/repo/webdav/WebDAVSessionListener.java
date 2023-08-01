/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.repo.webdav;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>WebDAVSessionListener is used to forcibly unlock documents that were 
 * persistently locked during user's session and were not unlocked because of some extraordinary
 * situations such as network connection lost. Was introduced in ALF-11777 jira issue.
 * </p>
 * 
 * @author Pavel.Yurkevich
 * 
 */
public class WebDAVSessionListener implements HttpSessionListener, ServletContextListener
{
    private static Log logger = LogFactory.getLog(WebDAVSessionListener.class);
    private WebDAVLockService webDAVLockService;

    /**
     * @param webDAVLockService
     *            the webDAVLockService to set
     */
    public void setWebDAVLockService(WebDAVLockService webDAVLockService)
    {
        this.webDAVLockService = webDAVLockService;
    }

    @Override
    public void sessionCreated(HttpSessionEvent hse)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Session created " + hse.getSession().getId());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sessionDestroyed(HttpSessionEvent hse)
    {
        webDAVLockService.setCurrentSession(hse.getSession());
        webDAVLockService.sessionDestroyed();

        if (logger.isDebugEnabled())
        {
            logger.debug("Session destroyed " + hse.getSession().getId());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        this.webDAVLockService = (WebDAVLockService)context.getBean(WebDAVLockService.BEAN_NAME);
    }
}
