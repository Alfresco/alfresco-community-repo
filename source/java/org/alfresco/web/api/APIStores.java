package org.alfresco.web.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

public class APIStores implements ApplicationContextAware, ApplicationListener
{
    private ApplicationContext applicationContext;
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();
    private APITemplateProcessor templateProcessor;

    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * Hooks into Spring Application Lifecycle
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            initTemplateProcessor();
            initScriptProcessor();
        }
    
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
        this.lifecycle.setApplicationContext(applicationContext);        
    }


    protected void initTemplateProcessor()
    {        
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        for (APIStore apiStore : getAPIStores())
        {
            TemplateLoader loader = apiStore.getTemplateLoader();
            if (loader == null)
            {
                throw new APIException("Unable to retrieve template loader for api store " + apiStore.getBasePath());
            }
            loaders.add(loader);
        }
        MultiTemplateLoader loader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
        templateProcessor.setTemplateLoader(loader);
    }

    protected void initScriptProcessor()
    {
        
    }
    
    @SuppressWarnings("unchecked")
    public Collection<APIStore> getAPIStores()
    {
        return applicationContext.getBeansOfType(APIStore.class, false, false).values();
    }

    public void setTemplateProcessor(APITemplateProcessor templateProcessor)
    {
        this.templateProcessor = templateProcessor;
    }
        
    public APITemplateProcessor getTemplateProcessor()
    {
        return templateProcessor;
    }
       
}
