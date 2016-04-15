package org.alfresco.rest.workflow.api.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Representation of a variable in the Activiti engine.
 * 
 * @author Frederik Heremans
 */
public class Variable
{
    protected String name;
    protected String type;
    protected Object value;
    
    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getType()
    {
        return this.type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    @JsonSerialize(include=Inclusion.ALWAYS)
    public Object getValue()
    {
        return this.value;
    }
    public void setValue(Object value)
    {
        this.value = value;
    }
}
