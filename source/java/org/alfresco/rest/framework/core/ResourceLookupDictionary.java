package org.alfresco.rest.framework.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    public ResourceWithMetadata locateEntityResource(Api api, String entityResource, HttpMethod httpMethod) throws InvalidArgumentException, UnsupportedResourceOperationException
    {
        return locateRelationResource(api, entityResource, (String)null, httpMethod);
    }

    @Override
    public ResourceWithMetadata locateRelationResource(Api api, String entityResource, String relationResource, HttpMethod httpMethod) throws InvalidArgumentException,UnsupportedResourceOperationException
    {
        String resourceKey = ResourceDictionary.resourceKey(entityResource, relationResource);
        if (logger.isDebugEnabled())
        {
          logger.debug("Locating resource :" + resourceKey);
        }
        Map<String, ResourceWithMetadata> apiResources = dictionary.getAllResources().get(api);
        if (apiResources == null)
        {
          throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_INVALID_API);         
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
                  if (!resource.getMetaData().supports(httpMethod)) { throw new UnsupportedResourceOperationException(); }
                  return resource;
              }
          }
          logger.warn("Unable to locate resource resource for :"+entityResource+" "+relationResource==null?"":relationResource);
          throw new InvalidArgumentException("Unable to locate resource resource for :"+entityResource+" "+(relationResource==null?"":relationResource));      
        } 
        else
        {
            if (!resource.getMetaData().supports(httpMethod)) { throw new UnsupportedResourceOperationException(); }
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
        String collectionName = templateVars.get(COLLECTION_RESOURCE);
        String entityId = templateVars.get(ENTITY_ID);
        String resourceName = templateVars.get(RELATIONSHIP_RESOURCE);
        
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
