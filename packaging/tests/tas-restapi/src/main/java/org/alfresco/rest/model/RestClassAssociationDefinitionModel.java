package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestClassAssociationDefinitionModel extends TestModel
{
    public String role = null;
    public String cls = null;
    public Boolean isMany = null;
    public Boolean isMandatory = null;
    public Boolean isMandatoryEnforced = null;

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getCls()
    {
        return cls;
    }

    public void setCls(String cls)
    {
        this.cls = cls;
    }

    public Boolean getMany()
    {
        return isMany;
    }

    public void setMany(Boolean many)
    {
        isMany = many;
    }

    public Boolean getMandatory()
    {
        return isMandatory;
    }

    public void setMandatory(Boolean mandatory)
    {
        isMandatory = mandatory;
    }

    public Boolean getMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }

    public void setMandatoryEnforced(Boolean mandatoryEnforced)
    {
        isMandatoryEnforced = mandatoryEnforced;
    }
}
