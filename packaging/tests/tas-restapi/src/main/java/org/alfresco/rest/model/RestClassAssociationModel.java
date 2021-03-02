package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestClassAssociationModel extends TestModel
{
    public String id;
    public String title;
    public String description;
    public Boolean isChild;
    public Boolean isProtected;
    public RestClassAssociationDefinitionModel source = null;
    public RestClassAssociationDefinitionModel target = null;

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

    public Boolean getChild()
    {
        return isChild;
    }

    public void setIsChild(Boolean isChild)
    {
        this.isChild = isChild;
    }

    public Boolean getIsProtected()
    {
        return isProtected;
    }

    public void setIsProtected(Boolean isProtected)
    {
        this.isProtected = isProtected;
    }

    public RestClassAssociationDefinitionModel getSource()
    {
        return source;
    }

    public void setSource(RestClassAssociationDefinitionModel source)
    {
        this.source = source;
    }

    public RestClassAssociationDefinitionModel getTarget()
    {
        return target;
    }

    public void setTarget(RestClassAssociationDefinitionModel target)
    {
        this.target = target;
    }
}


