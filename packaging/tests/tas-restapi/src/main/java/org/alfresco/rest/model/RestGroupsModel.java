package org.alfresco.rest.model;

import java.util.ArrayList;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestGroupsModel extends TestModel implements IRestModel<RestGroupsModel>
{
    @JsonProperty(required = true)
    private String id;
    @JsonProperty(required = true)
    private String displayName;
    @JsonProperty(required = true)
    private Boolean isRoot;

    @JsonProperty("parentIds")
    private ArrayList<String> parentIds;
    @JsonProperty("zones")
    private ArrayList<String> zones;

    @JsonProperty(value = "entry")
    RestGroupsModel model;


    @Override
    public ModelAssertion<RestGroupsModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestGroupsModel> assertThat()
    {
        return new ModelAssertion<RestGroupsModel>(this);
    }

    @Override
    public RestGroupsModel onModel()
    {
        return model;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public Boolean getIsRoot()
    {
        return isRoot;
    }

    public void setIsRoot(Boolean isRoot)
    {
        this.isRoot = isRoot;
    }

    public ArrayList<String> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(ArrayList<String> parentIds)
    {
        this.parentIds = parentIds;
    }

    public ArrayList<String> getZones()
    {
        return zones;
    }

    public void setZones(ArrayList<String> zones)
    {
        this.zones = zones;
    }
}
