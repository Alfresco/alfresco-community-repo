package org.alfresco.rest.workflow.api.model;

import java.util.List;

public class FormModelElement
{
    String name;
    String qualifiedName;
    String title;
    String dataType;
    boolean required;
    String defaultValue;
    List<String> allowedValues;
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getQualifiedName()
    {
        return qualifiedName;
    }
    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getDataType()
    {
        return dataType;
    }
    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }
    public boolean isRequired()
    {
        return required;
    }
    public void setRequired(boolean required)
    {
        this.required = required;
    }
    public String getDefaultValue()
    {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
	public List<String> getAllowedValues() 
	{
		return allowedValues;
	}
	public void setAllowedValues(List<String> allowedValues) 
	{
		this.allowedValues = allowedValues;
	}
}
