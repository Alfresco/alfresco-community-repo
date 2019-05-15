package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.utility.model.TestModel;

/**
 * Destination Model used for passing the body parameters in the move node request
 * 
 * @author Ana Bozianu
 */
public class RestNodeBodyMoveCopyModel extends TestModel
{
    @JsonProperty(required = true)
    private String targetParentId;

    @JsonProperty
    private String name;

    public String getTargetParentId()
    {
        return targetParentId;
    }

    public void setTargetParentId(String targetParentId)
    {
        this.targetParentId = targetParentId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
