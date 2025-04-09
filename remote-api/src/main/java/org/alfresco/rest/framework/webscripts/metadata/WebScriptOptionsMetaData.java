/*
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
package org.alfresco.rest.framework.webscripts.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceMetadata.RESOURCE_TYPE;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.metadata.ResourceMetaDataWriter;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.webscripts.ApiWebScript;

/**
 * Provides general information about an Api call and its methods.
 * 
 * @author Gethin James
 */
public class WebScriptOptionsMetaData extends ApiWebScript implements ResourceMetaDataWriter
{

    private static Log logger = LogFactory.getLog(WebScriptOptionsMetaData.class);
    private ResourceLookupDictionary lookupDictionary;
    private Map<String, ResourceMetaDataWriter> writers;

    public void setLookupDictionary(ResourceLookupDictionary lookupDictionary)
    {
        this.lookupDictionary = lookupDictionary;
    }

    @Override
    public void execute(final Api api, WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        ResourceDictionary resourceDic = lookupDictionary.getDictionary();
        Map<String, ResourceWithMetadata> apiResources = resourceDic.getAllResources().get(api);
        if (apiResources == null)
        {
            throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_INVALID_API);
        }
        String collectionName = templateVars.get(ResourceLocator.COLLECTION_RESOURCE);
        String resourceName = templateVars.get(ResourceLocator.RELATIONSHIP_RESOURCE);
        String resourceKey = ResourceDictionary.resourceKey(collectionName, resourceName);
        if (logger.isDebugEnabled())
        {
            logger.debug("Locating resource :" + resourceKey);
        }
        ResourceWithMetadata resource = apiResources.get(resourceKey);
        if (resource == null)
        {
            // Get entity resource and check if we are referencing a property on it.
            resourceKey = ResourceDictionary.propertyResourceKey(collectionName, resourceName);
            resource = apiResources.get(resourceKey);
        }
        ResourceMetaDataWriter writer = chooseWriter(req);
        writer.writeMetaData(res.getOutputStream(), resource, apiResources);

    }

    /**
     * Chooses the correct writer to use based on the supplied "format" param
     * 
     * @param req
     *            - the WebScriptRequest
     * @return ResourceMetaDataWriter - a matching writer - DEFAULT is this class.
     */
    protected ResourceMetaDataWriter chooseWriter(WebScriptRequest req)
    {
        if (writers != null)
        {
            ResourceMetaDataWriter theWriter = writers.get(req.getParameter("format"));
            if (theWriter != null)
                return theWriter;
        }
        return this;
    }

    /**
     * Processes the resulting resource and returns the data to be displayed
     * 
     * @param resource
     *            ResourceWithMetadata
     * @return Either a ExecutionResult or a CollectionWithPagingInfo
     */
    public static Object processResult(ResourceWithMetadata resource, Map<String, ResourceWithMetadata> apiResources)
    {
        List<ExecutionResult> results = new ArrayList();
        if (RESOURCE_TYPE.ENTITY.equals(resource.getMetaData().getType()))
        {
            results.add(new ExecutionResult(resource, null));
            for (ResourceWithMetadata aResource : apiResources.values())
            {
                if (resource.getMetaData().getUniqueId().equals(aResource.getMetaData().getParentResource()))
                {
                    results.add(new ExecutionResult(aResource, null));
                }
            }
        }
        if (results.isEmpty())
        {
            return new ExecutionResult(resource, null);
        }
        else
        {
            return CollectionWithPagingInfo.asPaged(Paging.DEFAULT, results);
        }
    }

    @Override
    public void writeMetaData(OutputStream out, ResourceWithMetadata resource, Map<String, ResourceWithMetadata> allApiResources) throws IOException
    {

        final Object result = processResult(resource, allApiResources);

        assistant.getJsonHelper().withWriter(out, new Writer() {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                    throws JsonGenerationException, JsonMappingException, IOException
            {
                objectMapper.writeValue(generator, result);
            }
        });
    }

    public void setWriters(Map<String, ResourceMetaDataWriter> writers)
    {
        this.writers = writers;
    }

}
