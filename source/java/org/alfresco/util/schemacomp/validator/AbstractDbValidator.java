package org.alfresco.util.schemacomp.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class providing DbValidator support.
 * 
 * @author Matt Ward
 */
public abstract class AbstractDbValidator implements DbValidator
{
    private final Map<String, String> properties = new HashMap<String, String>();
    private final Set<String> fieldsToValidate = new TreeSet<String>();
    
    @Override
    public void setProperty(String name, String value)
    {
        properties.put(name, value);
    }

    @Override
    public String getProperty(String name)
    {
        return properties.get(name);
    }

    @Override
    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    @Override
    public boolean validates(String fieldName)
    {
        return fieldsToValidate.contains(fieldName);
    }
    
    @Override
    public boolean validatesFullObject()
    {
        return false;
    }

    protected void setFieldsToValidate(Set<String> fieldsToValidate)
    {
        this.fieldsToValidate.clear();
        this.fieldsToValidate.addAll(fieldsToValidate);
    }
    
    protected void addFieldToValidate(String fieldName)
    {
        fieldsToValidate.add(fieldName);
    }
}
