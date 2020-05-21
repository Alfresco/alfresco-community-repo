package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomTypeModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

/**
 * @author Bogdan Bocancea
 */
public class CustomTypeManager extends ModelRequest<CustomTypeManager>
{
    private CustomContentModel customContentModel;
    private RestCustomTypeModel customTypeModel;
    private CustomModelProperties customProperties = new CustomModelProperties(restWrapper);

    public CustomTypeManager(CustomContentModel customContentModel, RestCustomTypeModel customTypeModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
        this.customTypeModel = customTypeModel;
    }

    public RestCustomTypeModel getCustomType()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "cmm/{modelName}/types/{typeName}?{parameters}",
                this.customContentModel.getName(), this.customTypeModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomTypeModel.class, request);
    }

    public void addProperty(CustomAspectPropertiesModel propertyModel)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, false,
            this.customTypeModel.getName(), false, null);
    }

    public void addPropertyWithMinMaxValueConstraint(CustomAspectPropertiesModel propertyModel, int minValue, int maxValue)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, false, this.customTypeModel.getName(),
        true, customProperties.createMinMaxValueConstraintArray(minValue, maxValue));
    }

    public void addPropertyWithMinMaxLengthConstraint(CustomAspectPropertiesModel propertyModel, int minLength, int maxLength)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, false, this.customTypeModel.getName(),
        true, customProperties.createMinMaxLengthConstraint(minLength, maxLength));
    }

    public void addPropertyWithListOfValues(CustomAspectPropertiesModel propertyModel, boolean sorted, String... listOfValues)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, false, this.customTypeModel.getName(),
        true, customProperties.createListOfValuesConstraint(sorted, listOfValues));
    }

    public void deleteCustomType()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE,
            "cmm/{modelName}/types/{typeName}",
            this.customContentModel.getName(),
            this.customTypeModel.getName());
        restWrapper.processEmptyModel(request);
    }
}
