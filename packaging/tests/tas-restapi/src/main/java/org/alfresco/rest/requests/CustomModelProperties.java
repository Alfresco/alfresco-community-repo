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
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Bogdan Bocancea
 */
public class CustomModelProperties extends ModelRequest<CustomModelProperties>
{
    public CustomModelProperties(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public void addProperty(CustomAspectPropertiesModel propertyModel,
                            CustomContentModel customContentModel,
                            boolean isAspect,
                            String aspectOrTypeName,
                            boolean hasConstraints,
                            JsonArrayBuilder constraintsArray)
    {
        JsonArrayBuilder array;
        if(hasConstraints)
        {
            array = getPropertiesArray(propertyModel, true, constraintsArray);
        }
        else
        {
            array = getPropertiesArray(propertyModel, false, null);
        }

        String body = JsonBodyGenerator.defineJSON()
            .add("name", aspectOrTypeName)
            .add("properties", array).build().toString();
        String urlPath;
        if(isAspect)
        {
            urlPath = "cmm/{modelName}/aspects/{aspectName}?select=props";
        }
        else
        {
            urlPath = "cmm/{modelName}/types/{typeName}?select=props";
        }
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, body, urlPath,
            customContentModel.getName(), aspectOrTypeName);
        restWrapper.processEmptyModel(request);
    }

    private JsonArrayBuilder getPropertiesArray(CustomAspectPropertiesModel propertyModel, boolean hasConstraints, JsonArrayBuilder constraintsArray)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();;
        if(hasConstraints)
        {
            array.add(JsonBodyGenerator.defineJSON()
                .add("name", propertyModel.getName())
                .add("title", propertyModel.getTitle())
                .add("description", propertyModel.getDescription())
                .add("dataType", propertyModel.getDataType())
                .add("multiValued", propertyModel.isMultiValued())
                .add("mandatory", propertyModel.isMandatory())
                .add("mandatoryEnforced", propertyModel.isMandatoryEnforced())
                .add("constraints", constraintsArray));
        }
        else
        {
            array.add(JsonBodyGenerator.defineJSON()
                .add("name", propertyModel.getName())
                .add("title", propertyModel.getTitle())
                .add("description", propertyModel.getDescription())
                .add("dataType", propertyModel.getDataType())
                .add("multiValued", propertyModel.isMultiValued())
                .add("mandatory", propertyModel.isMandatory())
                .add("mandatoryEnforced", propertyModel.isMandatoryEnforced()));
        }
        return array;
    }

    public JsonArrayBuilder createMinMaxValueConstraintArray(int minValue, int maxValue)
    {
        JsonArrayBuilder constraintsArray = JsonBodyGenerator.defineJSONArray();
        JsonObjectBuilder param1 = JsonBodyGenerator.defineJSON()
            .add("name", "minValue")
            .add("simpleValue", Integer.valueOf(minValue));
        JsonObjectBuilder param2 = JsonBodyGenerator.defineJSON()
            .add("name", "maxValue")
            .add("simpleValue", Integer.valueOf(maxValue));
        JsonArrayBuilder parameters = JsonBodyGenerator.defineJSONArray();
        parameters.add(0, param1).add(1, param2);
        constraintsArray.add(JsonBodyGenerator.defineJSON()
            .add("name", "MINMAX_" + UUID.randomUUID())
            .add("type", "MINMAX")
            .add("parameters", parameters));
        return constraintsArray;
    }

    public JsonArrayBuilder createMinMaxLengthConstraint(int minLength, int maxLength)
    {
        JsonArrayBuilder constraintsArray = JsonBodyGenerator.defineJSONArray();
        JsonObjectBuilder param1 = JsonBodyGenerator.defineJSON()
            .add("name", "minLength")
            .add("simpleValue", Integer.valueOf(minLength));
        JsonObjectBuilder param2 = JsonBodyGenerator.defineJSON()
            .add("name", "maxLength")
            .add("simpleValue", Integer.valueOf(maxLength));
        JsonArrayBuilder parameters = JsonBodyGenerator.defineJSONArray();
        parameters.add(0, param1).add(1, param2);
        constraintsArray.add(JsonBodyGenerator.defineJSON()
            .add("name", "LENGTH_" + UUID.randomUUID())
            .add("type", "LENGTH")
            .add("parameters", parameters));
        return constraintsArray;
    }

    public JsonArrayBuilder createListOfValuesConstraint(boolean sorted, String... listOfValues)
    {
        JsonArrayBuilder valuesArray = JsonBodyGenerator.defineJSONArray();
        Arrays.stream(listOfValues).forEach(valuesArray::add);
        JsonArrayBuilder constraintsArray = JsonBodyGenerator.defineJSONArray();
        JsonObjectBuilder param1 = JsonBodyGenerator.defineJSON()
            .add("name", "allowedValues")
            .add("listValue", valuesArray);
        JsonObjectBuilder param2 = JsonBodyGenerator.defineJSON()
            .add("name", "sorted")
            .add("simpleValue", Boolean.valueOf(sorted));
        JsonArrayBuilder parameters = JsonBodyGenerator.defineJSONArray();
        parameters.add(0, param1).add(1, param2);
        constraintsArray.add(JsonBodyGenerator.defineJSON()
            .add("name", "LIST_" + UUID.randomUUID())
            .add("type", "LIST")
            .add("parameters", parameters));
        return constraintsArray;
    }

    public JsonArrayBuilder createRegexConstraint(String regex)
    {
        JsonArrayBuilder constraintsArray = JsonBodyGenerator.defineJSONArray();
        JsonObjectBuilder param1 = JsonBodyGenerator.defineJSON()
            .add("name", "expression")
            .add("simpleValue", regex);
        JsonObjectBuilder param2 = JsonBodyGenerator.defineJSON()
            .add("name", "requiresMatch")
            .add("simpleValue", true);
        JsonArrayBuilder parameters = JsonBodyGenerator.defineJSONArray();
        parameters.add(0, param1).add(1, param2);
        constraintsArray.add(JsonBodyGenerator.defineJSON()
            .add("name", "REGEX_" + UUID.randomUUID())
            .add("type", "REGEX")
            .add("parameters", parameters));
        return constraintsArray;
    }
}
