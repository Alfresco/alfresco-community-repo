/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.util;

import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating the json object
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class PojoUtility
{
    /**
     * Logger for the class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PojoUtility.class);

    /** Private constructor to prevent instantiation. */
    private PojoUtility()
    {}

    /**
     * see {@link #toJson(Object, Class, Class)}
     */
    public static String toJson(Object model)
    {
        mandatoryObject("model", model);

        return toJson(model, null, null);
    }

    /**
     * Converting object to JSON string
     *
     * @param model The java object model to convert
     * @param target Class (or interface) whose annotations to effectively override
     * @param mixinSource Class (or interface) whose annotations are to be "added" to target's annotations, overriding as necessary
     * @return The converted java object as JSON string
     * @throws JsonProcessingException  Throws exceptions if the given object doesn't match to the POJO class model
     */
    public static String toJson(Object model, Class<?> target, Class<?> mixinSource)
    {
        mandatoryObject("model", model);

        ObjectMapper mapper = new ObjectMapper();

        if (target != null && mixinSource != null)
        {
            //inject the "mix-in" annotations  from FilePlanComponentMix to
            // FilePlanComponent POJO class when converting to json
            mapper.addMixIn(target, mixinSource);
        }

        //include only non null values
        mapper.setSerializationInclusion(Include.NON_NULL);

        //return the json object
        try
        {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        }
        catch (JsonProcessingException error)
        {
            return error.toString();
        }
    }

    /**
     * Converting json to  java object
     *
     * @param json   The json object to convert
     * @param classz Class  for the java object
     * @return The converted java object
     * @throws JsonProcessingException Throws exceptions if the given object doesn't match to the POJO class model
     */
    public static <T> T jsonToObject(JSONObject json, Class<T> classz)
    {
        mandatoryObject("model", classz);
        mandatoryObject("jsonObject", json);

        ObjectMapper mapper = new ObjectMapper();

        T obj = null;
        try
        {
            obj = mapper.readValue(json.toString(), classz);
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to convert the json into a java object.", e);
        }

        return obj;
    }

    /**
     * Converting json array into a list of java objects
     *
     * @param json   The json array to convert
     * @param classz Class  for the java object
     * @return The list of converted java objects
     * @throws JsonProcessingException Throws exceptions if the given object doesn't match to the POJO class model
     */
    public static <T> List<T> jsonToObject(JSONArray json, Class<T> classz)
    {

        mandatoryObject("model", classz);
        mandatoryObject("jsonObject", json);

        ObjectMapper mapper = new ObjectMapper();

        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, classz);
        List<T> asList = null;
        try
        {
            asList = mapper.readValue(json.toString(), collectionType);
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to convert the json array into a java collection.", e);
        }


        return asList;
    }

}
