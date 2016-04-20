package org.alfresco.repo.template;

import java.util.HashMap;

import org.alfresco.service.ServiceRegistry;

/**
 * An abstract Map class that can be used process the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public abstract class BaseTemplateMap extends HashMap implements Cloneable
{
    protected TemplateNode parent;
    protected ServiceRegistry services = null;
    
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public BaseTemplateMap(TemplateNode parent, ServiceRegistry services)
    {
        super(1, 1.0f);
        this.services = services;
        this.parent = parent;
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public abstract Object get(Object key);
}
