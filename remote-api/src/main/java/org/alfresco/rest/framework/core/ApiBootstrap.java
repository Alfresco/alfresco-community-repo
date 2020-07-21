/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.framework.core;

import java.util.Map;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bootstraps the restful API
 *
 * @author Gethin James
 */
public class ApiBootstrap extends AbstractLifecycleBean
{

    private static Log logger = LogFactory.getLog(ApiBootstrap.class);  
    
    ResourceLookupDictionary apiDictionary;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        logger.info("Bootstapping the API");
        ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
        ApplicationContext ac = refreshEvent.getApplicationContext();
        Map<String, Object> entityResourceBeans = ac.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = ac.getBeansWithAnnotation(RelationshipResource.class);
        apiDictionary.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        logger.info("Shutting down the API");
    }

    public void setApiDictionary(ResourceLookupDictionary apiDictionary)
    {
        this.apiDictionary = apiDictionary;
    }
}
