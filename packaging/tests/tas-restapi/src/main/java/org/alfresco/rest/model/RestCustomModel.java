package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.CustomContentModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bogdan Bocancea
 */
public class RestCustomModel extends CustomContentModel implements IRestModel<RestCustomModel>
{
    @JsonProperty(value = "entry")
    RestCustomModel model;
    
    @Override
    public ModelAssertion<RestCustomModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestCustomModel> assertThat()
    {
        return new ModelAssertion<>(this);
    }

    @Override
    public RestCustomModel onModel()
    {
        return model;
    }
}
