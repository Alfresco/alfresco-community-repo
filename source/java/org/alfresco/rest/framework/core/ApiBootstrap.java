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
