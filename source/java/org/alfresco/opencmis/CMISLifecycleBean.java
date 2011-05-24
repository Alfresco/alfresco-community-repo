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
package org.alfresco.opencmis;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

/**
 * This bean controls the lifecycle of the CMIS factory.
 * 
 * @author florian.mueller
 */
public class CMISLifecycleBean implements ServletContextAware, InitializingBean, DisposableBean
{
    private ServletContext servletContext;
    private CmisServiceFactory factory;

    @Override
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public void setCmisServiceFactory(CmisServiceFactory factory)
    {
        this.factory = factory;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (factory != null && servletContext != null)
        {
            factory.init(new HashMap<String, String>());
            servletContext.setAttribute(CmisRepositoryContextListener.SERVICES_FACTORY, factory);
        }
    }

    @Override
    public void destroy() throws Exception
    {
        if (factory != null)
        {
            factory.destroy();
        }
    }
}
