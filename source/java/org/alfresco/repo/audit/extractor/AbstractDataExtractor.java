package org.alfresco.repo.audit.extractor;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract implementation to provide support.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@AlfrescoPublicApi
public abstract class AbstractDataExtractor implements DataExtractor, InitializingBean, BeanNameAware
{
    /** Logger that can be used by subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    
    private String name;
    private NamedObjectRegistry<DataExtractor> registry;

    /**
     * Set the name with which to {@link #setRegistry(NamedObjectRegistry) register}
     * @param name          the name of the bean
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Set the registry with which to register
     */
    public void setRegistry(NamedObjectRegistry<DataExtractor> registry)
    {
        this.registry = registry;
    }

    /**
     * Registers the instance
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "name", name);
        PropertyCheck.mandatory(this, "registry", registry);

        registry.register(name, this);
    }

    /**
     * This implementation assumes all extractors are stateless i.e. if the class matches
     * then the instances are equal.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj.getClass().equals(this.getClass()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
