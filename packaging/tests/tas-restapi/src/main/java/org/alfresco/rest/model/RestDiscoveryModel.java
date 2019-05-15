package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestDiscoveryModel extends TestModel implements IRestModel<RestDiscoveryModel>
{
    @Override
    public ModelAssertion<RestDiscoveryModel> assertThat()
    {
        return new ModelAssertion<RestDiscoveryModel>(this);
    }

    @Override
    public ModelAssertion<RestDiscoveryModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestDiscoveryModel model;

    @Override
    public RestDiscoveryModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private RestRepositoryInfoModel repository;

    public RestRepositoryInfoModel getRepository()
    {
        return repository;
    }

    public void setRepository(RestRepositoryInfoModel repository)
    {
        this.repository = repository;
    }
}
