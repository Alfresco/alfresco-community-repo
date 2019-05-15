package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestSiteContainerModel extends TestModel implements IRestModel<RestSiteContainerModel>
{
    @JsonProperty(value = "entry")
    RestSiteContainerModel model;

    @Override
    public RestSiteContainerModel onModel()
    {
        return model;
    }

    private String id;
    private String folderId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteContainerModel> assertThat() 
    {
      return new ModelAssertion<RestSiteContainerModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteContainerModel> and() 
    {
      return assertThat();
    }
    
}    