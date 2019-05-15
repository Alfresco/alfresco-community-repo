package org.alfresco.rest.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import java.util.List;

/**
 * Hacked together by 'gethin' on '2017-03-23 10:59' from 'Alfresco Search REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/search/versions/1}
 */
public class RestRequestFilterQueryModel extends TestModel implements IRestModel<RestRequestFilterQueryModel>
{
    @Override
    public ModelAssertion<RestRequestFilterQueryModel> assertThat()
    {
        return new ModelAssertion<RestRequestFilterQueryModel>(this);
    }

    @Override
    public ModelAssertion<RestRequestFilterQueryModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestRequestFilterQueryModel model;

    @Override
    public RestRequestFilterQueryModel onModel()
    {
        return model;
    }

    public RestRequestFilterQueryModel()
    {
        super();
    }

    public RestRequestFilterQueryModel(String query)
    {
        super();
        this.query = query;
    }

    public RestRequestFilterQueryModel(String query, List<String> tags)
    {
        super();
        this.query = query;
        this.tags = tags;
    }

    /**
    The filter query
    */	        

    private String query;	    
    private List<String> tags;

    public String getQuery()
    {
        return this.query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }				

    public List<String> getTags()
    {
        return this.tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }				
}
 
