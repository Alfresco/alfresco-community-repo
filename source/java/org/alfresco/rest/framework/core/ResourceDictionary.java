package org.alfresco.rest.framework.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.rest.framework.Api;
import org.apache.commons.lang.StringUtils;

/**
 * A container for all information about resources and apis.
 *
 * @author Gethin James
 */
public class ResourceDictionary
{
    private final Map<Api,Map<String, ResourceWithMetadata>> allResources = new HashMap<Api,Map<String, ResourceWithMetadata>>();
    private final SortedSet<Api> publicApis = new TreeSet<Api>();
    private final SortedSet<Api> privateApis  = new TreeSet<Api>();
    private final String NEW_LINE = "\n";
    
    protected ResourceDictionary()
    {
        super();
    }

    /*
     * Return a key by combining the entity and relationship ids
     */
    public static String resourceKey(String entity, String relationship)
    {
        if (StringUtils.isNotBlank(relationship))
        {
            return "/"+entity+"/{entityId}/"+relationship;    
        }
        else
        {
            return "/"+entity;
        }
    }

    /*
     * Return a key by combining the entity and property ids
     */
    public static String propertyResourceKey(String entity, String property)
    {
      return "/"+entity+"/"+property;
    }
    
    /**
     * @return the allResources
     */
    public Map<Api, Map<String, ResourceWithMetadata>> getAllResources()
    {
        return this.allResources;
    }

    /**
     * @return the publicApis
     */
    public SortedSet<Api> getPublicApis()
    {
        return this.publicApis;
    }

    /**
     * @return the privateApis
     */
    public SortedSet<Api> getPrivateApis()
    {
        return this.privateApis;
    }

    /*
     * Prints a String representation of the Resource Dictionary
     */
    public String prettyPrint()
    {

        StringBuilder builder = new StringBuilder();
        builder.append("*******Resources********:").append(NEW_LINE);
        builder.append("**Public Apis **").append(NEW_LINE);
        for (Api api : this.publicApis)
        {
            printApi(builder,api);
        }
        builder.append(NEW_LINE);
        builder.append("**Private Apis **").append(NEW_LINE);
        for (Api api : this.privateApis)
        {
            printApi(builder,api);
        }
        builder.append("*******End of Resources ********:");
        return builder.toString();
    }
    
    private void printApi(StringBuilder builder, Api api)
    {
        builder.append(api).append(NEW_LINE);
        Map<String, ResourceWithMetadata> apiResources = allResources.get(api);     
        builder.append(apiResources.size()+ " resources.").append(NEW_LINE);
        
        Set<String> keys = apiResources.keySet();
        for (String key : keys)
        {
            builder.append("***"+key+"***").append(NEW_LINE);
            builder.append(apiResources.get(key).getMetaData()).append(NEW_LINE); 
        }
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceDictionary [allResources=");
        builder.append(this.allResources);
        builder.append(", publicApis=");
        builder.append(this.publicApis);
        builder.append(", privateApis=");
        builder.append(this.privateApis);
        builder.append("]");
        return builder.toString();
    }
    
    
}
