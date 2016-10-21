/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.body;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.alfresco.rest.model.PropertiesModel;
import org.alfresco.rest.model.RestFilePlanComponentModel;

/**
 * Helper for building JSON objects
 * @author Kristijan Conkas
 * @since 2.6
 */
public class IgJsonBodyGenerator
{
    private static JsonBuilderFactory jsonBuilderFactory;
    
    /**
     * @return the initialized JSON builder factory
     */
    private static JsonBuilderFactory getJsonBuilder()
    {
        if (jsonBuilderFactory == null)
        {
            return Json.createBuilderFactory(null);
        }
        else
        {
            return jsonBuilderFactory;
        }
    }
    
    public static String filePlanComponentCreate(RestFilePlanComponentModel model)
    {
        PropertiesModel properties = model.getProperties();
        JsonObjectBuilder valueBuilder = getJsonBuilder()
            .createObjectBuilder()
            .add("name", model.getName())
            .add("nodeType", model.getNodeType());
        if (properties != null)
        {
            // handle properties
            JsonObjectBuilder propertiesBuilder = getJsonBuilder().createObjectBuilder();
            if (properties.getTitle() != null)
            {
                propertiesBuilder.add("cm:title", properties.getTitle());
            }
            valueBuilder.add("properties", propertiesBuilder.build());
        }
        return valueBuilder.build().toString();
    }
}
