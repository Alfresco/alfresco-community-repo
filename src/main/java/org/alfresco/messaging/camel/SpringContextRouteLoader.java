/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.messaging.camel;

import java.util.ArrayList;

import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Injects a specified route context into a specified Camel context
 * 
 * @author Ray Gauss II
 */
public class SpringContextRouteLoader implements ApplicationContextAware, InitializingBean
{

    private ApplicationContext applicationContext;
    private String camelContextId;
    private String routeContextId;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    public void setCamelContextId(String camelContextId)
    {
        this.camelContextId = camelContextId;
    }

    public void setRouteContextId(String routeContextId)
    {
        this.routeContextId = routeContextId;
    }

    @SuppressWarnings("unchecked")
    public void addRoutesToCamelContext() throws Exception
    {
        ModelCamelContext modelCamelContext = (ModelCamelContext) applicationContext.getBean(camelContextId);
        ArrayList<RouteDefinition> routeDefinitions = (ArrayList<RouteDefinition>) applicationContext.getBean(routeContextId);
        modelCamelContext.addRouteDefinitions(routeDefinitions);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        addRoutesToCamelContext();
    }


}
