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
