/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.framework.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpMethod;

/**
 * Used for locating resources, implements ResourceLocator
 * Contains a ResourceDictionary (which is a reference to all the resources available)
 *
 * @author Gethin James
 */
public class ResourceLookupDictionary implements ResourceLocator
{
    private static Log logger = LogFactory.getLog(ResourceLookupDictionary.class);  
    
    private ResourceDictionary dictionary;

    @Override
    public Map<String, String> parseTemplateVars(Map<String, String> templateVars)
    {
        if ("preferences".equals(templateVars.get(ResourceLocator.RELATIONSHIP_RESOURCE)))
        {
            // REPO-855: special case for backwards-compatibility (see PersonPreferencesRelation.java)
            return templateVars;
        }

        String leftover = templateVars.get(ResourceLocator.LEFTOVER);
        if (StringUtils.isNotBlank(leftover))
        {
            Map<String, String> templateVars2 = new HashMap<>();
            templateVars2.putAll(templateVars);

            String[] split = leftover.split("/");
            if (split.length > 0)
            {
                // eg. r1 in /nodes/n1/versions/v1/renditions/r1
                templateVars2.put(RELATIONSHIP2_ID, split[0]);
            }
            if (split.length > 1)
            {
                // eg. content in /nodes/n1/versions/v1/renditions/r1/content
                templateVars2.put(PROPERTY2, split[1]);
            }

            return templateVars2;
        }
        else
        {
            return templateVars;
        }
    }

    @Override
    public ResourceWithMetadata locateEntityResource(Api api, String entityResource, HttpMethod httpMethod) throws NotFoundException, UnsupportedResourceOperationException
    {
        return locateRelationResource(api, entityResource, (String)null, httpMethod);
    }

    @Override
    public ResourceWithMetadata locateRelationPropertyResource(Api api, String entityResource, String relationResource, String property, HttpMethod httpMethod) throws NotFoundException,UnsupportedResourceOperationException
    {
        String resourceKey = ResourceDictionary.resourceKey(entityResource, relationResource);
        String propertyResourceKey = ResourceDictionary.propertyResourceKey(resourceKey, property);
        Map<String, ResourceWithMetadata> apiResources = dictionary.getAllResources().get(api);
        if (apiResources == null)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID);
        }

        ResourceWithMetadata resource = apiResources.get(propertyResourceKey);
        if (resource == null)
        {
            if (relationResource != null)
            {
                //Get entity/relationship resource and check if we are referencing a relation on it.
                resourceKey = ResourceDictionary.resourceKey(entityResource, relationResource);
                String relationResourceKey = ResourceDictionary.resourceKey(resourceKey, property);
                resource = apiResources.get(relationResourceKey);
                if (resource != null)
                {
                    ResourceOperation op = resource.getMetaData().getOperation(httpMethod);
                    if (op == null) { throw new UnsupportedResourceOperationException(); }
                    return resource;
                }
            }
            logger.warn("Unable to locate resource for: "+entityResource+" "+relationResource==null ? "" : relationResource+" "+property==null ? "" : property);
            throw new NotFoundException("Unable to locate resource for: "+entityResource+" "+(relationResource==null ? "" : relationResource+" "+property==null ? "" : property));
        }
        else
        {
            ResourceOperation op = resource.getMetaData().getOperation(httpMethod);
            if (op == null) { throw new UnsupportedResourceOperationException(); }
            return resource;
        }
    }

    @Override
    public ResourceWithMetadata locateRelationResource(Api api, String entityResource, String relationResource, HttpMethod httpMethod) throws NotFoundException,UnsupportedResourceOperationException
    {
        String resourceKey = ResourceDictionary.resourceKey(entityResource, relationResource);
        if (logger.isDebugEnabled())
        {
          logger.debug("Locating resource :" + resourceKey);
        }
        Map<String, ResourceWithMetadata> apiResources = dictionary.getAllResources().get(api);
        if (apiResources == null)
        {
          throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID);
        } 
        ResourceWithMetadata resource = apiResources.get(resourceKey);
        if (resource == null)
        {
          if (relationResource != null)
          {
              //Get entity resource and check if we are referencing a property on it.
              resourceKey = ResourceDictionary.propertyResourceKey(entityResource, relationResource);
              resource = apiResources.get(resourceKey);
              if (resource != null)
              {
                  ResourceOperation op = resource.getMetaData().getOperation(httpMethod);
                  if (op == null) { throw new UnsupportedResourceOperationException(); }
                  return resource;
              }
          }
          logger.warn("Unable to locate resource for: "+entityResource+" "+relationResource==null ? "" : relationResource);
          throw new NotFoundException("Unable to locate resource for: "+entityResource+" "+relationResource==null ? "" : relationResource);
        } 
        else
        {
            ResourceOperation op = resource.getMetaData().getOperation(httpMethod);
            if (op == null) { throw new UnsupportedResourceOperationException(); }
            return resource;
        }
    }

    /**
     * Locates a resource by URI path and wraps it in an invoker
     * 
     * This will probably get refactored later when we work out what we 
     * are doing with the discoverability model.  It shouldn't create
     * a new instance every time.
     */
    @Override
    public ResourceWithMetadata locateResource(Api api,Map<String, String> templateVars, HttpMethod httpMethod)
    {
        Map<String, String> resourceVars = parseTemplateVars(templateVars);

        String collectionName = resourceVars.get(COLLECTION_RESOURCE);
        String entityId = resourceVars.get(ENTITY_ID);
        String resourceName = resourceVars.get(RELATIONSHIP_RESOURCE);
        String property =  resourceVars.get(PROPERTY);
        String property2 = resourceVars.get(PROPERTY2);

        if (StringUtils.isNotBlank(property2))
        {
            String resourceName2 = ResourceDictionary.resourceKey(resourceName, property);
            return locateRelationPropertyResource(api,collectionName ,resourceName2, property2, httpMethod);
        }
        if (StringUtils.isNotBlank(property))
        {
            return locateRelationPropertyResource(api,collectionName ,resourceName, property,httpMethod);
        }
        if (StringUtils.isNotBlank(resourceName))
        {
            return locateRelationResource(api,collectionName ,resourceName,httpMethod);
        }
        if (StringUtils.isNotBlank(entityId))
        {
            return locateEntityResource(api,collectionName,httpMethod);
        }
        if (StringUtils.isNotBlank(collectionName))
        {
            return locateEntityResource(api,collectionName,httpMethod);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Unable to locate a resource for "+templateVars);
        }
        throw new NotFoundException();

    }

    @Override
    public  Map<String,ResourceWithMetadata> locateEmbeddedResources(Api api, Map<String,String> embeddedKeys)
    {
        if (embeddedKeys!=null && !embeddedKeys.isEmpty())
        {
            Map<String,ResourceWithMetadata> embeds = new HashMap<String,ResourceWithMetadata>();
            for (Entry<String, String> embeddedEntry : embeddedKeys.entrySet())
            {
                ResourceWithMetadata res = locateEntityResource(api,embeddedEntry.getValue(), HttpMethod.GET);  
                embeds.put(embeddedEntry.getKey(), res);
            }
            return embeds;

        }
        return Collections.emptyMap();
    }
   
    @Override
    public Map<String, ResourceWithMetadata> locateRelationResource(Api api, String entityKey, Collection<String> relationshipKeys, HttpMethod httpMethod)
    {
        if (relationshipKeys != null && !relationshipKeys.isEmpty())
        {
            Map<String,ResourceWithMetadata> embeds = new HashMap<String,ResourceWithMetadata>();
            
            for (String key : relationshipKeys)
            {
                embeds.put(key, locateRelationResource(api, entityKey, key, httpMethod));
            }

            return embeds;
        }
        return Collections.emptyMap();
    }

    /**
     * @param dictionary the dictionary to set
     */
    public void setDictionary(ResourceDictionary dictionary)
    {
        this.dictionary = dictionary;
    }
    
    public ResourceDictionary getDictionary()
    {
        return this.dictionary;
    }


}
