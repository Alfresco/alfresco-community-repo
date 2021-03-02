package org.alfresco.rest.model;

import java.util.List;

public class RestPropertyDefinitionModel
{
    private String id;
    private String title;
    private String description;
    private String defaultValue;
    private String dataType;
    private Boolean isMultiValued;
    private Boolean isMandatory;
    private Boolean isMandatoryEnforced;
    private Boolean isProtected;
    private List<RestConstraintDefinitionModel> constraints;

    public String getId() 
    {
        return id;
    }

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDefaultValue() 
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public boolean getIsProtected()
    {
        return isProtected;
    }

    public void setIsProtected(boolean isProtected)
    {
        this.isProtected = isProtected;
    }

    public List<RestConstraintDefinitionModel> getConstraints()
    {
        return constraints;
    }

    public void setConstraints(List<RestConstraintDefinitionModel> constraints)
    {
        this.constraints = constraints;
    }

    public boolean getIsMultiValued() 
    {
        return isMultiValued;
    }

    public void setIsMultiValued(boolean isMultiValued) 
    {
        this.isMultiValued = isMultiValued;
    }

    public boolean getIsMandatory() 
    {
        return isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) 
    {
        this.isMandatory = isMandatory;
    }

    public boolean getIsMandatoryEnforced() 
    {
        return isMandatoryEnforced;
    }

    public void setIsMandatoryEnforced(boolean isMandatoryEnforced) 
    {
        this.isMandatoryEnforced = isMandatoryEnforced;
    }
}