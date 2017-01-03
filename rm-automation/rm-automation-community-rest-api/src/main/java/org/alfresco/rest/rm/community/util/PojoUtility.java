/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for creating the json object
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class PojoUtility
{
    /**
     * Converting object to JSON string
     *
     * @param model  The java object model to convert
     * @throws JsonProcessingException Throws exceptions if the given object doesn't match to the POJO class model
     */
    public static String toJson(Object model) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        //include only values that differ from default settings to be included
        mapper.setSerializationInclusion(Include.NON_DEFAULT);
        try
        {
            //return the json object
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        }
        catch (JsonGenerationException e)
        {
            return e.toString();
        }
        catch (JsonMappingException e)
        {
            return e.toString();
        }
        catch (IOException e)
        {
            return e.toString();
        }
    }
}
