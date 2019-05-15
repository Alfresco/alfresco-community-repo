package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

/**
 * Created by Claudia Agache on 11/11/2016.
 */
public class RestByUserModel extends TestModel
{
    private String displayName;
    private String id;

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
}
