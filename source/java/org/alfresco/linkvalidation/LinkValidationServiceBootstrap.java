/*-----------------------------------------------------------------------------
*  Copyright 2007-2010 Alfresco Software Limited.
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
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    LinkValidationServiceBootstrap.java
*----------------------------------------------------------------------------*/
package org.alfresco.linkvalidation;

import org.alfresco.repo.avm.util.RawServices;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * Bootstrap for LinkValidationService
 */
public class LinkValidationServiceBootstrap extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(LinkValidationServiceBootstrap.class);
    private LinkValidationService linkValidationService_;
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        ApplicationContext springContext = RawServices.Instance().getContext();
        linkValidationService_ = (LinkValidationService) 
            springContext.getBean("LinkValidationService");

        linkValidationService_.onBootstrap();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        try
        {
            linkValidationService_.onShutdown();
        }
        catch (Throwable e)
        {
            logger.warn("Failed to shut down LinkValidationService", e);
        }
    }
}
