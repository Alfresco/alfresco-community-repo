/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomAspectModel;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

/**
 * @author Bogdan Bocancea
 */
public class CustomAspectModelManager extends ModelRequest<CustomAspectModelManager>
{
    private CustomContentModel customContentModel;
    private CustomAspectModel customAspectModel;
    private CustomModelProperties customProperties = new CustomModelProperties(restWrapper);


    public CustomAspectModelManager(CustomContentModel customContentModel, CustomAspectModel aspectModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
        this.customAspectModel = aspectModel;
    }

    public RestCustomAspectModel getAspect()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "cmm/{modelName}/aspects/{aspectName}?{parameters}",
            this.customContentModel.getName(), this.customAspectModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomAspectModel.class, request);
    }
    
    public void addProperty(CustomAspectPropertiesModel propertyModel)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, true,
            this.customAspectModel.getName(), false, null);
    }

    public void addPropertyWithMinMaxValueConstraint(CustomAspectPropertiesModel propertyModel, int minValue, int maxValue)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, true, this.customAspectModel.getName(),
true, customProperties.createMinMaxValueConstraintArray(minValue, maxValue));
    }

    public void addPropertyWithMinMaxLengthConstraint(CustomAspectPropertiesModel propertyModel, int minLength, int maxLength)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, true, this.customAspectModel.getName(),
true, customProperties.createMinMaxLengthConstraint(minLength, maxLength));
    }

    public void addPropertyWithListOfValues(CustomAspectPropertiesModel propertyModel, boolean sorted, String... listOfValues)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, true, this.customAspectModel.getName(),
true, customProperties.createListOfValuesConstraint(sorted, listOfValues));
    }

    public void addPropertyWithRegularExpression(CustomAspectPropertiesModel propertyModel, String regex)
    {
        customProperties.addProperty(propertyModel, this.customContentModel, true, this.customAspectModel.getName(),
                true, customProperties.createRegexConstraint(regex));
    }

    public void deleteAspectProperty(CustomAspectPropertiesModel propertyModel)
    {
        String body = JsonBodyGenerator.defineJSON()
            .add("name", this.customAspectModel.getName()).build().toString();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, body,
            "cmm/{modelName}/aspects/{aspectName}?select=props&delete={propertyName}",
            this.customContentModel.getName(),
            this.customAspectModel.getName(),
            propertyModel.getName());
        restWrapper.processEmptyModel(request);
    }
    
    public void deleteAspect()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE,
            "cmm/{modelName}/aspects/{aspectName}",
            this.customContentModel.getName(),
            this.customAspectModel.getName());
        restWrapper.processEmptyModel(request);
    }
}
