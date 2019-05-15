package org.alfresco.rest.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.testng.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bogdan Bocancea
 */
public class RestCustomAspectModel extends CustomAspectModel implements IRestModel<RestCustomAspectModel>
{
    @Override
    public ModelAssertion<RestCustomAspectModel> assertThat()
    {
        return new ModelAssertion<RestCustomAspectModel>(this);
    }

    @Override
    public ModelAssertion<RestCustomAspectModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestCustomAspectModel model;

    @Override
    public RestCustomAspectModel onModel()
    {
        return model;
    }

    public RestCustomAspectModel assertHasProperties(CustomAspectPropertiesModel... propertiesName)
    {
        List<String> propertiesModelName = new ArrayList<String>();
        List<CustomAspectPropertiesModel> properiesModel = getProperties();
        for (CustomAspectPropertiesModel model : properiesModel)
        {
            propertiesModelName.add(model.getName());
        }
        for (CustomAspectPropertiesModel property : propertiesName)
        {
            Assert.assertTrue(propertiesModelName.contains(property.getName()), String.format("Property %s was found.", property.getName()));
        }
        return this;
    }
}
