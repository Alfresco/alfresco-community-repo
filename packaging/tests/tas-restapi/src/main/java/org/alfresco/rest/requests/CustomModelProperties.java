package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
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
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        array.add(JsonBodyGenerator.defineJSON()
            .add("name", propertyModel.getName())
            .add("title", propertyModel.getTitle())
            .add("description", propertyModel.getDescription())
            .add("dataType", propertyModel.getDataType())
            .add("multiValued", propertyModel.isMultiValued())
            .add("mandatory", propertyModel.isMandatory())
            .add("mandatoryEnforced", propertyModel.isMandatoryEnforced()));
        if(hasConstraints)
        {
            array.add(JsonBodyGenerator.defineJSON().add("constraints", constraintsArray));
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
        JsonArrayBuilder constraintsArray = JsonBodyGenerator.defineJSONArray();
        JsonObjectBuilder param1 = JsonBodyGenerator.defineJSON()
            .add("name", "allowedValues")
            .add("listValue", listOfValues.toString());
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
