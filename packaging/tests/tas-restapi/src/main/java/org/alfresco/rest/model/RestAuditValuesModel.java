package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestAuditValuesModel extends TestModel
{

    public String getDisplayName()
    {
        return displayName;
    }
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    private String displayName;
    private String id;

}
