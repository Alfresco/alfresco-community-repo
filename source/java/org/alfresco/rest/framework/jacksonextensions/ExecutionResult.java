package org.alfresco.rest.framework.jacksonextensions;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of 1 or more executions.  This object will be rendered as JSON
 *
 * @author Gethin James
 */
public class ExecutionResult
{
    private final Object root;
    private boolean anEmbeddedEntity;
    private final Map<String,Object> embedded = new HashMap<String,Object>();
    private final Map<String,Object> related = new HashMap<String,Object>();
    private final BeanPropertiesFilter filter;
    
    public ExecutionResult(Object root,BeanPropertiesFilter filter)
    {
        super();
        this.root = root;
        this.filter = filter;
        this.anEmbeddedEntity = false;
    }

    /**
     * @return the filter
     */
    public BeanPropertiesFilter getFilter()
    {
        return this.filter;
    }
    
    public Object getRoot()
    {
        return this.root;
    }
    
    /**
     * Adds embeddeds object to the enclosing root object
     * @param key
     * @param embedded objects to add
     */
    public void addEmbedded(Map<String,Object> embedded)
    {
        this.embedded.putAll(embedded);
    }
    
    /**
     * Adds related object to the enclosing root object
     * @param key
     * @param related objects to add
     */
    public void addRelated(Map<String,Object> related)
    {
        this.related.putAll(related);
    }

    /**
     * Is this object and embedded entity
     * 
     * @return boolean - true if it is embedded, defaults to false
     */
    public boolean isAnEmbeddedEntity()
    {
        return this.anEmbeddedEntity;
    }
    
    /**
     * Is this object and embedded entity
     * 
     * @param anEmbeddedEntity - true if it is embedded, defaults to false
     */   
    public void setAnEmbeddedEntity(boolean anEmbeddedEntity)
    {
        this.anEmbeddedEntity = anEmbeddedEntity;
    }

    
    /**
     * Returns the Map of related objects
     * 
     * @return
     */
    public Map<String, Object> getRelated()
    {
        return this.related;
    }

    /**
     * Returns the Map of embedded objects
     * 
     * @return
     */
    public Map<String, Object> getEmbedded()
    {
        return this.embedded;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ExecutionResult [root=");
        builder.append(this.root);
        builder.append(", anEmbeddedEntity=");
        builder.append(this.anEmbeddedEntity);
        builder.append(", embedded=");
        builder.append(this.embedded);
        builder.append(", related=");
        builder.append(this.related);
        builder.append(", filter=");
        builder.append(this.filter);
        builder.append("]");
        return builder.toString();
    }

 

}
