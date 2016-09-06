/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rest.framework.tools;

import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/*
 * Reads information from the request
 *
 * @author Gethin James
 */
public interface RequestReader
{
    /**
     * Extracts the body contents from the request
     *
     * @param req          the request
     * @param jsonHelper   Jackson Helper
     * @param requiredType the type to return
     * @return the Object in the required type
     */
    default <T> T extractJsonContent(WebScriptRequest req, JacksonHelper jsonHelper, Class<T> requiredType)
    {
        Reader reader;
        try
        {
            reader = req.getContent().getReader();
            return jsonHelper.construct(reader, requiredType);
        }
        catch (JsonMappingException e)
        {
            rrLogger().warn("Could not read content from HTTP request body.", e);
            throw new InvalidArgumentException("Could not read content from HTTP request body.");
        }
        catch (IOException e)
        {
            throw new ApiException("Could not read content from HTTP request body.", e.getCause());
        }
    }

    /**
     * Extracts the body contents from the request as a List, the JSON can be an array or just a single value without the [] symbols
     *
     * @param req          the request
     * @param jsonHelper   Jackson Helper
     * @param requiredType the type to return (without the List param)
     * @return A List of "Object" as the required type
     */
    default <T> List<T> extractJsonContentAsList(WebScriptRequest req, JacksonHelper jsonHelper, Class<T> requiredType)
    {
        Reader reader;
        try
        {
            reader = req.getContent().getReader();
            return jsonHelper.constructList(reader, requiredType);
        }
        catch (IOException e)
        {
            throw new ApiException("Could not read content from HTTP request body.", e.getCause());
        }
    }

    default Log rrLogger()
    {
        return LogFactory.getLog(this.getClass());
    }

}
