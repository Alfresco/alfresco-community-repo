package org.alfresco.rest.framework.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.Api.SCOPE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;

/*
 * Builds a Map representing the Resource entities and their permitted actions
 */
public class ResourceDictionaryBuilder
{
    private static Log logger = LogFactory.getLog(ResourceDictionaryBuilder.class);  
    
    private static void processResources(
                ResourceDictionary rd, Map<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>> entityApiMap,
                Map<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>> relationshipApiMap)
    {
        logger.debug("Will process resources ");
        
        for (Entry<ApiScopeKey, Map<Integer, List<ResourceWithMetadata>>> apiGroupedByNameAndScopeEntry : entityApiMap.entrySet())
        {
            logger.debug("Processing "+apiGroupedByNameAndScopeEntry.getKey());
            
            Map<Integer, List<ResourceWithMetadata>> apiVersions = apiGroupedByNameAndScopeEntry.getValue();
            List<Integer> versions = new ArrayList<Integer>(apiVersions.keySet());
            logger.debug("Versions "+String.valueOf(versions));
            Collections.sort(versions);
            
            //Loop through the different versions of the API and add them to the Map of resources By API with Version.
            for (int i=0;i<versions.size();i++)
            {
                Api apiVersion = Api.valueOf(apiGroupedByNameAndScopeEntry.getKey().name, apiGroupedByNameAndScopeEntry.getKey().scope.toString(), Integer.toString(versions.get(i)));
                Map<String, ResourceWithMetadata> resourcesByApiAndVersion = findResources(rd.getAllResources(),apiVersion);         
                
                logger.debug("Working with api "+apiVersion);
                //If there's a previous version, get them first.
                if (i>0) {
                    logger.debug("Has previous so adding all entities from previous version ");
                    Api previousVersion = Api.valueOf(apiVersion.getName(), apiVersion.getScope().toString(), Integer.toString(apiVersion.getVersion()-1));
                    logger.debug("Previous version is "+previousVersion);
                    Map<String, ResourceWithMetadata> resourcesForPreviousVersion = findResources(rd.getAllResources(),previousVersion);
                    resourcesByApiAndVersion.putAll(resourcesForPreviousVersion);
                }
                
                //Now add this version's resource (overwriting any from the previous version)
                for (ResourceWithMetadata resourceWithMetadata : apiGroupedByNameAndScopeEntry.getValue().get(apiVersion.getVersion()))
                {
                    resourcesByApiAndVersion.put(resourceWithMetadata.getMetaData().getUniqueId(), resourceWithMetadata);
                }
            }
        }
    }

    /**
     * Finds a resources map, if it doesn't exist then it will create it.
     * @param allResources
     * @param api Key
     * @return Map<String, ResourceWithMetadata>
     */
    private static Map<String, ResourceWithMetadata> findResources(Map<Api, Map<String, ResourceWithMetadata>> allResources, Api api)
    {
        Map<String, ResourceWithMetadata> resourcesByApiAndVersion = allResources.get(api);
        if (resourcesByApiAndVersion == null)
        {
            resourcesByApiAndVersion = new HashMap<String, ResourceWithMetadata>();
            allResources.put(api, resourcesByApiAndVersion);
        }
        return resourcesByApiAndVersion;
    }

    /**
     * Sort through the resources grouping them by api name/scope, then version.
     * @param resources
     * @return
     */
    private static <T> Map<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>> parseResources(Collection<Object> resources)
    {
        Map<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>> apiMap = new HashMap<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>>();
        
        for (Object bean : resources)
        {
           List<ResourceMetadata> metaData = ResourceInspector.inspect(bean.getClass());
           Api api = ResourceInspector.inspectApi(bean.getClass());
           if (api == null)
           {
               throw new PlatformRuntimeException("Invalid resource bean defintion.  No @WebApi defined for package: "+bean.getClass().getPackage().getName());
           }
           ApiScopeKey key = new ApiScopeKey(api);
           
           //Find api scope/name and use a key
           Map<Integer,List<ResourceWithMetadata>> apiVersions = apiMap.get(key);
           if (apiVersions == null)
           {
               //if doesn't exist then create it.
               apiVersions = new HashMap<Integer,List<ResourceWithMetadata>>();
               apiMap.put(key, apiVersions);
           }
           
           List<ResourceWithMetadata> resourcesWithMeta = apiVersions.get(api.getVersion());
           if (resourcesWithMeta == null)
           {
               //if doesn't exist then create it.
               resourcesWithMeta = new ArrayList<ResourceWithMetadata>();
               apiVersions.put(api.getVersion(), resourcesWithMeta);
           }
            //For each meta just add it to the list.
            for (ResourceMetadata resourceMeta : metaData)
            {
                resourcesWithMeta.add(new ResourceWithMetadata(bean, resourceMeta));
            }
        }
        
        return apiMap;
    }   

    /**
     * Builds a ResourceDictionary by parsing the resources that are passed in.
     * @param entityResources - object annotated as @EntityResource
     * @param relationshipResources - object annotated as @RelationshipResource
     * @return ResourceDictionary
     */
    public static ResourceDictionary build(Collection<Object> entityResources, Collection<Object> relationshipResources) {
   
            Collection<Object> entitiesAndRelations = new ArrayList<Object>();
            entitiesAndRelations.addAll(entityResources);
            entitiesAndRelations.addAll(relationshipResources);
            Map<ApiScopeKey,Map<Integer,List<ResourceWithMetadata>>> apiMap = parseResources(entitiesAndRelations);
            ResourceDictionary rd = new ResourceDictionary();
            processResources(rd,apiMap,null);
            processTopLevelApis(rd);
            logger.debug(rd.prettyPrint());
            return rd;
    }

    private static void processTopLevelApis(ResourceDictionary rd)
    {
        List<Api> apis = new ArrayList<Api>(rd.getAllResources().keySet());

        for (Api api : apis)
        {
            switch (api.getScope()) {
                case PUBLIC:
                    rd.getPublicApis().add(api);
                case PRIVATE:
                    rd.getPrivateApis().add(api);    
            }
        }
    }

    /**
     * A helper class to create a unique key for the combination of API name and API scope
     *
     * @author Gethin James
     */
    public static class ApiScopeKey{
        final String name;
        final SCOPE scope;
        
        public ApiScopeKey(Api api)
        {
            this.name = api.getName();
            this.scope = api.getScope();
        }

        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            result = prime * result + ((this.scope == null) ? 0 : this.scope.hashCode());
            return result;
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ApiScopeKey other = (ApiScopeKey) obj;
            if (this.name == null)
            {
                if (other.name != null) return false;
            }
            else if (!this.name.equals(other.name)) return false;
            if (this.scope != other.scope) return false;
            return true;
        }
        
        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("ApiScopeKey [name=").append(this.name).append(", scope=").append(this.scope).append("]");
            return builder.toString();
        }
    }
}
